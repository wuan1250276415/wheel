package com.basebackend.wheel.controller;

import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.UserSyncDTO;
import com.basebackend.wheel.service.WheelUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户同步回调
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户同步", description = "同步登录用户信息")
public class UserSyncController {

    @Autowired
    private WheelUserService wheelUserService;

    @Operation(summary = "同步用户信息", description = "登录后同步用户信息到 wheel_user")
    @PostMapping("/sync-user")
    public Result<Void> syncUser(@Valid @RequestBody UserSyncDTO syncDTO) {
        wheelUserService.syncUser(syncDTO);
        log.info("用户信息同步完成: userId={}", syncDTO.getUserId());
        return Result.success();
    }
}
