package com.shangda.o2o.service.impl;

import com.shangda.o2o.dto.CartItemVO;
import com.shangda.o2o.entity.Cart;
import com.shangda.o2o.entity.Dish;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.CartMapper;
import com.shangda.o2o.mapper.DishMapper;
import com.shangda.o2o.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final DishMapper dishMapper;

    @Override
    public List<CartItemVO> listItems(Long userId) {
        return cartMapper.findItemsByUserId(userId);
    }

    @Override
    public void addItem(Long userId, Long dishId, Integer quantity) {
        Dish dish = dishMapper.findById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        if (dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BusinessException("菜品已下架");
        }
        Cart exist = cartMapper.findByUserAndDish(userId, dishId);
        if (exist != null) {
            // 已在购物车中则数量累加
            cartMapper.updateQuantity(exist.getId(), exist.getQuantity() + quantity);
            return;
        }
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setDishId(dishId);
        cart.setQuantity(quantity);
        try {
            cartMapper.insert(cart);
        } catch (DataIntegrityViolationException e) {
            // 并发加购触发唯一约束冲突时，重查记录并改为数量累加，保证幂等成功
            Cart concurrent = cartMapper.findByUserAndDish(userId, dishId);
            if (concurrent == null) {
                throw e;
            }
            cartMapper.updateQuantity(concurrent.getId(), concurrent.getQuantity() + quantity);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException("数量至少为 1");
        }
        checkOwnership(userId, cartId);
        cartMapper.updateQuantity(cartId, quantity);
    }

    @Override
    public void removeItem(Long userId, Long cartId) {
        checkOwnership(userId, cartId);
        cartMapper.deleteById(cartId);
    }

    @Override
    public void clear(Long userId) {
        cartMapper.deleteByUserId(userId);
    }

    /** 校验购物车记录存在且归属当前用户 */
    private void checkOwnership(Long userId, Long cartId) {
        Cart cart = cartMapper.findById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车记录不存在");
        }
    }
}
