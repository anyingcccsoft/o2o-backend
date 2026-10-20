package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告表 Mapper（管理端专属，XML: mapper/AdminNoticeMapper.xml）
 */
@Mapper
public interface AdminNoticeMapper {

    List<Notice> findAll();

    Notice findById(@Param("id") Long id);

    int insert(Notice notice);

    int update(Notice notice);

    int deleteById(@Param("id") Long id);
}
