package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类表 Mapper（管理端专属，XML: mapper/AdminCategoryMapper.xml）
 */
@Mapper
public interface AdminCategoryMapper {

    List<Category> findAll();

    Category findById(@Param("id") Long id);

    Category findByName(@Param("name") String name);

    int insert(Category category);

    int update(Category category);

    int deleteById(@Param("id") Long id);
}
