package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.WheelUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WheelUserMapper extends BaseMapper<WheelUser> {
    /**
     * 根据手机号查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户信息
     */
    WheelUser selectByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int updateLastLoginTime(@Param("userId") Long userId);
}