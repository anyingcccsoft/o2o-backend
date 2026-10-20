package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水 Mapper（管理端专属，XML: mapper/AdminStockLogMapper.xml）
 */
@Mapper
public interface AdminStockLogMapper {

    int insert(StockLog stockLog);
}
