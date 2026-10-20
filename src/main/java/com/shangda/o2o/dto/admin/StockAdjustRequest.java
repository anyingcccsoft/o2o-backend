package com.shangda.o2o.dto.admin;

import lombok.Data;

/**
 * 库存调整请求体：quantityDelta 与 targetStock 二选一，最终统一按增量执行
 */
@Data
public class StockAdjustRequest {
    /** 增量调整（正数入库、负数出库） */
    private Integer quantityDelta;
    /** 目标库存（服务端换算为增量） */
    private Integer targetStock;
}
