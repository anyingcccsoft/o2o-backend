package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜品表 Mapper（管理端专属，XML: mapper/AdminDishMapper.xml）
 */
@Mapper
public interface AdminDishMapper {

    long count(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    List<Dish> findPage(@Param("categoryId") Long categoryId, @Param("keyword") String keyword,
                        @Param("offset") int offset, @Param("limit") int limit);

    Dish findById(@Param("id") Long id);

    long countByCategoryId(@Param("categoryId") Long categoryId);

    int insert(Dish dish);

    int update(Dish dish);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 原子增量调整库存，结果不允许为负（影响行数=0 表示会导致负库存，调用方应报错）
     */
    int adjustStock(@Param("id") Long id, @Param("delta") int delta);
}
