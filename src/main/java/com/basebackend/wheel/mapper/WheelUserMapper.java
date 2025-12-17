package com.basebackend.wheel.mapper;

import com.basebackend.wheel.entity.WheelUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 *
 * @author wheel-api
 * @since 2025-12-16
 */
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
