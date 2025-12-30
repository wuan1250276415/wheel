package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserOnlineStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserOnlineStatusMapper extends BaseMapper<UserOnlineStatus> {
    /**
     * 根据用户ID查询在线状态
     *
     * @param userId 用户ID
     * @return 在线状态
     */
    UserOnlineStatus selectByUserId(@Param("userId") Long userId);

    /**
     * 更新用户在线状态
     *
     * @param userId 用户ID
     * @param isOnline 是否在线
     * @param sessionId 会话ID
     * @return 影响行数
     */
    int updateOnlineStatus(@Param("userId") Long userId,
                          @Param("isOnline") Integer isOnline,
                          @Param("sessionId") String sessionId);
}
