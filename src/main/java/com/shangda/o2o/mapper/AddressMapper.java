package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收货地址 Mapper
 */
@Mapper
public interface AddressMapper {

    Address findById(@Param("id") Long id);

    /** 用户地址列表，默认地址优先 */
    List<Address> findByUserId(@Param("userId") Long userId);

    int insert(Address address);

    int update(Address address);

    int deleteById(@Param("id") Long id);

    /** 清除该用户所有默认标记（设置新默认前调用） */
    int clearDefault(@Param("userId") Long userId);

    int setDefault(@Param("id") Long id, @Param("userId") Long userId);
}
