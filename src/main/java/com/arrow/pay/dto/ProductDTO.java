package com.arrow.pay.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 下单商品信息 DTO
 * @author Greenarrow
 * @date 2021-12-22 15:46
 **/
@Data
public class ProductDTO {


    /**
     * 商品id
     */
    @NotNull(message = "商品id不能为空")
    private Long productId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    private Integer productNumber;


    /**
     * 总价格（分）
     */
    @NotNull(message = "商品总价格不能为空")
    private Integer totalPrice;
}
