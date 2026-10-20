package com.shangda.o2o.controller;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.entity.Dish;
import com.shangda.o2o.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端菜品接口
 */
@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {

    private final CatalogService catalogService;

    @GetMapping
    public ApiResponse<PageResult<Dish>> page(@RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(catalogService.pageOnSaleDishes(categoryId, keyword, page, pageSize));
    }

    @GetMapping("/hot")
    public ApiResponse<List<Dish>> hot() {
        return ApiResponse.success(catalogService.listHotDishes());
    }

    @GetMapping("/{id}")
    public ApiResponse<Dish> detail(@PathVariable Long id) {
        return ApiResponse.success(catalogService.getDishDetail(id));
    }
}
