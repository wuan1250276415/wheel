package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 黑名单检查结果
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistCheckResult {

    /**
     * 是否被封禁
     */
    private boolean blocked;

    /**
     * 黑名单记录ID
     */
    private Long blacklistId;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 过期时间（null表示永久封禁）
     */
    private LocalDateTime expireAt;

    /**
     * 是否可以申诉
     */
    private boolean canAppeal;

    /**
     * 封禁类型：1-用户 2-IP 3-设备
     */
    private Integer blockType;

    /**
     * 创建未封禁的结果
     */
    public static BlacklistCheckResult notBlocked() {
        return BlacklistCheckResult.builder()
                .blocked(false)
                .canAppeal(false)
                .build();
    }

    /**
     * 创建已封禁的结果
     */
    public static BlacklistCheckResult blocked(Long blacklistId, String reason, 
                                                LocalDateTime expireAt, boolean canAppeal, 
                                                Integer blockType) {
        return BlacklistCheckResult.builder()
                .blocked(true)
                .blacklistId(blacklistId)
                .reason(reason)
                .expireAt(expireAt)
                .canAppeal(canAppeal)
                .blockType(blockType)
                .build();
    }

    /**
     * 判断是否为永久封禁
     */
    public boolean isPermanent() {
        return blocked && expireAt == null;
    }
}
