package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.AchievementVO;
import com.basebackend.wheel.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/achievements")
@Tag(name = "成就系统", description = "成就系统相关接口")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @Operation(summary = "获取用户成就列表", description = "获取当前用户的所有成就及解锁状态")
    @GetMapping("/list")
    public Result<List<AchievementVO>> getUserAchievements() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return Result.error(401, "未登录,请先登录");
            }
            List<AchievementVO> achievements = achievementService.getUserAchievements(userId);
            return Result.success(achievements);
        } catch (Exception e) {
            log.error("获取成就列表失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
}
