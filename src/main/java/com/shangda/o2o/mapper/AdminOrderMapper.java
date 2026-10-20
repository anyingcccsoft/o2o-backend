package com.shangda.o2o.mapper;

import com.shangda.o2o.dto.admin.AdminOrderVO;
import com.shangda.o2o.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单表 Mapper（管理端专属，XML: mapper/AdminOrderMapper.xml）
 */
@Mapper
public interface AdminOrderMapper {

    long count(@Param("status") Integer status);

    List<AdminOrderVO> findPage(@Param("status") Integer status,
                                @Param("offset") int offset, @Param("limit") int limit);

    AdminOrderVO findById(@Param("id") Long id);

    List<OrderItem> findItemsByOrderId(@Param("orderId") Long orderId);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("completedAt") LocalDateTime completedAt);

    /**
     * 取消订单回补库存（正增量）
     */
    int restoreStock(@Param("dishId") Long dishId, @Param("quantity") int quantity);
}
