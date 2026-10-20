package com.shangda.o2o.service.impl;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.entity.Category;
import com.shangda.o2o.entity.Dish;
import com.shangda.o2o.entity.Notice;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.CategoryMapper;
import com.shangda.o2o.mapper.DishMapper;
import com.shangda.o2o.mapper.NoticeMapper;
import com.shangda.o2o.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户端浏览服务实现
 */
@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    /** 热门菜品数量 */
    private static final int HOT_LIMIT = 6;

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final NoticeMapper noticeMapper;

    @Override
    public List<Category> listEnabledCategories() {
        return categoryMapper.findEnabled();
    }

    @Override
    public PageResult<Dish> pageOnSaleDishes(Long categoryId, String keyword, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        long total = dishMapper.countOnSale(categoryId, keyword);
        List<Dish> records = dishMapper.findOnSalePage(categoryId, keyword,
                (safePage - 1) * safeSize, safeSize);
        return PageResult.of(total, safePage, safeSize, records);
    }

    @Override
    public List<Dish> listHotDishes() {
        return dishMapper.findHot(HOT_LIMIT);
    }

    @Override
    public Dish getDishDetail(Long id) {
        Dish dish = dishMapper.findById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        return dish;
    }

    @Override
    public List<Notice> listPublishedNotices() {
        return noticeMapper.findPublished();
    }
}
