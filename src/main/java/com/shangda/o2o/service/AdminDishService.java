package com.shangda.o2o.service;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.StockAdjustRequest;
import com.shangda.o2o.entity.Dish;

/**
 * 管理端菜品服务
 */
public interface AdminDishService {

    PageResult<Dish> page(Long categoryId, String keyword, int page, int pageSize);

    void create(Dish dish);

    void update(Long id, Dish dish);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    /**
     * 库存调整（增量），写 stock_log change_type=3
     * @return 调整后的库存
     */
    int adjustStock(Long id, StockAdjustRequest request, String operator);
}
