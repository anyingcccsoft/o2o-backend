package com.shangda.o2o.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装（Mapper 手写 LIMIT/OFFSET 配合使用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总记录数 */
    private long total;
    /** 当前页码（从 1 开始） */
    private int page;
    /** 每页条数 */
    private int pageSize;
    /** 当前页数据 */
    private List<T> records;

    public static <T> PageResult<T> of(long total, int page, int pageSize, List<T> records) {
        return new PageResult<>(total, page, pageSize, records);
    }
}
