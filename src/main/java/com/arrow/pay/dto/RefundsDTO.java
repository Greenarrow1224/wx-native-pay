package com.arrow.pay.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author ren xiao fei
 * @date 2021-12-23 11:38
 **/
@Data
public class RefundsDTO implements Serializable {

    @NotBlank(message = "商户订单编号不能为空")
    private String orderNo;
    private String reason;
}
