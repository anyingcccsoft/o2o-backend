package com.shangda.o2o.entity;

import lombok.Data;

/**
 * 购物车实体（对应 cart 表）
 */
@Data
public class Cart {
    private Long id;
    private Long userId;
    private Long dishId;
    private Integer quantity;
}
