package com.arrow.pay.service;


import com.arrow.pay.dto.ProductDTO;
import com.arrow.pay.entity.OrderInfo;
import com.arrow.pay.enums.OrderStatus;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * OrderInfoService
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/
public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 生成订单
     * @param productId
     * @return
     */
    OrderInfo createOrder(Long productId,Integer productNumber, Integer totalPrice);

    /**
     * 保存订单二维码
     * @param orderNo
     * @param codeUrl
     */
    void saveCodeUrl(String orderNo, String codeUrl);

    /**
     * 根据商品 id 获取未支付订单
     * @param productId
     * @return
     */
    OrderInfo getNoPayOrder(Long productId);

    /**
     * 根据订单号修改订单状态
     * @param orderNo
     * @param success
     */
    void updateStatusByOrderNo(String orderNo, OrderStatus success);

    /**
     * 根据订单编号获取订单状态
     * @param orderNo
     * @return
     */
    String getOrderStatus(String orderNo);

    /**
     * 根据订单编号查询订单信息
     * @param orderNo
     * @return
     */
    OrderInfo getOrderByOrderNo(String orderNo);

    /**
     *获取超过指定时间的未支付订单
     * @param minutes
     * @return
     */
    List<OrderInfo> getNoPayOrderByMinutes(int minutes);

    /**
     * 获取超过指定时间的未退款订单
     * @param minutes
     * @return
     */
    List<OrderInfo> getNoRefundsOrderByMinutes(int minutes);

}
