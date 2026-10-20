package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单 Mapper（用户端）
 */
@Mapper
public interface OrderMapper {

    int insert(Order order);

    Order findById(@Param("id") Long id);

    /** 当前用户订单分页，按创建时间倒序 */
    List<Order> findByUserIdPage(@Param("userId") Long userId,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    long countByUserId(@Param("userId") Long userId);

    int updatePayStatus(@Param("id") Long id, @Param("payStatus") int payStatus);

    int updateOrderStatus(@Param("id") Long id, @Param("orderStatus") int orderStatus);
}
