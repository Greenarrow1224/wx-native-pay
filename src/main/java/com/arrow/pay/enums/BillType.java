package com.arrow.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账单类型枚举类
 *
 * @author Greenarrow
 * @date 2021-12-24 8:03
 **/
@Getter
@AllArgsConstructor
public enum  BillType {
    /**
     * 申请资金账单
     */
    FUNDFLOWBILL("fundflowbill"),

    /**
     * 申请交易账单
     */
    TRADEBILL("tradebill");

    private final String type;
}
