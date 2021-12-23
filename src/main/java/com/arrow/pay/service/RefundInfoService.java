package com.arrow.pay.service;


import com.arrow.pay.entity.RefundInfo;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
