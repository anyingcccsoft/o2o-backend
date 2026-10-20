package com.shangda.o2o.service;

import com.shangda.o2o.entity.Notice;

import java.util.List;

/**
 * 管理端公告服务
 */
public interface AdminNoticeService {

    List<Notice> listAll();

    void create(Notice notice);

    void update(Long id, Notice notice);

    void delete(Long id);
}
