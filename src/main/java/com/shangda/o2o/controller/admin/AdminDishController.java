package com.shangda.o2o.controller.admin;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.StockAdjustRequest;
import com.shangda.o2o.entity.Dish;
import com.shangda.o2o.service.AdminAuthService;
import com.shangda.o2o.service.AdminDishService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端菜品接口
 */
@RestController
@RequestMapping("/api/admin/dishes")
@RequiredArgsConstructor
public class AdminDishController {

    private final AdminDishService adminDishService;
    private final AdminAuthService adminAuthService;

    @Data
    public static class StatusRequest {
        private Integer status;
    }

    @GetMapping
    public ApiResponse<PageResult<Dish>> page(@RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminDishService.page(categoryId, keyword, page, pageSize));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Dish dish) {
        adminDishService.create(dish);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Dish dish) {
        adminDishService.update(id, dish);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminDishService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        adminDishService.updateStatus(id, request.getStatus());
        return ApiResponse.success("操作成功", null);
    }

    @PutMapping("/{id}/stock")
    public ApiResponse<Map<String, Integer>> adjustStock(@PathVariable Long id,
                                                         @RequestBody StockAdjustRequest request,
                                                         HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        String operator = adminAuthService.getInfo(adminId).getUsername();
        int stock = adminDishService.adjustStock(id, request, operator);
        return ApiResponse.success("库存调整成功", Map.of("stock", stock));
    }
}
