package com.arrow.pay.job;

import com.arrow.pay.entity.OrderInfo;
import com.arrow.pay.service.OrderInfoService;
import com.arrow.pay.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ren xiao fei
 * @date 2021-12-23 16:37
 **/
@Component
@Slf4j
public class WxPayJob {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private WxPayService wxPayService;

    /**
     * 从第0秒开始每隔30秒执行1次，查询创建超过5分钟，并且未支付的订单
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm(){
        log.info("{}被执行......",this.getClass().getSimpleName());
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByMinutes(5);
        orderInfoList.forEach(orderInfo -> {
            String orderNo = orderInfo.getOrderNo();
            log.info("超时订单===> {}",orderNo);
            // 核实订单状态，调用微信端查询订单接口
            wxPayService.checkOrderStatus(orderNo);

        });
    }
}
