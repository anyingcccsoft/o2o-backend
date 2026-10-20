package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水 Mapper
 */
@Mapper
public interface StockLogMapper {

    int insert(StockLog stockLog);
}
