package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.wheel.dto.WheelSpinDTO;
import com.basebackend.wheel.dto.WheelSpinResultDTO;
import com.basebackend.wheel.entity.WheelCategory;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.entity.WheelSpinRecord;
import com.basebackend.wheel.service.WheelSpinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 转盘控制器
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/wheel")
@Tag(name = "转盘管理", description = "转盘相关接口")
public class WheelController {

    @Autowired
    private WheelSpinService wheelSpinService;

    @Operation(summary = "获取转盘分类", description = "获取所有可用的转盘分类列表")
    @GetMapping("/categories")
    public ResponseEntity<List<WheelCategory>> getCategories() {
        try {
            List<WheelCategory> categories = wheelSpinService.getCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("获取转盘分类失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取转盘内容", description = "根据分类ID获取转盘内容（为空则获取所有分类的内容）")
    @GetMapping("/contents")
    public ResponseEntity<List<WheelContent>> getContents(
            @RequestParam(required = false) List<Long> categoryIds) {
        try {
            List<WheelContent> contents = wheelSpinService.getContents(categoryIds);
            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            log.error("获取转盘内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "执行转盘", description = "根据参数执行转盘，返回随机选中的内容")
    @PostMapping("/spin")
    public ResponseEntity<WheelSpinResultDTO> spin(
            @Valid @RequestBody WheelSpinDTO spinDTO) {
        try {
            Long userId = UserContextHolder.getUserId();
            WheelSpinResultDTO result = wheelSpinService.spin(userId, spinDTO);
            log.info("转盘成功: userId={}", userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("转盘失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取转盘历史", description = "获取当前用户的转盘历史记录")
    @GetMapping("/history")
    public ResponseEntity<List<WheelSpinRecord>> getHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            List<WheelSpinRecord> history = wheelSpinService.getHistory(userId, pageNum, pageSize);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取转盘历史失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取用户统计", description = "获取当前用户的使用统计信息")
    @GetMapping("/stats")
    public ResponseEntity<WheelSpinService.UserStats> getStats(HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            WheelSpinService.UserStats stats = wheelSpinService.getStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取用户统计失败: error={}", e.getMessage());
            throw e;
        }
    }
}
