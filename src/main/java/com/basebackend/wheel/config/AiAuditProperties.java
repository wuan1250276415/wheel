package com.basebackend.wheel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI内容审核配置属性
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "wheel.ai-audit")
public class AiAuditProperties {

    /**
     * 是否启用AI审核
     */
    private boolean enabled = false;

    /**
     * 主服务提供商：aliyun 或 tencent
     */
    private String primaryProvider = "aliyun";

    /**
     * 阿里云配置
     */
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * 腾讯云配置
     */
    private TencentConfig tencent = new TencentConfig();

    /**
     * 超时配置（毫秒）
     */
    private int connectTimeout = 3000;
    private int readTimeout = 6000;

    /**
     * 重试配置
     */
    private int maxRetries = 2;
    private int retryDelayMs = 500;

    /**
     * 阿里云内容安全配置
     */
    @Data
    public static class AliyunConfig {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * AccessKey ID
         */
        private String accessKeyId;

        /**
         * AccessKey Secret
         */
        private String accessKeySecret;

        /**
         * 区域ID
         */
        private String regionId = "cn-shanghai";

        /**
         * 端点
         */
        private String endpoint = "green-cip.cn-shanghai.aliyuncs.com";

        /**
         * 文本审核场景
         */
        private String textScenes = "antispam";

        /**
         * 图片审核场景
         */
        private String imageScenes = "porn,terrorism,ad";
    }

    /**
     * 腾讯云天御配置
     */
    @Data
    public static class TencentConfig {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * SecretId
         */
        private String secretId;

        /**
         * SecretKey
         */
        private String secretKey;

        /**
         * 区域
         */
        private String region = "ap-guangzhou";

        /**
         * 端点
         */
        private String endpoint = "tms.tencentcloudapi.com";

        /**
         * 图片审核端点
         */
        private String imageEndpoint = "ims.tencentcloudapi.com";
    }
}
