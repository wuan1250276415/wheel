package com.basebackend.wheel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.Blacklist;

import java.util.List;
import java.util.Map;

/**
 * 黑名单管理服务接口
 * 
 * 支持用户黑名单、IP黑名单、设备黑名单的管理
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface BlacklistService {

    // ==================== 添加黑名单 ====================

    /**
     * 添加用户到黑名单
     *
     * @param entry 黑名单条目
     * @return 黑名单记录ID
     */
    Long addUserToBlacklist(BlacklistEntryDTO entry);

    /**
     * 添加IP到黑名单
     *
     * @param entry 黑名单条目
     * @return 黑名单记录ID
     */
    Long addIpToBlacklist(BlacklistEntryDTO entry);

    /**
     * 添加设备到黑名单
     *
     * @param entry 黑名单条目
     * @return 黑名单记录ID
     */
    Long addDeviceToBlacklist(BlacklistEntryDTO entry);

    /**
     * 通用添加黑名单方法
     *
     * @param entry 黑名单条目
     * @return 黑名单记录ID
     */
    Long addToBlacklist(BlacklistEntryDTO entry);

    // ==================== 检查黑名单 ====================

    /**
     * 检查用户是否在黑名单
     *
     * @param userId 用户ID
     * @return 检查结果
     */
    BlacklistCheckResult checkUser(Long userId);

    /**
     * 检查IP是否在黑名单
     *
     * @param ip IP地址
     * @return 检查结果
     */
    BlacklistCheckResult checkIp(String ip);

    /**
     * 检查设备是否在黑名单
     *
     * @param deviceId 设备ID
     * @return 检查结果
     */
    BlacklistCheckResult checkDevice(String deviceId);

    /**
     * 综合检查（用户+IP+设备）
     *
     * @param userId   用户ID（可为null）
     * @param ip       IP地址（可为null）
     * @param deviceId 设备ID（可为null）
     * @return 检查结果（返回第一个命中的黑名单）
     */
    BlacklistCheckResult checkAll(Long userId, String ip, String deviceId);

    // ==================== 解除黑名单 ====================

    /**
     * 解除黑名单
     *
     * @param blacklistId 黑名单记录ID
     * @param operatorId  操作人ID
     */
    void removeFromBlacklist(Long blacklistId, Long operatorId);

    /**
     * 解除用户黑名单
     *
     * @param userId     用户ID
     * @param operatorId 操作人ID
     */
    void removeUserFromBlacklist(Long userId, Long operatorId);

    /**
     * 解除IP黑名单
     *
     * @param ip         IP地址
     * @param operatorId 操作人ID
     */
    void removeIpFromBlacklist(String ip, Long operatorId);

    /**
     * 解除设备黑名单
     *
     * @param deviceId   设备ID
     * @param operatorId 操作人ID
     */
    void removeDeviceFromBlacklist(String deviceId, Long operatorId);

    // ==================== 申诉功能 ====================

    /**
     * 提交申诉
     *
     * @param appealDTO 申诉信息
     */
    void submitAppeal(BlacklistAppealDTO appealDTO);

    /**
     * 处理申诉
     *
     * @param handleDTO 处理信息
     */
    void handleAppeal(BlacklistAppealHandleDTO handleDTO);

    /**
     * 获取待处理的申诉列表
     *
     * @return 申诉列表
     */
    List<BlacklistVO> getPendingAppeals();

    // ==================== 查询功能 ====================

    /**
     * 根据ID获取黑名单记录
     *
     * @param blacklistId 黑名单ID
     * @return 黑名单记录
     */
    BlacklistVO getById(Long blacklistId);

    /**
     * 分页查询黑名单列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param type     类型（可为null）
     * @param status   状态（可为null）
     * @param keyword  关键词（可为null）
     * @return 分页结果
     */
    Page<BlacklistVO> getBlacklistPage(int page, int pageSize, Integer type, 
                                        Integer status, String keyword);

    // ==================== 过期处理 ====================

    /**
     * 处理过期的黑名单（自动解除）
     *
     * @return 处理数量
     */
    int releaseExpiredBlacklist();

    /**
     * 获取即将过期的黑名单列表（用于提醒）
     *
     * @param minutesBeforeExpire 过期前多少分钟
     * @return 即将过期的黑名单列表
     */
    List<Blacklist> getExpiringBlacklist(int minutesBeforeExpire);

    // ==================== 统计功能 ====================

    /**
     * 统计各类型黑名单数量
     *
     * @return 类型统计
     */
    Map<Integer, Long> countByType();

    /**
     * 获取黑名单统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getStatistics();
}
