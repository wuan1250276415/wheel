package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.ThemeVO;
import com.basebackend.wheel.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 主题控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/theme")
@Tag(name = "转盘主题管理", description = "转盘主题相关接口")
public class ThemeController {

    @Autowired
    private ThemeService themeService;

    @Operation(summary = "获取所有主题", description = "获取所有可用的转盘主题列表")
    @GetMapping("/list")
    public Result<List<ThemeVO>> getAllThemes() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }
            List<ThemeVO> themes = themeService.getAllThemes(userId);
            return Result.success(themes);
        } catch (Exception e) {
            log.error("获取主题列表失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取主题列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取当前主题", description = "获取用户当前使用的主题")
    @GetMapping("/current")
    public Result<ThemeVO> getCurrentTheme() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }
            ThemeVO theme = themeService.getCurrentTheme(userId);
            return Result.success(theme);
        } catch (Exception e) {
            log.error("获取当前主题失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取当前主题失败: " + e.getMessage());
        }
    }

    @Operation(summary = "购买主题", description = "使用积分购买主题")
    @PostMapping("/purchase/{themeId}")
    public Result<String> purchaseTheme(@PathVariable Long themeId) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }
            themeService.purchaseTheme(userId, themeId);
            return Result.success(null, "购买成功");
        } catch (Exception e) {
            log.error("购买主题失败: userId={}, themeId={}, error={}",
                    UserContextHolder.getUserId(), themeId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @Operation(summary = "应用主题", description = "应用已拥有的主题")
    @PostMapping("/apply/{themeId}")
    public Result<String> applyTheme(@PathVariable Long themeId) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }
            themeService.applyTheme(userId, themeId);
            return Result.success(null, "应用成功");
        } catch (Exception e) {
            log.error("应用主题失败: userId={}, themeId={}, error={}",
                    UserContextHolder.getUserId(), themeId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @Operation(summary = "获取拥有的主题", description = "获取用户拥有的所有主题ID列表")
    @GetMapping("/owned")
    public Result<List<Long>> getOwnedThemes() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }
            List<Long> themeIds = themeService.getOwnedThemeIds(userId);
            return Result.success(themeIds);
        } catch (Exception e) {
            log.error("获取拥有主题失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取拥有主题失败: " + e.getMessage());
        }
    }
}
