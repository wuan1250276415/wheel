package com.basebackend.wheel.service.impl;

import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.WheelSpinDTO;
import com.basebackend.wheel.dto.WheelSpinResultDTO;
import com.basebackend.wheel.entity.WheelCategory;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.entity.WheelSpinRecord;
import com.basebackend.wheel.engine.TimeSensitiveFilter;
import com.basebackend.wheel.engine.WheelSpinEngine;
import com.basebackend.wheel.mapper.WheelCategoryMapper;
import com.basebackend.wheel.mapper.WheelContentMapper;
import com.basebackend.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.mapper.WheelSpinRecordMapper;
import com.basebackend.wheel.service.AntiCheatService;
import com.basebackend.wheel.service.StatisticsService;
import com.basebackend.wheel.service.WheelSpinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import com.basebackend.wheel.util.RequestUtils;
import com.basebackend.wheel.enums.ClientType;

/**
 * 转盘服务实现
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Service
public class WheelSpinServiceImpl implements WheelSpinService {

    @Autowired
    private WheelCategoryMapper categoryMapper;

    @Autowired
    private WheelContentMapper contentMapper;

    @Autowired
    private WheelSpinRecordMapper spinRecordMapper;

    @Autowired
    private WheelSpinEngine spinEngine;

    @Autowired
    private AntiCheatService antiCheatService;

    @Autowired
    private TimeSensitiveFilter timeFilter;

    @Autowired
    private StatisticsService statisticsService;

    @Value("${wheel.wheel.max-daily-spins:50}")
    private int maxDailySpins;

    @Value("${wheel.wheel.min-spin-interval:1000}")
    private long minSpinInterval;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public List<WheelCategory> getCategories() {
        return categoryMapper.selectEnabledCategoriesOrderBySort();
    }

    @Override
    public List<WheelContent> getContents(List<Long> categoryIds) {
        LambdaQueryWrapper<WheelContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WheelContent::getAuditStatus, 1)
                .eq(WheelContent::getStatus, 1);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            wrapper.in(WheelContent::getCategoryId, categoryIds);
        }
        wrapper.orderByDesc(WheelContent::getCreateTime);
        return contentMapper.selectList(wrapper);
    }

    @Override
    public WheelSpinResultDTO spin(Long userId, WheelSpinDTO spinDTO) {
        // 0. 如果未指定分类，使用所有启用的分类
        List<Long> categoryIds = spinDTO.getCategoryIds();
        if (categoryIds == null || categoryIds.isEmpty()) {
            List<WheelCategory> allCategories = getCategories();
            if (allCategories.isEmpty()) {
                throw new BusinessException("暂无可用分类");
            }
            categoryIds = allCategories.stream()
                    .map(WheelCategory::getId)
                    .toList();
            spinDTO.setCategoryIds(categoryIds);
        }

        // 1. 获取客户端信息（IP地址、设备ID）
        String ipAddress = RequestUtils.getClientIp();
        String deviceId = RequestUtils.getDeviceId();
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = "user-" + userId;
        }

        // 2. 防作弊验证
        AntiCheatService.ValidationResult frequencyResult = antiCheatService.validateSpinFrequency(userId, ipAddress,
                deviceId);
        if (!frequencyResult.isValid()) {
            throw new BusinessException(frequencyResult.getMessage());
        }

        AntiCheatService.ValidationResult ipResult = antiCheatService.validateIpAddress(userId, ipAddress);
        if (ipResult.getRiskLevel() >= 2) {
            throw new BusinessException("IP地址异常，请稍后再试");
        }

        AntiCheatService.ValidationResult deviceResult = antiCheatService.checkDeviceAnomaly(userId, deviceId);
        if (deviceResult.getRiskLevel() >= 2) {
            throw new BusinessException("设备异常，请稍后再试");
        }

        // 3. 获取可用内容
        List<WheelContent> availableContents = getContents(spinDTO.getCategoryIds());
        if (availableContents.isEmpty()) {
            throw new BusinessException("暂无可用内容");
        }

        // 4. 使用转盘引擎进行权重随机选择（带种子，确保可复现）
        LocalDateTime currentTime = LocalDateTime.now();
        long spinSeed = spinEngine.generateSpinSeed(userId, currentTime, spinDTO.getCategoryIds());
        WheelContent selectedContent = spinEngine.selectByWeight(availableContents, currentTime, spinSeed);

        // 5. 调整权重（根据时间敏感规则）
        selectedContent = adjustWeightByTime(selectedContent, currentTime);

        // 6. 计算旋转角度
        double rotationAngle = spinEngine.calculateRotationAngle(selectedContent, availableContents, 0);

        // 7. 保存转盘记录
        WheelSpinRecord record = createSpinRecord(userId, selectedContent, spinDTO, ipAddress, deviceId, currentTime);
        spinRecordMapper.insert(record);

        // 8. 验证结果一致性
        boolean consistent = spinEngine.verifyResultConsistency(spinSeed, selectedContent, availableContents,
                currentTime);
        if (!consistent) {
            log.warn("转盘结果一致性验证失败: userId={}, contentId={}", userId, selectedContent.getId());
            // 记录异常但不阻止流程
            antiCheatService.recordAnomaly(userId, "RESULT_INCONSISTENCY",
                    "contentId=" + selectedContent.getId() + ",seed=" + spinSeed);
        }

        // 9. 构建返回结果
        WheelSpinResultDTO result = new WheelSpinResultDTO();
        result.setContentId(selectedContent.getId());
        result.setResultText(selectedContent.getContentText());
        result.setCategoryId(selectedContent.getCategoryId());
        result.setCategoryName(getCategoryName(selectedContent.getCategoryId()));
        result.setRotationAngle(rotationAngle);
        result.setSpinDuration(
                spinDTO.getAnimationDuration() != null ? spinDTO.getAnimationDuration().longValue() : 3000L);
        result.setIsWinning(spinEngine.isWinningResult(selectedContent));

        // 添加置信度信息
        double confidence = spinEngine.getResultConfidence(selectedContent, availableContents);
        log.info("用户转盘成功: userId={}, contentId={}, result={}, confidence={}, riskLevel={}",
                userId, selectedContent.getId(), selectedContent.getContentText(),
                confidence, ipResult.getRiskLevel());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WheelSpinRecord> getHistory(Long userId, Integer pageNum, Integer pageSize) {
        Page<WheelSpinRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WheelSpinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WheelSpinRecord::getUserId, userId);
        wrapper.orderByDesc(WheelSpinRecord::getSpinTime);

        spinRecordMapper.selectPage(page, wrapper);

        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public UserStats getStats(Long userId) {
        StatisticsService.UserStatistics userStatistics = statisticsService.getUserStatistics(userId);
        UserStats stats = new UserStats();
        stats.setTotalSpins(getSafeLong(userStatistics != null ? userStatistics.getTotalSpins() : null));
        stats.setTodaySpins(getSafeLong(userStatistics != null ? userStatistics.getTodaySpins() : null));
        stats.setWeekSpins(getSafeLong(userStatistics != null ? userStatistics.getWeekSpins() : null));
        stats.setMonthSpins(getSafeLong(userStatistics != null ? userStatistics.getMonthSpins() : null));
        stats.setFavoriteCategory(userStatistics != null ? userStatistics.getFavoriteCategory() : null);
        stats.setAvgSpinDuration(getSafeLong(userStatistics != null ? userStatistics.getAvgSpinDuration() : null));
        return stats;
    }

    @Override
    public boolean validateSpinFrequency(Long userId, String ipAddress) {
        String deviceId = RequestUtils.getDeviceId();
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = "user-" + userId;
        }
        AntiCheatService.ValidationResult result = antiCheatService.validateSpinFrequency(userId, ipAddress, deviceId);
        return result.isValid();
    }

    /**
     * 根据时间规则调整内容权重
     */
    private WheelContent adjustWeightByTime(WheelContent content, LocalDateTime currentTime) {
        try {
            String categoryName = getCategoryName(content.getCategoryId());
            double adjustment = timeFilter.getCategoryWeightAdjustment(categoryName, currentTime);

            if (adjustment != 1.0) {
                // 创建副本并调整权重
                WheelContent adjusted = new WheelContent();
                adjusted.setId(content.getId());
                adjusted.setCategoryId(content.getCategoryId());
                adjusted.setContentText(content.getContentText());
                adjusted.setWeight(getSafeWeight(content) * adjustment);
                adjusted.setCreateUserId(content.getCreateUserId());
                adjusted.setAuditStatus(content.getAuditStatus());
                adjusted.setTags(content.getTags());
                adjusted.setIsSystem(content.getIsSystem());
                adjusted.setStatus(content.getStatus());

                log.debug("调整内容权重: contentId={}, originalWeight={}, adjustedWeight={}, adjustment={}",
                        content.getId(), content.getWeight(), adjusted.getWeight(), adjustment);

                return adjusted;
            }
        } catch (Exception e) {
            log.warn("调整权重失败，使用原始内容: contentId={}, error={}", content.getId(), e.getMessage());
        }

        return content;
    }

    private double getSafeWeight(WheelContent content) {
        return content != null && content.getWeight() != null ? content.getWeight() : 1.0;
    }

    private long getSafeLong(Long value) {
        return value != null ? value : 0L;
    }

    /**
     * 创建转盘记录
     */
    private WheelSpinRecord createSpinRecord(Long userId, WheelContent content,
            WheelSpinDTO spinDTO, String ipAddress,
            String deviceId, LocalDateTime spinTime) {
        WheelSpinRecord record = new WheelSpinRecord();
        record.setUserId(userId);
        record.setContentId(content.getId());
        record.setResultText(content.getContentText());
        record.setCategoryId(content.getCategoryId());
        record.setSpinDuration(
                spinDTO.getAnimationDuration() != null ? spinDTO.getAnimationDuration().longValue() : 3000L);
        record.setSpinTime(spinTime);
        record.setIpAddress(ipAddress);
        record.setDeviceId(deviceId);
        record.setClientType(ClientType.WECHAT_MINI.getCode());
        record.setIsAnomaly(0); // 默认正常
        record.setCreateBy(userId);

        return record;
    }

    /**
     * 获取分类名称
     */
    private String getCategoryName(Long categoryId) {
        WheelCategory category = categoryMapper.selectById(categoryId);
        return category != null ? category.getCategoryName() : "未知分类";
    }
}
