package com.arrow.pay.job;

import com.arrow.pay.entity.RefundInfo;
import com.arrow.pay.service.RefundInfoService;
import com.arrow.pay.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Greenarrow
 * @date 2021-12-23 16:37
 **/
@Component
@Slf4j
public class RefundsPayJob {

    @Autowired
    private RefundInfoService refundInfoService;

    @Autowired
    private WxPayService wxPayService;

    /**
     * 从第0秒开始每隔30秒执行1次，查询创建超过5分钟，并且未成功退款的订单
     */
    @Scheduled(cron = "${time.cron}")
    public void orderConfirm(){
        log.info("{}被执行......",this.getClass().getSimpleName());
        List<RefundInfo> refundInfoList = refundInfoService.getNoRefundOrder(5);
        refundInfoList.forEach(refundInfo -> {
            String refundNo = refundInfo.getRefundNo();
            log.info("超时未退款的退款单号===> {}",refundNo);
            // 核实订单状态，调用微信端查询退款接口
            wxPayService.checkRefundStatus(refundNo);

        });
    }
}
