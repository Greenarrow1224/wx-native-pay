package com.arrow.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PayType {
    /**
     * 微信
     */
    WX_NATIVE("微信 Native 支付"),

    /**
     * 微信公众号支付
     */
    WX_H5("微信公众号支付"),

    /**
     * 支付宝
     */
    ALIPAY("支付宝");

    /**
     * 类型
     */
    private final String type;
}
