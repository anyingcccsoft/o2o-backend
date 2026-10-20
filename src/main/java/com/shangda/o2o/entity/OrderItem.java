package com.shangda.o2o.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体（对应 order_item 表）
 */
@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long dishId;
    private String dishName;
    private String dishImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
