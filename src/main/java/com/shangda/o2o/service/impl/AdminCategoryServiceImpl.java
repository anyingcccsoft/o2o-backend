package com.shangda.o2o.service.impl;

import com.shangda.o2o.entity.Category;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.AdminCategoryMapper;
import com.shangda.o2o.mapper.AdminDishMapper;
import com.shangda.o2o.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理端分类服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final AdminCategoryMapper adminCategoryMapper;
    private final AdminDishMapper adminDishMapper;

    @Override
    public List<Category> listAll() {
        return adminCategoryMapper.findAll();
    }

    @Override
    public void create(Category category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        if (adminCategoryMapper.findByName(category.getName()) != null) {
            throw new BusinessException("分类名称已存在");
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        adminCategoryMapper.insert(category);
    }

    @Override
    public void update(Long id, Category category) {
        Category exist = adminCategoryMapper.findById(id);
        if (exist == null) {
            throw new BusinessException("分类不存在");
        }
        if (!StringUtils.hasText(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        // 名称唯一校验（排除自身）
        Category sameName = adminCategoryMapper.findByName(category.getName());
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new BusinessException("分类名称已存在");
        }
        category.setId(id);
        if (category.getSortOrder() == null) {
            category.setSortOrder(exist.getSortOrder());
        }
        if (category.getStatus() == null) {
            category.setStatus(exist.getStatus());
        }
        adminCategoryMapper.update(category);
    }

    @Override
    public void delete(Long id) {
        if (adminCategoryMapper.findById(id) == null) {
            throw new BusinessException("分类不存在");
        }
        if (adminDishMapper.countByCategoryId(id) > 0) {
            throw new BusinessException("该分类下存在菜品，无法删除");
        }
        adminCategoryMapper.deleteById(id);
    }
}
