package com.arrow.pay.service.impl;

import com.arrow.pay.config.NativeWeChatPay2Validator;
import com.arrow.pay.config.WxPayConfig;
import com.arrow.pay.dto.ProductDTO;
import com.arrow.pay.entity.OrderInfo;
import com.arrow.pay.entity.RefundInfo;
import com.arrow.pay.enums.BillType;
import com.arrow.pay.enums.OrderStatus;
import com.arrow.pay.enums.wxpay.WxApiType;
import com.arrow.pay.enums.wxpay.WxNotifyType;
import com.arrow.pay.enums.wxpay.WxRefundStatus;
import com.arrow.pay.enums.wxpay.WxTradeState;
import com.arrow.pay.service.OrderInfoService;
import com.arrow.pay.service.PaymentInfoService;
import com.arrow.pay.service.RefundInfoService;
import com.arrow.pay.service.WxPayService;
import com.arrow.pay.util.HttpUtils;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * WxPayServiceImpl
 *
 * @author Greenarrow
 * @date 2021-12-17 16:50
 **/
@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private WxPayConfig wxPayConfig;

    @Autowired
    private Verifier verifier;

    @Autowired
    private RefundInfoService refundInfoService;

    @Autowired
    private CloseableHttpClient wxPayClient;

    @Autowired
    private CloseableHttpClient wxPayNoSignClient;


    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public Map<String, Object> nativePay(ProductDTO productDTO) {
        log.info("{}生成订单",this.getClass().getSimpleName());
        // 生成订单
        OrderInfo orderInfo = orderInfoService.createOrder(productDTO.getProductId(),productDTO.getProductNumber(),productDTO.getTotalPrice());
        // 先查询本地有没有订单，如果有并且二维码有效则直接返回，没有则调用微信统一下单接口
        if (orderInfo != null && StringUtils.isNotBlank(orderInfo.getCodeUrl())){
            String codeUrl = orderInfo.getCodeUrl();
            log.info("订单已存在，二维码已保存");
            //返回二维码
            Map<String, Object> data = new HashMap<>();
            data.put("codeUrl", codeUrl);
            data.put("orderNo", orderInfo.getOrderNo());
            return data;
        }
        log.info("调用微信端统一下单支付API");
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        // 封装请求 body 参数
        Gson gson = new Gson();
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        paramsMap.put("notify_url",
                wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        Map<String,Object> amountMap = new HashMap<>();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);
        // 将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数：" + jsonParams);
        StringEntity entity = new StringEntity(jsonParams,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpPost);
            String bodyString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == statusCode){
                log.info("请求成功：{}",bodyString);
                Map<String,String> resultMap = gson.fromJson(bodyString, HashMap.class);
                String codeUrl = resultMap.get("code_url");
                String orderNo = orderInfo.getOrderNo();
                orderInfoService.saveCodeUrl(orderNo,codeUrl);
                log.info(codeUrl);
                Map<String, Object> data = new HashMap<>(2);
                data.put("codeUrl", codeUrl);
                data.put("orderNo", orderInfo.getOrderNo());
                return data;
            } else if (HttpStatus.SC_NO_CONTENT == statusCode){
                log.info("请求成功，没有响应体");
            }else {
                log.info("Native下单失败,响应码 = " + statusCode+ ",返回结果 = " + bodyString);
            }
        } catch (Exception e) {
            log.error("请求失败：",e);
            e.printStackTrace();
        }finally {
            if (response != null){
                try {
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public String queryRefund(String refundNo) {
        log.info("调用查询退款API");
        String url = String.format(WxApiType.DOMESTIC_REFUNDS_QUERY.getType(),refundNo);
        url = wxPayConfig.getDomain().concat(url);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        // 完成签名并执行请求，并完成验签
        CloseableHttpResponse response =null;
        try {
            response = wxPayClient.execute(httpGet);
            // 解析响应结果
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                log.info("查询退款成功");
                return bodyAsString;
            } else if (statusCode == HttpStatus.SC_NO_CONTENT) {
                log.info("查询退款成功，没有响应体");
            } else {
                throw new RuntimeException("查询退款异常, 响应码 = " + statusCode + ", 查询退款返回结果 = " + bodyAsString);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Map<String, Object> bodyMap) {
        log.info("处理支付回调成功的订单");
        String plainText = decryptFromResource(bodyMap);

        if (StringUtils.isNotBlank(plainText)){
            //转换明文
            Gson gson = new Gson();
            Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
            String orderNo = (String)plainTextMap.get("out_trade_no");

            if (lock.tryLock()){
                // 处理重复通知
                // 保证接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return;
                }
                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
                // 记录支付日志
                paymentInfoService.createPaymentInfo(plainText);
                lock.unlock();
            }

        }
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refund(String orderNo, String reason) {
        log.info("创建退款单记录");
        // 根据订单编号创建退款单
        RefundInfo refundsInfo = refundInfoService.createRefundByOrderNo(orderNo, reason);
        log.info("调用微信退款API");
        // 调用统一下单API
        String url =
                wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost httpPost = new HttpPost(url);
        // 封装请求body参数
        Gson gson = new Gson();
        Map<String, Object> paramsMap = new HashMap<>(8);
        // 订单编号
        paramsMap.put("out_trade_no", orderNo);
        // 退款单编号
        paramsMap.put("out_refund_no", refundsInfo.getRefundNo());
        // 退款原因
        paramsMap.put("reason", reason);
        // 退款通知地址
        paramsMap.put("notify_url",
                wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));
        Map<String, Object> amountMap = new HashMap<>(3);
        // 退款金额
        amountMap.put("refund", refundsInfo.getRefund());
        // 原订单金额
        amountMap.put("total", refundsInfo.getTotalFee());
        // 退款币种
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);
        // 将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> " + jsonParams);
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        // 设置请求报文格式
        entity.setContentType("application/json");
        // 将请求报文放入请求对象
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");//设置响应报文格式
        // 完成签名并执行请求，并完成验签
        CloseableHttpResponse response =null;
        try {
            response = wxPayClient.execute(httpPost);
            // 解析响应结果
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                log.info("测试成功");
            }else if (statusCode == HttpStatus.SC_NO_CONTENT){
                log.info("微信退款成功，没有响应体");
            }else {
                throw new RuntimeException("退款异常, 响应码 = " + statusCode+ ", 退款返回结果 = " + bodyAsString);
            }
            // 更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);
            // 更新退款单
            refundInfoService.updateRefund(bodyAsString);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
           if (response != null){
               try {
                   response.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }
    }

    @Override
    public String refundsNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("退款通知执行");
        Gson gson = new Gson();
        Map<String, Object> hashMap = new HashMap<>(3);

        try {
            // 处理退款通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            String requestId = (String)bodyMap.get("id");
            log.info("支付通知的id ===> {}", requestId);
            NativeWeChatPay2Validator nativeWeChatPay2Validator = new NativeWeChatPay2Validator(verifier, requestId, body);
            if (!nativeWeChatPay2Validator.validate(request)){
                log.error("退款通知验签失败");
                //失败应答
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                hashMap.put("code", "ERROR");
                hashMap.put("message", "通知验签失败");
                return gson.toJson(hashMap);

            }
            log.info("退款通知验签成功");
            // 处理退款单
            this.processRefund(bodyMap);
            //成功应答
            response.setStatus(HttpStatus.SC_OK);
            hashMap.put("code", "SUCCESS");
            hashMap.put("message", "成功");
            return gson.toJson(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Map<String, Object> bodyMap) {
        String decrypt = decryptFromResource(bodyMap);
        Gson gson = new Gson();
        Map<String,Object> map = gson.fromJson(decrypt, HashMap.class);
        String orderNo = map.get("out_trade_no").toString();
        if (lock.tryLock()){
          try {
              String orderStatus = orderInfoService.getOrderStatus(orderNo);
              if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus)){
                  return;
              }
              // 更新订单状态
              orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.REFUND_SUCCESS);
              // 更新退款单
              refundInfoService.updateRefund(decrypt);
          }finally {
              lock.unlock();
          }
        }

    }

    @Override
    public void cancel(String orderNo) {
        // 调用微信支付关单接口
        this.closerOrder(orderNo);
        // 更新商户端的订单状态
        orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.CANCEL);
    }

    /**
     * 微信订单查询有两种方式，这里采用的是根据商户订单号查询
     * @param orderNo
     * @return
     */
    @Override
    public String queryOrder(String orderNo) {
        log.info("查单接口调用 ===> {}", orderNo);
        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        // 完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpGet);
            String bodyString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == statusCode){
                log.info("查询微信端订单返回成功：{}",bodyString);
            }else if (HttpStatus.SC_NO_CONTENT == statusCode){
                log.info("查询微信端订单返回成功,没有响应体");
            }else {
                log.info("下单失败，响应码：{} 返回结果：{}",statusCode,bodyString);
                throw new IOException("request failed");
            }
            return bodyString;
        }catch (Exception e){
            e.printStackTrace();
            log.error("请求失败",e);
        }finally {
            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void checkOrderStatus(String orderNo) {
        log.info("根据订单号核实订单状态 ===> {}", orderNo);
        String resultString = this.queryOrder(orderNo);
        Gson gson = new Gson();
        Map<String,Object> resultMap = gson.fromJson(resultString, HashMap.class);
        // 获取微信支付端的订单状态
        Object tradeState = resultMap.get("trade_state");
        if (WxTradeState.SUCCESS.getType().equals(tradeState)){
            log.info("核实订单已支付 ===> {}", orderNo);
            // 更新本地订单状态为已支付
            orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.SUCCESS);
            // 记录支付日志
            paymentInfoService.createPaymentInfo(resultString);
        }

        if (WxTradeState.NOTPAY.getType().equals(tradeState)){
            log.warn("核实订单未支付 ===> {}", orderNo);
            // 调用关单方法
            this.closerOrder(orderNo);
            // 修改本地订单状态为超时已关闭
            orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.CLOSED);

        }

    }

    @Override
    public void checkRefundStatus(String refundNo) {
        log.info("根据退款单号核实退款单状态====> {}",refundNo);
        String result = this.queryRefund(refundNo);
        Gson gson = new Gson();
        Map resultMap = gson.fromJson(result, HashMap.class);
        String status = resultMap.get("status").toString();
        String orderNo = resultMap.get("out_trade_no").toString();
        if (WxRefundStatus.SUCCESS.getType().equals(status)){
            log.info("核实订单已退款成功====> {}",orderNo);
            // 更新订单状态为已退款
            orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.REFUND_SUCCESS);
            // 更新退款单
            refundInfoService.updateRefund(result);
        }
        // 核实订单退款异常
        if (WxRefundStatus.ABNORMAL.getType().equals(status)){
            log.info("核实订单退款异常====> {}",orderNo);
            // 更新订单状态为退款异常
            orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.REFUND_ABNORMAL);
            // 更新退款单记录
            refundInfoService.updateRefund(result);
        }



    }

    @Override
    public String queryTradeBill(String billDate, String type) {
        log.info("申请账单接口：{}，{}",billDate,type);
        String url;
        if (BillType.TRADEBILL.getType().equals(type)){
            url = WxApiType.TRADE_BILLS.getType();
        }else if (BillType.FUNDFLOWBILL.getType().equals(type)){
            url = WxApiType.FUND_FLOW_BILLS.getType();
        }else {
            throw new RuntimeException("不支持的交易账单类型！");
        }
        url = wxPayConfig.getDomain().concat(url).concat("?bill_date=").concat(billDate);
        // 创建远程 Get 请求对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");
        // 使用 wxPayClient 发送请求得到响应
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String bodyString = EntityUtils.toString(entity);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == statusCode){
                log.info("账单申请成功：{}",bodyString);
            }else if (HttpStatus.SC_NO_CONTENT == statusCode){
                log.info("账单申请成功，没有响应体");
            }else {
                throw new RuntimeException("申请账单异常, 响应码: {}" + statusCode+ ", 申请账单返回结果 = " + bodyString);
            }
            Gson gson = new Gson();
            Map<String,String> resultMap = gson.fromJson(bodyString, HashMap.class);
            return resultMap.get("download_url");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public String downloadBill(String billDate, String type) {
        // 调用账单查询方法获取账单下载url
        String downloadUrl = this.queryTradeBill(billDate, type);
        // 创建远程 Get 请求对象
        HttpGet httpGet = new HttpGet(downloadUrl);
        httpGet.addHeader("Accept", "application/json");
        //使用 wxPayClient 发送请求得到响应
        CloseableHttpResponse response = null;
        try {
            response = wxPayNoSignClient.execute(httpGet);
            String bodyString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == statusCode){
                log.info("账单下载成功，返回信息：{}",bodyString);
            }else if (HttpStatus.SC_NO_CONTENT == statusCode){
                log.info("账单下载成功，没有返回体");
            }else {
                throw new RuntimeException("账单下载失败，Code："+statusCode+"返回信息"+bodyString);
            }
           return bodyString;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void closerOrder(String orderNo){
        log.info("开始关单，订单号 ===> {}", orderNo);
        // 创建远程请求对象
        String url = String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url);
        HttpPost httpPost = new HttpPost(url);
        // 封装 json 请求体
        Gson gson = new Gson();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}", jsonParams);
        StringEntity stringEntity = new StringEntity(jsonParams, "UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        httpPost.setHeader("Accept", "application/json");
        //完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == statusCode){
                log.info("关单请求成功");
            }else if (HttpStatus.SC_NO_CONTENT == statusCode){
                log.info("关单请求成功，但没有相应体");
            }else {
                log.info("关单失败，响应码：{}",statusCode);
                throw new IOException("request failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }










    /**
     * 对称解密
     * @param bodyMap
     * @return
     * @throws GeneralSecurityException
     */
    private String decryptFromResource(Map<String, Object> bodyMap)  {
        log.info("密文解密");
        // 通知数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        // 数据密文
        String ciphertext = resourceMap.get("ciphertext");
        // 随机串
        String nonce = resourceMap.get("nonce");
        // 附加数据
        String associatedData = resourceMap.get("associated_data");
        log.info("密文 ===> {}", ciphertext);
        AesUtil aesUtil = new
                AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        String plainText = null;
        try {
            plainText = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),
                    nonce.getBytes(StandardCharsets.UTF_8),
                    ciphertext);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        log.info("明文 ===> {}", plainText);
        return plainText;
    }
}
