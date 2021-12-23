package com.arrow.pay.entity;

import com.arrow.pay.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * PaymentInfo
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/
@Data
@TableName("t_payment_info")
@Accessors(chain = true)
public class PaymentInfo extends BaseEntity {

    private static final long serialVersionUID = -2803376647839435571L;
    /**
     * 商品订单编号
     */
    private String orderNo;

    /**
     * 支付系统交易编号
     */
    private String transactionId;

    /**
     * 支付类型
     */
    private String paymentType;
    /**
     * 交易类型
     */
    private String tradeType;

    /**
     * 交易状态
     */
    private String tradeState;

    /**
     * 支付金额(分)
     */
    private Integer payerTotal;

    /**
     * 通知参数
     */
    private String content;
}
