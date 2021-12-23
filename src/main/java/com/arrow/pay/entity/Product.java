package com.arrow.pay.entity;

import com.arrow.pay.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Product
 *
 * @author Greenarrow
 * @date 2021-12-17 13:49
 **/
@Data
@TableName("t_product")
public class Product extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -1773094867402874104L;

    //定义主键策略：跟随数据库的主键自增
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 商品名称
     */
    private String title;


    /**
     * 价格（分）
     */
    private Integer price;
}
