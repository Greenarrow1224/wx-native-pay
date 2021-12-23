package com.arrow.pay.controller;

import com.arrow.pay.common.Result;
import com.arrow.pay.entity.Product;
import com.arrow.pay.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ProductController
 *
 * @author Greenarrow
 * @date 2021-12-17 14:36
 **/
@CrossOrigin
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @GetMapping("/list")
    public Result list(){
        List<Product> data = productService.list();
        return Result.ok().data("productList",data);
    }
}
