package com.shangda.o2o.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体（对应 user 表）
 */
@Data
public class User {
    private Long id;
    private String username;
    private String phone;
    private String passwordHash;
    private String nickname;
    private LocalDateTime createdAt;
}
