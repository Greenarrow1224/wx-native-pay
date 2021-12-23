package com.arrow.pay.service.impl;


import com.arrow.pay.entity.Product;
import com.arrow.pay.mapper.ProductMapper;
import com.arrow.pay.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * ProductServiceImpl
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/


@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
}
