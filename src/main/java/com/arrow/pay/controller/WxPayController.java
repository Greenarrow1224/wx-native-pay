package com.arrow.pay.controller;

import com.arrow.pay.common.Result;
import com.arrow.pay.config.NativeWeChatPay2Validator;
import com.arrow.pay.dto.ProductDTO;
import com.arrow.pay.dto.RefundsDTO;
import com.arrow.pay.service.WxPayService;
import com.arrow.pay.util.HttpUtils;
import com.baomidou.mybatisplus.extension.api.R;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付
 *
 * @author Greenarrow
 * @date 2021-12-17 16:46
 **/
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/wx/pay")
public class WxPayController {

    @Autowired
    private WxPayService wxPayService;

    /**
     * native 统一下单
     * @return
     */
    @PostMapping("/native/pay")
    public Result nativePay(@Validated @RequestBody ProductDTO productDTO){
        Map<String,Object> data = wxPayService.nativePay(productDTO);
        return Result.ok().setData(data);
    }

    @GetMapping("/query/{orderNo}")
    public Result queryOrder(@PathVariable String orderNo) {
       String bodyString =  wxPayService.queryOrder(orderNo);
        return Result.ok().setMessage("查询订单").data("body",bodyString);
    }

    /**
     * 接收支付通知回调
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response){
        Gson gson = new Gson();
        //应答对象
        Map<String, String> map = new HashMap<>();
        String body = HttpUtils.readData(request);
        Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
        log.info("支付通知的id ===> {}", bodyMap.get("id"));
        log.info("支付通知的完整数据 ===> {}", body);
        // 处理订单
        wxPayService.processOrder(bodyMap);
        response.setStatus(HttpStatus.SC_OK);
        map.put("code", "SUCCESS");
        map.put("message", "成功");
        return gson.toJson(map);
    }


    /**
     * 用户申请退款
     * @param refundsDTO
     * @return
     */
    @PostMapping("/refunds")
    public Result refunds(@Validated @RequestBody RefundsDTO refundsDTO){
        wxPayService.refund(refundsDTO.getOrderNo(), refundsDTO.getReason());
        return Result.ok();
    }

    @PostMapping("/refunds/notify")
    public String refundsNotify(HttpServletRequest request, HttpServletResponse response){
        return wxPayService.refundsNotify(request,response);
    }

    /**
     * 用户取消订单
     * @param orderNo
     * @return
     */
    @GetMapping("/cancel/orderNo")
    public Result cancel(@PathVariable String orderNo){
        wxPayService.cancel(orderNo);
        return Result.ok().setMessage("订单已取消");
    }
}
