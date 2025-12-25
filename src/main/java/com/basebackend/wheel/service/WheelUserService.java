package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.UserSyncDTO;
import com.basebackend.wheel.entity.WheelUser;

/**
 * 用户同步服务
 *
 * @author wheel-api
 * @since 2025-12-16
 */
public interface WheelUserService {

    /**
     * 同步用户信息
     *
     * @param syncDTO 用户信息
     * @return 同步后的用户
     */
    WheelUser syncUser(UserSyncDTO syncDTO);
}
