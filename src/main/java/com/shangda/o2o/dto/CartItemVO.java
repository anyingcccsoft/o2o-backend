package com.shangda.o2o.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车列表项（cart 联查 dish）
 */
@Data
public class CartItemVO {
    private Long id;
    private Long dishId;
    private Integer quantity;
    private String dishName;
    private String dishImage;
    private BigDecimal price;
    private Integer stock;
    /** 菜品上架状态 1上架/0下架 */
    private Integer dishStatus;
}
