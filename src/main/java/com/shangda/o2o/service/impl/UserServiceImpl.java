package com.shangda.o2o.service.impl;

import com.shangda.o2o.dto.LoginRequest;
import com.shangda.o2o.dto.PasswordChangeRequest;
import com.shangda.o2o.dto.RegisterRequest;
import com.shangda.o2o.entity.User;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.UserMapper;
import com.shangda.o2o.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户端账户服务实现（BCrypt 密码存储）
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /** 本地实例化，避免与管理端并行开发时的公共 Bean 冲突 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(RegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (userMapper.findByPhone(request.getPhone()) != null) {
            throw new BusinessException("手机号已被注册");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());
        userMapper.insert(user);
    }

    @Override
    public User login(LoginRequest request) {
        User user = userMapper.findByAccount(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }
        return user;
    }

    @Override
    public User getInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public void updateInfo(Long userId, String nickname, String phone) {
        if (!StringUtils.hasText(nickname) && !StringUtils.hasText(phone)) {
            throw new BusinessException("未提供任何修改内容");
        }
        if (StringUtils.hasText(phone)) {
            User exist = userMapper.findByPhone(phone);
            if (exist != null && !exist.getId().equals(userId)) {
                throw new BusinessException("手机号已被其他账号使用");
            }
        }
        userMapper.updateInfo(userId, nickname, phone);
    }

    @Override
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码不正确");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
    }
}
