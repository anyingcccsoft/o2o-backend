package com.shangda.o2o.service;

import com.shangda.o2o.entity.Category;

import java.util.List;

/**
 * 管理端分类服务
 */
public interface AdminCategoryService {

    List<Category> listAll();

    void create(Category category);

    void update(Long id, Category category);

    void delete(Long id);
}
