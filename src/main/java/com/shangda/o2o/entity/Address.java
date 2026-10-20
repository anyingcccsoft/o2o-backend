package com.shangda.o2o.entity;

import lombok.Data;

/**
 * 收货地址实体（对应 address 表）
 */
@Data
public class Address {
    private Long id;
    private Long userId;
    private String consignee;
    private String phone;
    private String region;
    private String detail;
    /** 默认地址 1是/0否 */
    private Integer isDefault;
}
