package com.shangda.o2o.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求
 */
@Data
public class OrderCreateRequest {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** 支付方式 1微信/2支付宝/3货到付款 */
    @NotNull(message = "支付方式不能为空")
    private Integer payMethod;

    private String remark;
}
