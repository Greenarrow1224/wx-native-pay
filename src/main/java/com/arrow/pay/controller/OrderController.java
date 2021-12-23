package com.arrow.pay.controller;

import com.arrow.pay.common.Result;
import com.arrow.pay.entity.OrderInfo;
import com.arrow.pay.enums.OrderStatus;
import com.arrow.pay.service.OrderInfoService;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderController
 *
 * @author Greenarrow
 * @date 2021-12-17 14:34
 **/
@CrossOrigin
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderInfoService orderInfoService;


    @GetMapping("/list")
    public Result listOrder(){
        List<OrderInfo> list = orderInfoService.list();
        return Result.ok().data("orderList",list);
    }

    @GetMapping("/status/{orderNo}")
    public Result queryOrderStatus(@PathVariable String orderNo){
        String orderStatus = orderInfoService.getOrderStatus(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)){
            return Result.ok();
        }
        return Result.ok().setCode(HttpStatus.SC_PROCESSING).setMessage("支付中");

    }


}
