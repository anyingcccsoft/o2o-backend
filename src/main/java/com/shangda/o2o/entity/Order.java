package com.shangda.o2o.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体（对应 orders 表）
 */
@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    /** 支付方式 1微信/2支付宝/3货到付款 */
    private Integer payMethod;
    /** 支付状态 0待支付/1已支付 */
    private Integer payStatus;
    /** 订单状态 0待接单/1制作中/2配送中/3已完成/4已取消 */
    private Integer orderStatus;
    private String addressSnapshot;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
