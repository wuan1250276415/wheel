package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserMembership;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMembershipMapper extends BaseMapper<UserMembership> {

    UserMembership selectActiveByUserId(@Param("userId") Long userId);

    List<UserMembership> selectExpiringMemberships(@Param("beforeTime") LocalDateTime beforeTime);

    int updateStatusByUserId(@Param("userId") Long userId, @Param("status") Integer status);
}
