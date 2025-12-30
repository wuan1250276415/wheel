package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.AdVO;
import com.basebackend.wheel.enums.ImpressionType;
import com.basebackend.wheel.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告控制器
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Slf4j
@RestController
@RequestMapping("/api/ads")
@Tag(name = "广告管理", description = "广告相关接口")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    @Operation(summary = "获取广告", description = "获取指定广告位的广告列表（VIP用户自动返回空列表）")
    @GetMapping("/{placementKey}")
    public Result<List<AdVO>> getAds(@PathVariable String placementKey) {
        try {
            Long userId = null;
            try {
                userId = UserContextHolder.getUserId();
            } catch (Exception e) {
                // 未登录用户，userId为null
            }

            List<AdVO> ads = advertisementService.getAdsForPlacement(userId, placementKey);
            return Result.success(ads);
        } catch (Exception e) {
            log.error("获取广告失败: placementKey={}, error={}", placementKey, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "记录广告展示", description = "记录广告的展示或点击行为")
    @PostMapping("/impression")
    public Result<Void> recordImpression(
            @RequestParam Long adId,
            @RequestParam String placementKey,
            @RequestParam ImpressionType type) {
        try {
            Long userId = null;
            try {
                userId = UserContextHolder.getUserId();
            } catch (Exception e) {
                // 未登录用户，userId为null
            }

            advertisementService.recordImpression(adId, userId, placementKey, type);
            return Result.success();
        } catch (Exception e) {
            log.error("记录广告展示失败: adId={}, type={}, error={}",
                    adId, type, e.getMessage(), e);
            throw e;
        }
    }
}
