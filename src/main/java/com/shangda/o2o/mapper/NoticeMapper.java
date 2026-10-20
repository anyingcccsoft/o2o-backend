package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 公告 Mapper（用户端只读）
 */
@Mapper
public interface NoticeMapper {

    /** 已发布公告，按创建时间倒序 */
    List<Notice> findPublished();
}
