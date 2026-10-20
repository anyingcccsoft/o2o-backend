package com.shangda.o2o.service;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.OrderCreateRequest;
import com.shangda.o2o.dto.OrderVO;

import java.util.Map;

/**
 * 用户端订单服务
 */
public interface OrderService {

    /** 从购物车创建订单（扣库存、写快照、清购物车），返回 {id, orderNo} */
    Map<String, Object> create(Long userId, OrderCreateRequest request);

    /** 模拟支付 */
    void pay(Long userId, Long orderId);

    /** 取消订单并回补库存 */
    void cancel(Long userId, Long orderId);

    PageResult<OrderVO> page(Long userId, int page, int pageSize);

    OrderVO detail(Long userId, Long orderId);
}
