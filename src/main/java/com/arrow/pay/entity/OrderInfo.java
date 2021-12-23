package com.arrow.pay.entity;

import com.arrow.pay.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * OrderInfo
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("t_order_info")
public class OrderInfo  extends BaseEntity implements Serializable{

    private static final long serialVersionUID = 8536803407441287501L;
    /**
     * 订单标题
     */
    private String title;

    /**
     * 商户订单编号
     */
    private String orderNo;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 支付产品id
     */
    private Long productId;

    /**
     * 支付产品数量
     */
    private Integer productNumber;

    /**
     * 订单金额(分)
     */
    private Integer totalFee;

    /**
     * 订单二维码连接
     */
    private String codeUrl;

    /**
     * 订单状态
     */
    private String orderStatus;
}
