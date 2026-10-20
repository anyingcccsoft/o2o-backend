package com.shangda.o2o.controller.admin;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.dto.admin.EmployeeVO;
import com.shangda.o2o.entity.Employee;
import com.shangda.o2o.service.AdminAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端认证接口：登录 / 登出 / 当前员工信息
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @PostMapping("/login")
    public ApiResponse<EmployeeVO> login(@RequestBody LoginRequest request, HttpSession session) {
        Employee employee = adminAuthService.login(request.getUsername(), request.getPassword());
        session.setAttribute("adminId", employee.getId());
        return ApiResponse.success("登录成功", adminAuthService.getInfo(employee.getId()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.removeAttribute("adminId");
        session.invalidate();
        return ApiResponse.success("已退出登录", null);
    }

    @GetMapping("/info")
    public ApiResponse<EmployeeVO> info(HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        return ApiResponse.success(adminAuthService.getInfo(adminId));
    }
}
