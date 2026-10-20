package com.shangda.o2o.service.impl;

import com.shangda.o2o.entity.Notice;
import com.shangda.o2o.exception.BusinessException;
import com.shangda.o2o.mapper.AdminNoticeMapper;
import com.shangda.o2o.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理端公告服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminNoticeServiceImpl implements AdminNoticeService {

    private final AdminNoticeMapper adminNoticeMapper;

    @Override
    public List<Notice> listAll() {
        return adminNoticeMapper.findAll();
    }

    @Override
    public void create(Notice notice) {
        if (!StringUtils.hasText(notice.getTitle())) {
            throw new BusinessException("公告标题不能为空");
        }
        if (notice.getStatus() == null) {
            notice.setStatus(1);
        }
        adminNoticeMapper.insert(notice);
    }

    @Override
    public void update(Long id, Notice notice) {
        Notice exist = adminNoticeMapper.findById(id);
        if (exist == null) {
            throw new BusinessException("公告不存在");
        }
        if (!StringUtils.hasText(notice.getTitle())) {
            throw new BusinessException("公告标题不能为空");
        }
        notice.setId(id);
        if (notice.getStatus() == null) {
            notice.setStatus(exist.getStatus());
        }
        adminNoticeMapper.update(notice);
    }

    @Override
    public void delete(Long id) {
        if (adminNoticeMapper.findById(id) == null) {
            throw new BusinessException("公告不存在");
        }
        adminNoticeMapper.deleteById(id);
    }
}
