package com.arrow.pay.entity;

import com.arrow.pay.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * RefundInfo
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/
@Data
@TableName("t_refund_info")
@Accessors(chain = true)
public class RefundInfo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 9220528102879200413L;
    /**
     * 商品订单编号
     */
    private String orderNo;

    /**
     * 退款单编号
     */
    private String refundNo;

    /**
     * 支付系统退款单号
     */
    private String refundId;

    /**
     * 支付系统退款单号
     */
    private Integer totalFee;

    /**
     * 退款金额(分)
     */
    private Integer refund;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款单状态
     */
    private String refundStatus;

    /**
     * 申请退款返回参数
     */
    private String contentReturn;

    /**
     * 申请退款返回参数
     */
    private String contentNotify;
}
