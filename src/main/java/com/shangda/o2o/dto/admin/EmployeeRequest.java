package com.shangda.o2o.dto.admin;

import lombok.Data;

/**
 * 员工新增/编辑请求体（password 新增时为初始密码，编辑时可选用于重置密码）
 */
@Data
public class EmployeeRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
}
