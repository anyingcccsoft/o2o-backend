package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜品 Mapper（用户端查询 + 下单库存原子扣减/回补）
 */
@Mapper
public interface DishMapper {

    Dish findById(@Param("id") Long id);

    /** 上架菜品分页查询（可选分类/关键词） */
    List<Dish> findOnSalePage(@Param("categoryId") Long categoryId,
                              @Param("keyword") String keyword,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long countOnSale(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    /** 热门菜品：按销量降序 top N */
    List<Dish> findHot(@Param("limit") int limit);

    /** 原子扣减库存并累加销量，库存不足时影响行数为 0 */
    int deductStock(@Param("id") Long id, @Param("qty") int qty);

    /** 取消订单回补库存并扣回销量 */
    int restoreStock(@Param("id") Long id, @Param("qty") int qty);
}
