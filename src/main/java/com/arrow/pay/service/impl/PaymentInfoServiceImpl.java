package com.arrow.pay.service.impl;


import com.arrow.pay.entity.PaymentInfo;
import com.arrow.pay.enums.PayType;
import com.arrow.pay.mapper.PaymentInfoMapper;
import com.arrow.pay.service.PaymentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * PaymentInfoServiceImpl
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/

@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
    @Override
    public void createPaymentInfo(String plainText) {
        log.info("记录支付日志");
        Gson gson = new Gson();
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String)plainTextMap.get("out_trade_no");
        String transactionId = (String)plainTextMap.get("transaction_id");
        String tradeType = (String)plainTextMap.get("trade_type");
        String tradeState = (String)plainTextMap.get("trade_state");
        Map<String, Object> amount = (Map)plainTextMap.get("amount");
        Integer payerTotal = ((Double) amount.get("payer_total")).intValue();
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo)
        .setPaymentType(PayType.WXPAY.getType())
        .setTransactionId(transactionId)
        .setTradeType(tradeType)
        .setTradeState(tradeState)
        .setPayerTotal(payerTotal)
        .setContent(plainText);
        baseMapper.insert(paymentInfo);
    }
}
