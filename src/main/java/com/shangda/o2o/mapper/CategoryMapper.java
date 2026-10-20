package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜品分类 Mapper（用户端只读）
 */
@Mapper
public interface CategoryMapper {

    /** 启用中的分类，按 sort_order 升序 */
    List<Category> findEnabled();
}
