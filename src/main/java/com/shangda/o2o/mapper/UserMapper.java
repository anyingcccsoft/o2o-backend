package com.shangda.o2o.mapper;

import com.shangda.o2o.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表 Mapper
 */
@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    User findByPhone(@Param("phone") String phone);

    /** 按用户名或手机号查询（登录用） */
    User findByAccount(@Param("account") String account);

    int insert(User user);

    /** 更新昵称/手机号 */
    int updateInfo(@Param("id") Long id, @Param("nickname") String nickname, @Param("phone") String phone);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);
}
