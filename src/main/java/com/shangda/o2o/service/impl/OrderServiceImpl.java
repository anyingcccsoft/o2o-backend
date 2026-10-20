package com.shangda.o2o.service.impl;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.OrderCreateRequest;
import com.shangda.o2o.dto.OrderVO;
import com.shangda.o2o.entity.Address;
import com.shangda.o2o.entity.Cart;
import com.shangda.o2o.entity.Dish;
import com.shangda.o2o.entity.Order;
import com.shangda.o2o.entity.OrderItem;
import com.shangda.o2o.entity.StockLog;
import com.shangda.o2o.entity.User;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.AddressMapper;
import com.shangda.o2o.mapper.CartMapper;
import com.shangda.o2o.mapper.DishMapper;
import com.shangda.o2o.mapper.OrderItemMapper;
import com.shangda.o2o.mapper.OrderMapper;
import com.shangda.o2o.mapper.StockLogMapper;
import com.shangda.o2o.mapper.UserMapper;
import com.shangda.o2o.service.OrderService;
import com.shangda.o2o.util.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户端订单服务实现（下单事务 + 原子扣库存防超卖）
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** 库存流水类型：1 下单扣减 / 2 取消回补 */
    private static final int STOCK_CHANGE_ORDER = 1;
    private static final int STOCK_CHANGE_CANCEL = 2;

    /** 订单状态：0 待接单 / 1 制作中 / 4 已取消 */
    private static final int ORDER_STATUS_CANCELLED = 4;

    private final CartMapper cartMapper;
    private final DishMapper dishMapper;
    private final AddressMapper addressMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StockLogMapper stockLogMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Map<String, Object> create(Long userId, OrderCreateRequest request) {
        // 1. 读取购物车
        List<Cart> carts = cartMapper.findByUserId(userId);
        if (carts.isEmpty()) {
            throw new BusinessException("购物车为空，无法下单");
        }
        // 2. 校验收货地址归属
        Address address = addressMapper.findById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }
        User user = userMapper.findById(userId);
        String operator = user != null ? user.getUsername() : String.valueOf(userId);

        // 3. 校验菜品上架并原子扣库存，同时计算金额
        String orderNo = OrderNoGenerator.generate(userId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (Cart cart : carts) {
            Dish dish = dishMapper.findById(cart.getDishId());
            if (dish == null || dish.getStatus() == null || dish.getStatus() != 1) {
                String name = dish != null ? dish.getName() : "部分菜品";
                throw new BusinessException(name + "已下架，请从购物车移除后重试");
            }
            // 原子扣减：影响行数为 0 说明库存不足，抛异常回滚整个事务
            int affected = dishMapper.deductStock(dish.getId(), cart.getQuantity());
            if (affected == 0) {
                throw new BusinessException(dish.getName() + "库存不足");
            }
            BigDecimal subtotal = dish.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishImage(dish.getImageUrl());
            item.setPrice(dish.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(subtotal);
            items.add(item);
        }

        // 4. 写订单（地址快照：收件人 电话 区域 详址）
        String snapshot = address.getConsignee() + " " + address.getPhone() + " "
                + (address.getRegion() != null ? address.getRegion() + " " : "") + address.getDetail();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayMethod(request.getPayMethod());
        order.setPayStatus(0);
        order.setOrderStatus(0);
        order.setAddressSnapshot(snapshot);
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        // 5. 写订单明细与库存流水
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            insertStockLog(item.getDishId(), STOCK_CHANGE_ORDER, -item.getQuantity(), orderNo, operator);
        }

        // 6. 清空购物车
        cartMapper.deleteByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", orderNo);
        return result;
    }

    @Override
    public void pay(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (order.getPayStatus() != 0) {
            throw new BusinessException("订单已支付，请勿重复支付");
        }
        if (order.getOrderStatus() == ORDER_STATUS_CANCELLED) {
            throw new BusinessException("订单已取消，无法支付");
        }
        orderMapper.updatePayStatus(orderId, 1);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        int status = order.getOrderStatus();
        if (status != 0 && status != 1) {
            throw new BusinessException("当前订单状态不可取消");
        }
        User user = userMapper.findById(userId);
        String operator = user != null ? user.getUsername() : String.valueOf(userId);

        orderMapper.updateOrderStatus(orderId, ORDER_STATUS_CANCELLED);
        // 回补库存并写流水
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem item : items) {
            dishMapper.restoreStock(item.getDishId(), item.getQuantity());
            insertStockLog(item.getDishId(), STOCK_CHANGE_CANCEL, item.getQuantity(),
                    order.getOrderNo(), operator);
        }
    }

    @Override
    public PageResult<OrderVO> page(Long userId, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        long total = orderMapper.countByUserId(userId);
        List<Order> orders = orderMapper.findByUserIdPage(userId, (safePage - 1) * safeSize, safeSize);
        if (orders.isEmpty()) {
            return PageResult.of(total, safePage, safeSize, Collections.emptyList());
        }
        // 批量查明细后按订单分组
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderItem>> itemMap = orderItemMapper.findByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        List<OrderVO> vos = orders.stream()
                .map(o -> toVO(o, itemMap.getOrDefault(o.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        return PageResult.of(total, safePage, safeSize, vos);
    }

    @Override
    public OrderVO detail(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        return toVO(order, orderItemMapper.findByOrderId(orderId));
    }

    /** 查询订单并校验归属当前用户 */
    private Order getOwnedOrder(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void insertStockLog(Long dishId, int changeType, int quantityDelta, String orderNo, String operator) {
        StockLog log = new StockLog();
        log.setDishId(dishId);
        log.setChangeType(changeType);
        log.setQuantityDelta(quantityDelta);
        log.setOrderNo(orderNo);
        log.setOperator(operator);
        stockLogMapper.insert(log);
    }

    private OrderVO toVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayMethod(order.getPayMethod());
        vo.setPayStatus(order.getPayStatus());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setAddressSnapshot(order.getAddressSnapshot());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setItems(items);
        return vo;
    }
}
