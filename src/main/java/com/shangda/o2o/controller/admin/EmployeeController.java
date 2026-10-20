package com.shangda.o2o.controller.admin;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.EmployeeRequest;
import com.shangda.o2o.dto.admin.EmployeeVO;
import com.shangda.o2o.service.EmployeeService;
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

/**
 * 员工管理接口
 */
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Data
    public static class StatusRequest {
        private Integer status;
    }

    @GetMapping
    public ApiResponse<PageResult<EmployeeVO>> page(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(employeeService.page(page, pageSize));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody EmployeeRequest request) {
        employeeService.create(request);
        return ApiResponse.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        employeeService.update(id, request);
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        employeeService.delete(id, adminId);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request,
                                          HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        employeeService.updateStatus(id, request.getStatus(), adminId);
        return ApiResponse.success("操作成功", null);
    }
}
