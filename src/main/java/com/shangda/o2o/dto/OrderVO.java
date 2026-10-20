package com.shangda.o2o.dto;

import com.shangda.o2o.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图（订单 + 明细）
 */
@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer payMethod;
    private Integer payStatus;
    private Integer orderStatus;
    private String addressSnapshot;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<OrderItem> items;
}
