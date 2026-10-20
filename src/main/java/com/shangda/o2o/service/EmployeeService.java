package com.shangda.o2o.service;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.EmployeeRequest;
import com.shangda.o2o.dto.admin.EmployeeVO;

/**
 * 员工管理服务
 */
public interface EmployeeService {

    PageResult<EmployeeVO> page(int page, int pageSize);

    void create(EmployeeRequest request);

    void update(Long id, EmployeeRequest request);

    void delete(Long id, Long currentAdminId);

    void updateStatus(Long id, Integer status, Long currentAdminId);
}
