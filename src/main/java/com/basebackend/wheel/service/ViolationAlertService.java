package com.basebackend.wheel.service;

/**
 * 违规预警服务接口
 * 
 * 负责监控违规内容数量并在异常增加时发送预警通知
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface ViolationAlertService {

    /**
     * 检查违规内容数量并发送预警
     * 
     * 比较当前时间段与历史平均值，如果超过阈值则发送预警
     */
    void checkAndAlert();

    /**
     * 发送违规预警通知给管理员
     *
     * @param alertType    预警类型（如：VIOLATION_SPIKE, HIGH_REJECTION_RATE）
     * @param currentCount 当前违规数量
     * @param threshold    阈值
     * @param message      预警消息
     */
    void sendAlertNotification(String alertType, long currentCount, long threshold, String message);

    /**
     * 获取指定时间段内的违规数量
     *
     * @param hours 小时数
     * @return 违规数量
     */
    long getViolationCountInHours(int hours);

    /**
     * 获取历史平均违规数量（每小时）
     *
     * @param days 统计天数
     * @return 平均每小时违规数量
     */
    double getAverageViolationPerHour(int days);
}
