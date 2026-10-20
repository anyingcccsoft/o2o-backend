package com.shangda.o2o.service;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.entity.Category;
import com.shangda.o2o.entity.Dish;
import com.shangda.o2o.entity.Notice;

import java.util.List;

/**
 * 用户端浏览服务：分类 / 菜品 / 公告
 */
public interface CatalogService {

    List<Category> listEnabledCategories();

    PageResult<Dish> pageOnSaleDishes(Long categoryId, String keyword, int page, int pageSize);

    List<Dish> listHotDishes();

    Dish getDishDetail(Long id);

    List<Notice> listPublishedNotices();
}
