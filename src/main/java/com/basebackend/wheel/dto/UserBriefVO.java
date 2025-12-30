package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户简要信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBriefVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatarUrl;
}
