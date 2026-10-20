package com.shangda.o2o.service.impl;

import com.shangda.o2o.entity.Address;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.AddressMapper;
import com.shangda.o2o.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 收货地址服务实现（默认地址互斥更新）
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<Address> list(Long userId) {
        return addressMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public void add(Long userId, Address address) {
        validate(address);
        address.setId(null);
        address.setUserId(userId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        if (address.getIsDefault() == 1) {
            addressMapper.clearDefault(userId);
        }
        addressMapper.insert(address);
    }

    @Override
    @Transactional
    public void update(Long userId, Long addressId, Address address) {
        validate(address);
        checkOwnership(userId, addressId);
        address.setId(addressId);
        address.setUserId(userId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        if (address.getIsDefault() == 1) {
            addressMapper.clearDefault(userId);
        }
        addressMapper.update(address);
    }

    @Override
    public void delete(Long userId, Long addressId) {
        checkOwnership(userId, addressId);
        addressMapper.deleteById(addressId);
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        checkOwnership(userId, addressId);
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(addressId, userId);
    }

    private void validate(Address address) {
        if (address == null || !StringUtils.hasText(address.getConsignee())
                || !StringUtils.hasText(address.getPhone())
                || !StringUtils.hasText(address.getDetail())) {
            throw new BusinessException("收货人、电话与详细地址不能为空");
        }
    }

    /** 校验地址存在且归属当前用户 */
    private void checkOwnership(Long userId, Long addressId) {
        Address exist = addressMapper.findById(addressId);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
    }
}
