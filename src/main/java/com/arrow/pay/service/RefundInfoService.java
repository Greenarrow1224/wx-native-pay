package com.arrow.pay.service;


import com.arrow.pay.entity.RefundInfo;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * RefundInfoService
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/
public interface RefundInfoService extends IService<RefundInfo> {

    /**
     * 创建退款单记录
     * @param orderNo
     * @param reason
     * @return
     */
    RefundInfo createRefundByOrderNo(String orderNo, String reason);

    /**
     * 更新退款单信息
     * @param content
     */
    void updateRefund(String content);

    /**
     * 根据订单编号查询退款编号
     * @param orderNo
     * @return
     */
    RefundInfo queryRefundByOrderNo(String orderNo);

    /**
     *找出申请退款超过 minutes分钟并且未成功的退款单
     * @param minutes
     * @return
     */
    List<RefundInfo> getNoRefundOrder(int minutes);
}
