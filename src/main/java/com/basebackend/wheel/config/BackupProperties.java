package com.basebackend.wheel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据库备份配置属性
 * 支持从 application.yml 加载配置，同时可以从数据库动态覆盖
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@Component
@ConfigurationProperties(prefix = "wheel.backup")
public class BackupProperties {

    /**
     * 是否启用备份功能
     */
    private boolean enabled = true;

    /**
     * 每日备份 cron 表达式
     */
    private String dailyCron = "0 0 2 * * ?";

    /**
     * 每周备份 cron 表达式
     */
    private String weeklyCron = "0 0 3 ? * SUN";

    /**
     * 清理任务 cron 表达式
     */
    private String cleanupCron = "0 0 4 * * ?";

    /**
     * 健康检查 cron 表达式
     */
    private String healthCheckCron = "0 0 * * * ?";

    /**
     * 备份保留天数
     */
    private int retentionDays = 30;

    /**
     * 最少保留备份数
     */
    private int minBackupCount = 7;

    /**
     * 备份存储路径
     */
    private String storagePath = "/data/backups";

    /**
     * 是否启用加密
     */
    private boolean encryptionEnabled = true;

    /**
     * 加密密钥（建议从环境变量读取）
     */
    private String encryptionKey;

    /**
     * 无备份告警阈值（小时）
     */
    private int alertThresholdHours = 48;

    /**
     * 存储告警阈值（GB）
     */
    private int storageWarningThresholdGb = 50;

    /**
     * MySQL配置
     */
    private MysqlConfig mysql = new MysqlConfig();

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * MySQL 配置
     */
    @Data
    public static class MysqlConfig {
        /**
         * mysqldump 命令路径
         */
        private String mysqldumpPath = "mysqldump";

        /**
         * mysql 命令路径
         */
        private String mysqlPath = "mysql";

        /**
         * 数据库主机
         */
        private String host = "localhost";

        /**
         * 数据库端口
         */
        private int port = 3306;

        /**
         * 数据库名称
         */
        private String database;

        /**
         * 数据库用户名
         */
        private String username;

        /**
         * 数据库密码
         */
        private String password;

        /**
         * 额外的 mysqldump 参数
         */
        private String extraArgs = "--single-transaction --quick --lock-tables=false";
    }

    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 最大重试次数
         */
        private int maxRetries = 3;

        /**
         * 初始重试延迟（毫秒）
         */
        private long initialDelayMs = 1000;

        /**
         * 重试延迟倍数（指数退避）
         */
        private double multiplier = 2.0;

        /**
         * 最大重试延迟（毫秒）
         */
        private long maxDelayMs = 30000;
    }
}
