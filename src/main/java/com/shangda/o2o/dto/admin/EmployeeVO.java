package com.shangda.o2o.dto.admin;

import lombok.Data;

/**
 * 员工信息视图对象（不含密码，供管理端列表/详情返回）
 */
@Data
public class EmployeeVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    /** 状态 1启用/0禁用 */
    private Integer status;
}
