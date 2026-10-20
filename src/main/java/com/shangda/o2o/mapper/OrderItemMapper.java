package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单明细 Mapper
 */
@Mapper
public interface OrderItemMapper {

    int insert(OrderItem item);

    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /** 一次查多个订单的明细（订单列表页用） */
    List<OrderItem> findByOrderIds(@Param("orderIds") List<Long> orderIds);
}
