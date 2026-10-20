package com.shangda.o2o.entity;

import lombok.Data;

/**
 * 员工实体（对应 employee 表，管理端账号）
 */
@Data
public class Employee {
    private Long id;
    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    /** 状态 1启用/0禁用 */
    private Integer status;
}
