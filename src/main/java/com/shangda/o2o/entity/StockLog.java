package com.shangda.o2o.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水实体（对应 stock_log 表）
 */
@Data
public class StockLog {
    private Long id;
    private Long dishId;
    /** 类型 1下单扣减/2取消回补/3后台调整 */
    private Integer changeType;
    /** 变动数量（负为扣减） */
    private Integer quantityDelta;
    private String orderNo;
    private String operator;
    private LocalDateTime createdAt;
}
