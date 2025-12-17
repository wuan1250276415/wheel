package com.basebackend.wheel.service;

import java.time.LocalDateTime;

/**
 * 防作弊服务接口
 *
 * @author wheel-api
 * @since 2025-12-16
 */
public interface AntiCheatService {

    /**
     * 验证转盘频率
     *
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ValidationResult validateSpinFrequency(Long userId, String ipAddress, String deviceId);

    /**
     * 验证IP地址
     *
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 验证结果
     */
    ValidationResult validateIpAddress(Long userId, String ipAddress);

    /**
     * 检查设备异常
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ValidationResult checkDeviceAnomaly(Long userId, String deviceId);

    /**
     * 验证转盘结果一致性
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param spinTime 旋转时间
     * @param ipAddress IP地址
     * @return 验证结果
     */
    ValidationResult validateSpinResult(Long userId, Long contentId, LocalDateTime spinTime, String ipAddress);

    /**
     * 记录异常行为
     *
     * @param userId 用户ID
     * @param anomalyType 异常类型
     * @param details 异常详情
     * @return 是否成功记录
     */
    boolean recordAnomaly(Long userId, String anomalyType, String details);

    /**
     * 获取用户风险等级
     *
     * @param userId 用户ID
     * @return 风险等级（0-低风险，1-中风险，2-高风险）
     */
    int getUserRiskLevel(Long userId);

    /**
     * 验证结果
     */
    class ValidationResult {
        private boolean valid;
        private String message;
        private int riskLevel; // 0-正常，1-警告，2-危险
        private String details;

        public ValidationResult() {
        }

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public ValidationResult(boolean valid, String message, int riskLevel) {
            this.valid = valid;
            this.message = message;
            this.riskLevel = riskLevel;
        }

        // Getters and Setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(int riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, "验证通过");
        }

        public static ValidationResult success(String message) {
            return new ValidationResult(true, message);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message, 2);
        }

        public static ValidationResult warn(String message) {
            return new ValidationResult(true, message, 1);
        }
    }
}
