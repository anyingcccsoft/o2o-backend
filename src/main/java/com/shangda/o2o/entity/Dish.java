package com.shangda.o2o.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品实体（对应 dish 表）
 */
@Data
public class Dish {
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    /** 状态 1上架/0下架 */
    private Integer status;
    private Integer sales;
}
