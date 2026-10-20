package com.shangda.o2o.service.impl;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.EmployeeRequest;
import com.shangda.o2o.dto.admin.EmployeeVO;
import com.shangda.o2o.entity.Employee;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.EmployeeMapper;
import com.shangda.o2o.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 员工管理服务实现
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public PageResult<EmployeeVO> page(int page, int pageSize) {
        long total = employeeMapper.count();
        List<EmployeeVO> records = employeeMapper.findPage((page - 1) * pageSize, pageSize);
        return PageResult.of(total, page, pageSize, records);
    }

    @Override
    public void create(EmployeeRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException("初始密码不能为空");
        }
        if (employeeMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        Employee employee = new Employee();
        employee.setUsername(request.getUsername());
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setRealName(request.getRealName());
        employee.setPhone(request.getPhone());
        employee.setStatus(1);
        employeeMapper.insert(employee);
    }

    @Override
    public void update(Long id, EmployeeRequest request) {
        Employee exist = employeeMapper.findById(id);
        if (exist == null) {
            throw new BusinessException("员工不存在");
        }
        Employee employee = new Employee();
        employee.setId(id);
        employee.setRealName(request.getRealName());
        employee.setPhone(request.getPhone());
        // 传入 password 时重置密码
        if (StringUtils.hasText(request.getPassword())) {
            employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        employeeMapper.update(employee);
    }

    @Override
    public void delete(Long id, Long currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw new BusinessException("不能删除当前登录账号");
        }
        if (employeeMapper.findById(id) == null) {
            throw new BusinessException("员工不存在");
        }
        employeeMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status, Long currentAdminId) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态参数不合法");
        }
        if (status == 0 && id.equals(currentAdminId)) {
            throw new BusinessException("不能禁用当前登录账号");
        }
        if (employeeMapper.findById(id) == null) {
            throw new BusinessException("员工不存在");
        }
        employeeMapper.updateStatus(id, status);
    }
}
