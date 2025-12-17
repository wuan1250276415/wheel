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
import com.basebackend.wheel.mapper.WheelSpinRecordMapper;
import com.basebackend.wheel.service.AntiCheatService;
import com.basebackend.wheel.service.WheelSpinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 转盘服务实现
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional
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

    @Value("${wheel.wheel.max-daily-spins:50}")
    private int maxDailySpins;

    @Value("${wheel.wheel.min-spin-interval:1000}")
    private long minSpinInterval;

    private Random random = new Random();

    @Override
    public List<WheelCategory> getCategories() {
        return categoryMapper.selectEnabledCategoriesOrderBySort();
    }

    @Override
    public List<WheelContent> getContents(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            // 查询所有分类的内容
            List<WheelCategory> categories = getCategories();
            return categories.stream()
                    .map(cat -> contentMapper.selectByCategoryIdAndAuditStatus(cat.getId(), 1))
                    .flatMap(List::stream)
                    .toList();
        } else {
            return categoryIds.stream()
                    .map(categoryId -> contentMapper.selectByCategoryIdAndAuditStatus(categoryId, 1))
                    .flatMap(List::stream)
                    .toList();
        }
    }

    @Override
    public WheelSpinResultDTO spin(Long userId, WheelSpinDTO spinDTO) {
        // 1. 获取客户端信息（IP地址、设备ID）
        String ipAddress = "127.0.0.1"; // TODO: 从request中获取真实IP
        String deviceId = "device-" + userId; // TODO: 从request或本地存储获取设备ID

        // 2. 防作弊验证
        AntiCheatService.ValidationResult frequencyResult = antiCheatService.validateSpinFrequency(userId, ipAddress, deviceId);
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

        // 4. 使用转盘引擎进行权重随机选择
        LocalDateTime currentTime = LocalDateTime.now();
        WheelContent selectedContent = spinEngine.selectByWeight(availableContents, currentTime);

        // 5. 调整权重（根据时间敏感规则）
        selectedContent = adjustWeightByTime(selectedContent, currentTime);

        // 6. 计算旋转角度
        double rotationAngle = spinEngine.calculateRotationAngle(selectedContent, availableContents.size(), 0);

        // 7. 生成转盘种子（用于结果验证）
        long spinSeed = spinEngine.generateSpinSeed(userId, currentTime, spinDTO.getCategoryIds());

        // 8. 保存转盘记录
        WheelSpinRecord record = createSpinRecord(userId, selectedContent, spinDTO, ipAddress, deviceId, currentTime);
        spinRecordMapper.insert(record);

        // 9. 验证结果一致性
        boolean consistent = spinEngine.verifyResultConsistency(spinSeed, selectedContent, availableContents, currentTime);
        if (!consistent) {
            log.warn("转盘结果一致性验证失败: userId={}, contentId={}", userId, selectedContent.getId());
            // 记录异常但不阻止流程
            antiCheatService.recordAnomaly(userId, "RESULT_INCONSISTENCY",
                    "contentId=" + selectedContent.getId() + ",seed=" + spinSeed);
        }

        // 10. 构建返回结果
        WheelSpinResultDTO result = new WheelSpinResultDTO();
        result.setContentId(selectedContent.getId());
        result.setResultText(selectedContent.getContentText());
        result.setCategoryId(selectedContent.getCategoryId());
        result.setCategoryName(getCategoryName(selectedContent.getCategoryId()));
        result.setRotationAngle(rotationAngle);
        result.setSpinDuration(spinDTO.getAnimationDuration() != null ? spinDTO.getAnimationDuration().longValue() : 3000L);
        result.setIsWinning(spinEngine.isWinningResult(selectedContent));

        // 添加置信度信息
        double confidence = spinEngine.getResultConfidence(selectedContent, availableContents);
        log.info("用户转盘成功: userId={}, contentId={}, result={}, confidence={}, riskLevel={}",
                userId, selectedContent.getId(), selectedContent.getContentText(),
                confidence, ipResult.getRiskLevel());

        return result;
    }

    @Override
    public List<WheelSpinRecord> getHistory(Long userId, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return spinRecordMapper.selectByUserId(userId, offset, pageSize);
    }

    @Override
    public UserStats getStats(Long userId) {
        // TODO: 实现用户统计查询
        UserStats stats = new UserStats();
        stats.setTotalSpins(0L);
        stats.setTodaySpins(0L);
        stats.setWeekSpins(0L);
        stats.setMonthSpins(0L);
        stats.setFavoriteCategory("聊天话题");
        stats.setAvgSpinDuration(3000L);
        return stats;
    }

    @Override
    public boolean validateSpinFrequency(Long userId, String ipAddress) {
        // 委托给防作弊服务
        AntiCheatService.ValidationResult result = antiCheatService.validateSpinFrequency(userId, ipAddress, "device-" + userId);
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
                adjusted.setWeight(content.getWeight() * adjustment);
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
        record.setSpinDuration(spinDTO.getAnimationDuration() != null ? spinDTO.getAnimationDuration().longValue() : 3000L);
        record.setSpinTime(spinTime);
        record.setIpAddress(ipAddress);
        record.setDeviceId(deviceId);
        record.setClientType(1); // 微信小程序
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
