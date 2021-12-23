package com.arrow.pay.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.util.Date;

/**
 * 公共类
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/
public class BaseEntity {

    private Date createTime;

    private Date updateTime;
}
