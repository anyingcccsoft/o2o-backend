package com.shangda.o2o.entity;

import lombok.Data;

/**
 * 菜品分类实体（对应 category 表）
 */
@Data
public class Category {
    private Long id;
    private String name;
    private Integer sortOrder;
    /** 状态 1启用/0禁用 */
    private Integer status;
}
