package com.shangda.o2o.service;

import com.shangda.o2o.dto.CartItemVO;

import java.util.List;

/**
 * 购物车服务（所有操作均校验归属当前用户）
 */
public interface CartService {

    List<CartItemVO> listItems(Long userId);

    void addItem(Long userId, Long dishId, Integer quantity);

    void updateQuantity(Long userId, Long cartId, Integer quantity);

    void removeItem(Long userId, Long cartId);

    void clear(Long userId);
}
