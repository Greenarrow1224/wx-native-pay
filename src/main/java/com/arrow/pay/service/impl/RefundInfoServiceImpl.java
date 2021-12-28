package com.arrow.pay.service.impl;

import com.arrow.pay.controller.WxPayController;
import com.arrow.pay.entity.OrderInfo;
import com.arrow.pay.entity.RefundInfo;
import com.arrow.pay.mapper.RefundInfoMapper;
import com.arrow.pay.service.OrderInfoService;
import com.arrow.pay.service.RefundInfoService;
import com.arrow.pay.service.WxPayService;
import com.arrow.pay.util.OrderNoUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * RefundInfoServiceImpl
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/

@Service
@Slf4j
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {


    @Autowired
    private OrderInfoService orderInfoService;




    @Override
    public RefundInfo createRefundByOrderNo(String orderNo, String reason) {

        // 根据订单号获取订单信息
        OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(orderNo);
        // 根据订单号生成退款订单
        RefundInfo refundInfo = new RefundInfo()
                // 订单编号
        .setOrderNo(orderNo)
                // 退款单编号
        .setRefundNo(OrderNoUtils.getRefundNo())
                // 原订单金额(分)
        .setTotalFee(orderInfo.getTotalFee())
                // 退款金额(分)
        .setRefund(orderInfo.getTotalFee())
                // 退款原因
        .setReason(reason);
        // 保存退款订单
        baseMapper.insert(refundInfo);
        return refundInfo;
    }

    @Override
    public void updateRefund(String content) {
        // 将json字符串转换成Map
        Gson gson = new Gson();
        Map<String, String> resultMap = gson.fromJson(content, HashMap.class);
        // 根据退款单编号修改退款单
        // 设置要修改的字段
        RefundInfo refundInfo = new RefundInfo();
        // 微信支付退款单号
        refundInfo.setRefundId(resultMap.get("refund_id"));
        // 查询退款和申请退款中的返回参数
        if(resultMap.get("status") != null){
            // 退款状态
            refundInfo.setRefundStatus(resultMap.get("status"));
            // 将全部响应结果存入数据库的content字段
            refundInfo.setContentReturn(content);
        }
        // 退款回调中的回调参数
        if(resultMap.get("refund_status") != null){
            // 退款状态
            refundInfo.setRefundStatus(resultMap.get("refund_status"));
            // 将全部响应结果存入数据库的content字段
            refundInfo.setContentNotify(content);
        }
        // 更新退款单
        baseMapper.update(refundInfo, new QueryWrapper<RefundInfo>().eq("refund_no", resultMap.get("out_refund_no")));
    }

    @Override
    public RefundInfo queryRefundByOrderNo(String orderNo) {
        return baseMapper.selectOne(new QueryWrapper<RefundInfo>().eq("order_no",orderNo));
    }
}
