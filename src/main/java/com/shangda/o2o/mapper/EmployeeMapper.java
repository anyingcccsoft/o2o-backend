package com.shangda.o2o.mapper;

import com.shangda.o2o.dto.admin.EmployeeVO;
import com.shangda.o2o.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工表 Mapper（管理端专属，XML: mapper/EmployeeMapper.xml）
 */
@Mapper
public interface EmployeeMapper {

    Employee findByUsername(@Param("username") String username);

    Employee findById(@Param("id") Long id);

    long count();

    List<EmployeeVO> findPage(@Param("offset") int offset, @Param("limit") int limit);

    int insert(Employee employee);

    int update(Employee employee);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
