package com.shangda.o2o.controller;

import com.shangda.o2o.common.ApiResponse;
import com.shangda.o2o.dto.LoginRequest;
import com.shangda.o2o.dto.PasswordChangeRequest;
import com.shangda.o2o.dto.RegisterRequest;
import com.shangda.o2o.entity.User;
import com.shangda.o2o.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户端账户接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success("注册成功", null);
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        User user = userService.login(request);
        session.setAttribute("userId", user.getId());
        user.setPasswordHash(null);
        return ApiResponse.success("登录成功", user);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.success("已退出登录", null);
    }

    @GetMapping("/info")
    public ApiResponse<User> info(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return ApiResponse.success(userService.getInfo(userId));
    }

    @PutMapping("/info")
    public ApiResponse<Void> updateInfo(@RequestBody Map<String, String> body, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        userService.updateInfo(userId, body.get("nickname"), body.get("phone"));
        return ApiResponse.success("修改成功", null);
    }

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request,
                                            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        userService.changePassword(userId, request);
        return ApiResponse.success("密码修改成功", null);
    }
}
