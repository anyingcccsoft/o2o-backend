package com.shangda.o2o.service;

import com.shangda.o2o.dto.admin.EmployeeVO;
import com.shangda.o2o.entity.Employee;

/**
 * 管理端认证服务
 */
public interface AdminAuthService {

    /**
     * 员工登录：校验用户名/密码/启用状态，成功返回员工实体
     */
    Employee login(String username, String password);

    /**
     * 查询当前登录员工信息（不含密码）
     */
    EmployeeVO getInfo(Long adminId);
}
