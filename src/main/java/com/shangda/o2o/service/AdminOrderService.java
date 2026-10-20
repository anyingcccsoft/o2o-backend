package com.shangda.o2o.service;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.AdminOrderVO;

/**
 * 管理端订单服务
 */
public interface AdminOrderService {

    PageResult<AdminOrderVO> page(Integer status, int page, int pageSize);

    AdminOrderVO detail(Long id);

    /**
     * 订单状态流转：0→1→2→3 逐级推进，0/1/2→4 取消（取消回补库存并写 stock_log）
     */
    void updateStatus(Long id, Integer targetStatus, String operator);
}
