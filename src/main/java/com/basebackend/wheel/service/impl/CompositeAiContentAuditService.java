package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.config.AiAuditProperties;
import com.basebackend.wheel.dto.AiAuditResult;
import com.basebackend.wheel.enums.AuditDecision;
import com.basebackend.wheel.service.AiContentAuditService;
import com.basebackend.wheel.service.AuditConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 组合AI内容审核服务
 * 
 * 整合多个AI审核服务提供商，实现自动切换和降级
 * 支持基于阈值的审核决策逻辑
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
public class CompositeAiContentAuditService implements AiContentAuditService {

    private final AiAuditProperties aiAuditProperties;
    private final AuditConfigService auditConfigService;
    private final AiServiceFallbackHandler fallbackHandler;
    
    /**
     * 可用的AI审核服务列表（按优先级排序）
     */
    private List<AiContentAuditService> availableServices = new ArrayList<>();

    @Autowired(required = false)
    private AliyunContentAuditService aliyunService;

    @Autowired(required = false)
    private TencentContentAuditService tencentService;

    public CompositeAiContentAuditService(AiAuditProperties aiAuditProperties,
                                          AuditConfigService auditConfigService,
                                          AiServiceFallbackHandler fallbackHandler) {
        this.aiAuditProperties = aiAuditProperties;
        this.auditConfigService = auditConfigService;
        this.fallbackHandler = fallbackHandler;
    }

    @PostConstruct
    public void init() {
        // 收集所有可用的AI审核服务
        if (aliyunService != null && aliyunService.isAvailable()) {
            availableServices.add(aliyunService);
            log.info("阿里云内容审核服务已启用");
        }
        
        if (tencentService != null && tencentService.isAvailable()) {
            availableServices.add(tencentService);
            log.info("腾讯云内容审核服务已启用");
        }
        
        // 按优先级排序
        availableServices.sort(Comparator.comparingInt(AiContentAuditService::getPriority));
        
        log.info("组合AI审核服务初始化完成，可用服务数: {}", availableServices.size());
    }

    @Override
    public AiAuditResult auditText(String text) {
        if (!aiAuditProperties.isEnabled()) {
            log.debug("AI审核服务未启用，跳过审核");
            return AiAuditResult.pass(AiAuditResult.PROVIDER_LOCAL_FALLBACK, 0);
        }

        // 尝试使用可用的AI服务
        for (AiContentAuditService service : availableServices) {
            try {
                AiAuditResult result = service.auditText(text);
                
                // 应用阈值决策逻辑
                return applyThresholdDecision(result);
                
            } catch (Exception e) {
                log.warn("AI服务 {} 文本审核失败，尝试下一个服务: {}", 
                        service.getProviderName(), e.getMessage());
            }
        }
        
        // 所有AI服务都失败，使用降级处理
        log.warn("所有AI审核服务都不可用，降级为本地敏感词过滤");
        AiAuditResult fallbackResult = fallbackHandler.fallbackTextAudit(text, 
                new RuntimeException("所有AI审核服务不可用"));
        return applyThresholdDecision(fallbackResult);
    }

    @Override
    public AiAuditResult auditImage(String imageUrl) {
        if (!aiAuditProperties.isEnabled()) {
            log.debug("AI审核服务未启用，跳过审核");
            return AiAuditResult.pass(AiAuditResult.PROVIDER_LOCAL_FALLBACK, 0);
        }

        // 尝试使用可用的AI服务
        for (AiContentAuditService service : availableServices) {
            try {
                AiAuditResult result = service.auditImage(imageUrl);
                
                // 应用阈值决策逻辑
                return applyThresholdDecision(result);
                
            } catch (Exception e) {
                log.warn("AI服务 {} 图片审核失败，尝试下一个服务: {}", 
                        service.getProviderName(), e.getMessage());
            }
        }
        
        // 所有AI服务都失败，使用降级处理
        log.warn("所有AI审核服务都不可用，图片审核降级为人工复审");
        AiAuditResult fallbackResult = fallbackHandler.fallbackImageAudit(imageUrl, 
                new RuntimeException("所有AI审核服务不可用"));
        return applyThresholdDecision(fallbackResult);
    }

    @Override
    public List<AiAuditResult> batchAuditText(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }
        
        return texts.stream()
                .map(this::auditText)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiAuditResult> batchAuditImage(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return new ArrayList<>();
        }
        
        return imageUrls.stream()
                .map(this::auditImage)
                .collect(Collectors.toList());
    }

    @Override
    public String getProviderName() {
        return "COMPOSITE";
    }

    @Override
    public boolean isAvailable() {
        return aiAuditProperties.isEnabled() && 
               (!availableServices.isEmpty() || fallbackHandler.isFallbackAvailable());
    }

    @Override
    public int getPriority() {
        return 0; // 组合服务优先级最高
    }

    /**
     * 应用阈值决策逻辑
     * 
     * 根据配置的阈值重新计算审核决策
     *
     * @param result 原始审核结果
     * @return 应用阈值后的审核结果
     */
    private AiAuditResult applyThresholdDecision(AiAuditResult result) {
        if (result == null) {
            return AiAuditResult.review(AiAuditResult.PROVIDER_LOCAL_FALLBACK, 50.0, null, 0);
        }
        
        // 获取配置的阈值
        int passThreshold = auditConfigService.getAiPassThreshold();
        int rejectThreshold = auditConfigService.getAiRejectThreshold();
        
        double riskScore = result.getRiskScore();
        
        // 根据阈值重新计算决策
        AuditDecision newDecision = AuditDecision.fromRiskScore(riskScore, passThreshold, rejectThreshold);
        
        // 如果决策发生变化，更新结果
        if (newDecision != result.getDecision()) {
            log.debug("审核决策根据阈值调整: {} -> {}, 风险分: {}, 通过阈值: {}, 拒绝阈值: {}",
                    result.getDecision(), newDecision, riskScore, passThreshold, rejectThreshold);
            
            result.setDecision(newDecision);
            result.setPass(newDecision == AuditDecision.PASS);
        }
        
        return result;
    }

    /**
     * 获取当前主要服务提供商
     */
    public Optional<AiContentAuditService> getPrimaryService() {
        return availableServices.isEmpty() ? Optional.empty() : Optional.of(availableServices.get(0));
    }

    /**
     * 获取所有可用服务
     */
    public List<AiContentAuditService> getAvailableServices() {
        return new ArrayList<>(availableServices);
    }

    /**
     * 刷新服务列表
     */
    public void refreshServices() {
        availableServices.clear();
        
        if (aliyunService != null && aliyunService.isAvailable()) {
            availableServices.add(aliyunService);
        }
        
        if (tencentService != null && tencentService.isAvailable()) {
            availableServices.add(tencentService);
        }
        
        availableServices.sort(Comparator.comparingInt(AiContentAuditService::getPriority));
        
        log.info("AI审核服务列表已刷新，可用服务数: {}", availableServices.size());
    }
}
