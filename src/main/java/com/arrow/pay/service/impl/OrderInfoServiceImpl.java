package com.arrow.pay.service.impl;


import com.arrow.pay.dto.ProductDTO;
import com.arrow.pay.entity.OrderInfo;
import com.arrow.pay.entity.Product;
import com.arrow.pay.enums.OrderStatus;
import com.arrow.pay.mapper.OrderInfoMapper;
import com.arrow.pay.mapper.ProductMapper;
import com.arrow.pay.service.OrderInfoService;
import com.arrow.pay.util.OrderNoUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OrderInfoServiceImpl
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private ProductMapper productMapper;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo createOrder(Long productId,Integer productNumber, Integer totalPrice) {
        OrderInfo noPayOrder = this.getNoPayOrder(productId);
        if( noPayOrder != null){
            return noPayOrder;
        }
        // 获取商品
        Product product = productMapper.selectById(productId);
        // 生成订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTitle("测试订单")
                .setOrderNo(OrderNoUtils.getOrderNo())
                .setProductId(productId)
                // 如有相关优惠券则需按相应规则设置
                .setTotalFee(totalPrice)
                .setProductNumber(productNumber)
                .setOrderStatus(OrderStatus.NOTPAY.getType());
        // 入库
        baseMapper.insert(orderInfo);
        return orderInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCodeUrl(String orderNo, String codeUrl) {
        baseMapper.update(new OrderInfo().setCodeUrl(codeUrl),new QueryWrapper<OrderInfo>().eq("order_no",orderNo));
    }

    @Override
    public OrderInfo getNoPayOrder(Long productId) {
        // 如有 userId 则需再根据当前登录人筛选
        return orderInfoMapper.selectOne(new QueryWrapper<OrderInfo>().eq("product_id",productId).eq("order_status",OrderStatus.NOTPAY.getType()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus) {
        log.info("更新订单状态 ===> {}", orderStatus.getType());
        baseMapper.update(new OrderInfo().setOrderStatus(orderStatus.getType()),new QueryWrapper<OrderInfo>().eq("order_no",orderNo));
    }

    @Override
    public String getOrderStatus(String orderNo) {
        OrderInfo orderInfo = baseMapper.selectOne(new QueryWrapper<OrderInfo>().eq("order_no", orderNo));
        if (orderInfo == null){
            return null;
        }
        return orderInfo.getOrderStatus();
    }

    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {
        return baseMapper.selectOne(new QueryWrapper<OrderInfo>().eq("order_no", orderNo));
    }

    /**
     * 获取创建超过指定分钟并且未支付的订单
     * @param minutes
     * @return
     */
    @Override
    public List<OrderInfo> getNoPayOrderByMinutes(int minutes) {
        Instant instant = Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)).minus(Duration.ofMillis(minutes));
        log.info("=========>"+instant);
        return baseMapper.selectList(new QueryWrapper<OrderInfo>()
                .eq("order_status",OrderStatus.NOTPAY.getType()).le("create_time",instant));

    }

    @Override
    public List<OrderInfo> getNoRefundsOrderByMinutes(int minutes) {
        Instant instant = Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)).minus(Duration.ofMillis(minutes));
        log.info("=========>"+instant);
        return baseMapper.selectList(new QueryWrapper<OrderInfo>()
                .eq("order_status",OrderStatus.REFUND_PROCESSING.getType()).le("create_time",instant));
    }
}
