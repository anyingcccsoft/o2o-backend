package com.shangda.o2o.controller.admin;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.AdminOrderVO;
import com.shangda.o2o.service.AdminAuthService;
import com.shangda.o2o.service.AdminOrderService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端订单接口：列表 / 详情 / 状态流转
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final AdminAuthService adminAuthService;

    @Data
    public static class StatusRequest {
        private Integer status;
    }

    @GetMapping
    public ApiResponse<PageResult<AdminOrderVO>> page(@RequestParam(required = false) Integer status,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminOrderService.page(status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminOrderVO> detail(@PathVariable Long id) {
        return ApiResponse.success(adminOrderService.detail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request,
                                          HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        String operator = adminAuthService.getInfo(adminId).getUsername();
        adminOrderService.updateStatus(id, request.getStatus(), operator);
        return ApiResponse.success("操作成功", null);
    }
}
