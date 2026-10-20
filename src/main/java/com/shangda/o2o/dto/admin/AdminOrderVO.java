package com.shangda.o2o.dto.admin;

import com.shangda.o2o.entity.Order;
import com.shangda.o2o.entity.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 管理端订单视图对象：订单字段 + 下单用户名 +（详情时）订单明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminOrderVO extends Order {
    /** 下单用户名（关联 user 表） */
    private String username;
    /** 订单明细（仅详情接口填充） */
    private List<OrderItem> items;
}
