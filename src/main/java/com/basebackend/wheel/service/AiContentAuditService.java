package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.AiAuditResult;

import java.util.List;

/**
 * AI内容审核服务接口
 * 
 * 支持阿里云内容安全API和腾讯云天御API
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface AiContentAuditService {

    /**
     * 审核文本内容
     *
     * @param text 文本内容
     * @return 审核结果
     */
    AiAuditResult auditText(String text);

    /**
     * 审核图片内容
     *
     * @param imageUrl 图片URL
     * @return 审核结果
     */
    AiAuditResult auditImage(String imageUrl);

    /**
     * 批量审核文本
     *
     * @param texts 文本列表
     * @return 审核结果列表
     */
    List<AiAuditResult> batchAuditText(List<String> texts);

    /**
     * 批量审核图片
     *
     * @param imageUrls 图片URL列表
     * @return 审核结果列表
     */
    List<AiAuditResult> batchAuditImage(List<String> imageUrls);

    /**
     * 获取服务提供商名称
     *
     * @return 提供商名称
     */
    String getProviderName();

    /**
     * 检查服务是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取服务优先级（数值越小优先级越高）
     *
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
}
