package com.shangda.o2o.controller;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.entity.Category;
import com.shangda.o2o.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端分类接口
 */
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CatalogService catalogService;

    @GetMapping("/api/categories")
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(catalogService.listEnabledCategories());
    }
}
