package com.shangda.o2o.service;

import com.shangda.o2o.dto.LoginRequest;
import com.shangda.o2o.dto.PasswordChangeRequest;
import com.shangda.o2o.dto.RegisterRequest;
import com.shangda.o2o.entity.User;

/**
 * 用户端账户服务
 */
public interface UserService {

    void register(RegisterRequest request);

    /** 登录成功返回用户实体（含 id 供写入 session） */
    User login(LoginRequest request);

    /** 查询用户信息（不含密码） */
    User getInfo(Long userId);

    void updateInfo(Long userId, String nickname, String phone);

    void changePassword(Long userId, PasswordChangeRequest request);
}
