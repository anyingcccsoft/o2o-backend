package com.shangda.o2o.service.impl;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.StockAdjustRequest;
import com.shangda.o2o.entity.Dish;
import com.shangda.o2o.entity.StockLog;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.AdminCategoryMapper;
import com.shangda.o2o.mapper.AdminDishMapper;
import com.shangda.o2o.mapper.AdminStockLogMapper;
import com.shangda.o2o.service.AdminDishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端菜品服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminDishServiceImpl implements AdminDishService {

    private final AdminDishMapper adminDishMapper;
    private final AdminCategoryMapper adminCategoryMapper;
    private final AdminStockLogMapper adminStockLogMapper;

    @Override
    public PageResult<Dish> page(Long categoryId, String keyword, int page, int pageSize) {
        long total = adminDishMapper.count(categoryId, keyword);
        List<Dish> records = adminDishMapper.findPage(categoryId, keyword, (page - 1) * pageSize, pageSize);
        return PageResult.of(total, page, pageSize, records);
    }

    @Override
    public void create(Dish dish) {
        validate(dish);
        if (dish.getStock() == null) {
            dish.setStock(0);
        }
        if (dish.getStatus() == null) {
            dish.setStatus(1);
        }
        if (dish.getSales() == null) {
            dish.setSales(0);
        }
        adminDishMapper.insert(dish);
    }

    @Override
    public void update(Long id, Dish dish) {
        if (adminDishMapper.findById(id) == null) {
            throw new BusinessException("菜品不存在");
        }
        validate(dish);
        dish.setId(id);
        adminDishMapper.update(dish);
    }

    @Override
    public void delete(Long id) {
        if (adminDishMapper.findById(id) == null) {
            throw new BusinessException("菜品不存在");
        }
        adminDishMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态参数不合法");
        }
        if (adminDishMapper.findById(id) == null) {
            throw new BusinessException("菜品不存在");
        }
        adminDishMapper.updateStatus(id, status);
    }

    @Override
    @Transactional
    public int adjustStock(Long id, StockAdjustRequest request, String operator) {
        Dish dish = adminDishMapper.findById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        // quantityDelta 优先，targetStock 换算为增量
        Integer delta;
        if (request.getQuantityDelta() != null) {
            delta = request.getQuantityDelta();
        } else if (request.getTargetStock() != null) {
            if (request.getTargetStock() < 0) {
                throw new BusinessException("目标库存不能为负数");
            }
            delta = request.getTargetStock() - dish.getStock();
        } else {
            throw new BusinessException("请传入 quantityDelta 或 targetStock");
        }
        if (delta == 0) {
            return dish.getStock();
        }
        // 原子调整，结果不允许为负
        int affected = adminDishMapper.adjustStock(id, delta);
        if (affected == 0) {
            throw new BusinessException("调整后库存不能为负数，当前库存：" + dish.getStock());
        }
        StockLog stockLog = new StockLog();
        stockLog.setDishId(id);
        stockLog.setChangeType(3);
        stockLog.setQuantityDelta(delta);
        stockLog.setOperator(operator);
        adminStockLogMapper.insert(stockLog);
        return adminDishMapper.findById(id).getStock();
    }

    /** 新增/编辑公共校验 */
    private void validate(Dish dish) {
        if (!StringUtils.hasText(dish.getName())) {
            throw new BusinessException("菜品名称不能为空");
        }
        if (dish.getCategoryId() == null) {
            throw new BusinessException("请选择菜品分类");
        }
        if (adminCategoryMapper.findById(dish.getCategoryId()) == null) {
            throw new BusinessException("所选分类不存在");
        }
        if (dish.getPrice() == null || dish.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("菜品价格不合法");
        }
    }
}
