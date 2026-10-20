package com.shangda.o2o.mapper;

import com.shangda.o2o.dto.CartItemVO;
import com.shangda.o2o.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车 Mapper
 */
@Mapper
public interface CartMapper {

    Cart findById(@Param("id") Long id);

    Cart findByUserAndDish(@Param("userId") Long userId, @Param("dishId") Long dishId);

    /** 联查菜品信息的购物车列表 */
    List<CartItemVO> findItemsByUserId(@Param("userId") Long userId);

    /** 购物车原始记录（下单时使用） */
    List<Cart> findByUserId(@Param("userId") Long userId);

    int insert(Cart cart);

    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    int deleteById(@Param("id") Long id);

    int deleteByUserId(@Param("userId") Long userId);
}
