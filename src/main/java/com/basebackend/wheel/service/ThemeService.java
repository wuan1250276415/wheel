package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.ThemeVO;

import java.util.List;

/**
 * 主题服务接口
 */
public interface ThemeService {

    /**
     * 获取所有可用主题列表
     * @param userId 用户ID（用于标记拥有和使用状态）
     * @return 主题列表
     */
    List<ThemeVO> getAllThemes(Long userId);

    /**
     * 获取用户当前使用的主题
     * @param userId 用户ID
     * @return 当前主题
     */
    ThemeVO getCurrentTheme(Long userId);

    /**
     * 购买主题
     * @param userId 用户ID
     * @param themeId 主题ID
     */
    void purchaseTheme(Long userId, Long themeId);

    /**
     * 应用主题
     * @param userId 用户ID
     * @param themeId 主题ID
     */
    void applyTheme(Long userId, Long themeId);

    /**
     * 获取用户拥有的主题列表
     * @param userId 用户ID
     * @return 拥有的主题ID列表
     */
    List<Long> getOwnedThemeIds(Long userId);
}
