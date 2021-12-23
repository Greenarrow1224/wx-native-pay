package com.arrow.pay.service;

import com.arrow.pay.dto.ProductDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * WxPayService
 *
 * @author ren xiao fei
 * @date 2021-12-17 16:50
 **/
public interface WxPayService {
    /**
     * native 下单
     * @param productDTO
     * @return
     */
    Map<String, Object> nativePay(ProductDTO productDTO);

    /**
     * 处理微信通知支付成功的订单
     * @param bodyMap
     */
    void processOrder(Map<String, Object> bodyMap);

    /**
     * 申请退款
     * @param orderNo
     * @param reason
     */
    void refund(String orderNo, String reason);

    /**
     * 用户取消订单
     * @param orderNo
     */
    void cancel(String orderNo);

    /**
     * 处理退款回调通知
     * @param request
     * @param response
     * @return
     */
    String refundsNotify(HttpServletRequest request, HttpServletResponse response);

    /**
     * 处理退款和订单
     * @param bodyMap
     * @throws Exception
     */
    void processRefund(Map<String, Object> bodyMap);


    /**
     * 调用微信接口查询订单
     * @param orderNo
     * @return
     */
    String queryOrder(String orderNo);

    /**
     * 根据订单号查询微信支付查单接口，核实订单状态
     * 如果订单已支付，则更新商户端订单状态，并记录支付日志
     * 如果订单未支付，则调用关单接口关闭订单，并更新商户端订单状态
     * @param orderNo
     */
    void checkOrderStatus(String orderNo);
}
