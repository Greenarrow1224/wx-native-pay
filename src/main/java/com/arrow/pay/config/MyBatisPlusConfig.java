package com.arrow.pay.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MP配置类
 *
 * @author Greenarrow
 * @date 2021-12-17 14:22
 **/
@Configuration
@MapperScan("com.arrow.pay.mapper")
@EnableTransactionManagement
public class MyBatisPlusConfig {
}
