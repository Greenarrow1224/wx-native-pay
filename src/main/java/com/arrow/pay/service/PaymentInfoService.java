package com.arrow.pay.service;

/**
 * PaymentInfoService
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/

public interface PaymentInfoService {

    /**
     * 记录支付日志
     * @param plainText
     */
    void createPaymentInfo(String plainText);
}
