package com.shangda.o2o.service;

import com.shangda.o2o.entity.Address;

import java.util.List;

/**
 * 收货地址服务（所有操作均校验归属当前用户）
 */
public interface AddressService {

    List<Address> list(Long userId);

    void add(Long userId, Address address);

    void update(Long userId, Long addressId, Address address);

    void delete(Long userId, Long addressId);

    void setDefault(Long userId, Long addressId);
}
