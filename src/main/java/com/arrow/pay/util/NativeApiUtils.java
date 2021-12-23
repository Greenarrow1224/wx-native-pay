package com.arrow.pay.util;

import com.arrow.pay.config.WxPayConfig;
import com.arrow.pay.entity.OrderInfo;
import com.arrow.pay.enums.wxpay.WxApiType;
import com.arrow.pay.enums.wxpay.WxNotifyType;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Native 支付 API 工具类
 * @author Greenarrow
 * @date 2021-12-23 13:32
 **/
@Slf4j
public class NativeApiUtils {

    @Autowired
    private static WxPayConfig wxPayConfig;

    @Autowired
    private static CloseableHttpClient wxPayClient;

    public static Map<String, Object> nativePay(OrderInfo orderInfo){
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        Map<String,Object> paramsMap = new HashMap<>(12);
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
        Gson gson = new Gson();
        String jsonParams = gson.toJson(paramsMap);
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
                Map<String,String> resultMap = gson.fromJson(bodyString, HashMap.class);
                String codeUrl = resultMap.get("code_url");
                Map<String, Object> data = new HashMap<>(2);
                data.put("codeUrl", codeUrl);
                data.put("orderNo", orderInfo.getOrderNo());
                return data;
            } else if (HttpStatus.SC_NO_CONTENT == statusCode){
                log.info("Native下单请求成功，没有响应体");
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

}
