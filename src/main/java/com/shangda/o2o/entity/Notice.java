package com.shangda.o2o.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体（对应 notice 表）
 */
@Data
public class Notice {
    private Long id;
    private String title;
    private String content;
    /** 状态 1发布/0下线 */
    private Integer status;
    private LocalDateTime createdAt;
}
