package com.shangda.o2o.service.impl;

import com.shangda.o2o.common.PageResult;
import com.shangda.o2o.dto.admin.AdminOrderVO;
import com.shangda.o2o.entity.OrderItem;
import com.shangda.o2o.entity.StockLog;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.AdminOrderMapper;
import com.shangda.o2o.mapper.AdminStockLogMapper;
import com.shangda.o2o.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端订单服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    /** 订单状态：0待接单/1制作中/2配送中/3已完成/4已取消 */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_MAKING = 1;
    private static final int STATUS_DELIVERING = 2;
    private static final int STATUS_COMPLETED = 3;
    private static final int STATUS_CANCELLED = 4;

    private final AdminOrderMapper adminOrderMapper;
    private final AdminStockLogMapper adminStockLogMapper;

    @Override
    public PageResult<AdminOrderVO> page(Integer status, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        long total = adminOrderMapper.count(status);
        List<AdminOrderVO> records = adminOrderMapper.findPage(status, (safePage - 1) * safeSize, safeSize);
        return PageResult.of(total, safePage, safeSize, records);
    }

    @Override
    public AdminOrderVO detail(Long id) {
        AdminOrderVO order = adminOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setItems(adminOrderMapper.findItemsByOrderId(id));
        return order;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer targetStatus, String operator) {
        if (targetStatus == null || targetStatus < STATUS_MAKING || targetStatus > STATUS_CANCELLED) {
            throw new BusinessException("目标状态不合法");
        }
        AdminOrderVO order = adminOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        int current = order.getOrderStatus();

        if (targetStatus == STATUS_CANCELLED) {
            // 取消：仅未完成态（0/1/2）可取消，回补库存并写流水
            if (current != STATUS_PENDING && current != STATUS_MAKING && current != STATUS_DELIVERING) {
                throw new BusinessException("当前状态不允许取消");
            }
            List<OrderItem> items = adminOrderMapper.findItemsByOrderId(id);
            for (OrderItem item : items) {
                adminOrderMapper.restoreStock(item.getDishId(), item.getQuantity());
                StockLog stockLog = new StockLog();
                stockLog.setDishId(item.getDishId());
                stockLog.setChangeType(2);
                stockLog.setQuantityDelta(item.getQuantity());
                stockLog.setOrderNo(order.getOrderNo());
                stockLog.setOperator(operator);
                adminStockLogMapper.insert(stockLog);
            }
            adminOrderMapper.updateStatus(id, STATUS_CANCELLED, null);
            return;
        }

        // 推进：只允许 0→1→2→3 逐级
        if (targetStatus != current + 1) {
            throw new BusinessException("订单状态只能逐级流转");
        }
        // 接单前须已支付
        if (targetStatus == STATUS_MAKING && (order.getPayStatus() == null || order.getPayStatus() != 1)) {
            throw new BusinessException("订单未支付，不能接单");
        }
        LocalDateTime completedAt = targetStatus == STATUS_COMPLETED ? LocalDateTime.now() : null;
        adminOrderMapper.updateStatus(id, targetStatus, completedAt);
    }
}
