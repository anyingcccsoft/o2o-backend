package com.shangda.o2o.service.impl;

import com.shangda.o2o.dto.admin.EmployeeVO;
import com.shangda.o2o.entity.Employee;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.EmployeeMapper;
import com.shangda.o2o.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 管理端认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Employee login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("用户名和密码不能为空");
        }
        Employee employee = employeeMapper.findByUsername(username);
        if (employee == null || !passwordEncoder.matches(password, employee.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (employee.getStatus() == null || employee.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        return employee;
    }

    @Override
    public EmployeeVO getInfo(Long adminId) {
        Employee employee = employeeMapper.findById(adminId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        EmployeeVO vo = new EmployeeVO();
        vo.setId(employee.getId());
        vo.setUsername(employee.getUsername());
        vo.setRealName(employee.getRealName());
        vo.setPhone(employee.getPhone());
        vo.setStatus(employee.getStatus());
        return vo;
    }
}
