package com.basebackend.wheel.util;

import com.basebackend.wheel.entity.UserMembership;
import com.basebackend.wheel.enums.MembershipStatus;
import com.basebackend.wheel.enums.MembershipTier;
import com.basebackend.wheel.mapper.UserMembershipMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 会员权益检查工具类
 */
@Slf4j
@Component
public class MembershipPrivilegeHelper {

    @Autowired
    private UserMembershipMapper userMembershipMapper;

    /**
     * 获取用户当前有效会员等级
     *
     * @param userId 用户ID
     * @return 会员等级代码，0表示普通用户
     */
    public Integer getUserTier(Long userId) {
        if (userId == null) {
            return 0;
        }

        UserMembership membership = userMembershipMapper.selectActiveByUserId(userId);
        if (membership == null || membership.getEndTime().isBefore(LocalDateTime.now())) {
            return 0;
        }

        return membership.getTier();
    }

    /**
     * 检查用户是否为VIP及以上会员
     */
    public boolean isVipOrAbove(Long userId) {
        Integer tier = getUserTier(userId);
        return tier >= MembershipTier.VIP.getCode();
    }

    /**
     * 检查用户是否为SVIP会员
     */
    public boolean isSvip(Long userId) {
        Integer tier = getUserTier(userId);
        return tier >= MembershipTier.SVIP.getCode();
    }

    /**
     * 检查用户是否有无限转盘权益
     */
    public boolean hasUnlimitedSpins(Long userId) {
        return isVipOrAbove(userId);
    }

    /**
     * 检查用户是否有优先审核权益
     */
    public boolean hasPriorityAudit(Long userId) {
        return isVipOrAbove(userId);
    }

    /**
     * 检查用户是否有高级统计权益
     */
    public boolean hasAdvancedStatistics(Long userId) {
        return isVipOrAbove(userId);
    }

    /**
     * 检查用户是否有去广告权益
     */
    public boolean hasAdFree(Long userId) {
        return isVipOrAbove(userId);
    }

    /**
     * 检查用户是否有专属客服权益
     */
    public boolean hasDedicatedSupport(Long userId) {
        return isSvip(userId);
    }

    /**
     * 获取主题解锁等级要求
     * VIP(1) 可以使用VIP主题
     * SVIP(2) 可以使用VIP和SVIP主题
     *
     * @param userId 用户ID
     * @param themeRequiredTier 主题要求的等级
     * @return 是否可以使用该主题
     */
    public boolean canAccessTheme(Long userId, Integer themeRequiredTier) {
        if (themeRequiredTier == null || themeRequiredTier == 0) {
            return true;
        }
        Integer userTier = getUserTier(userId);
        return userTier >= themeRequiredTier;
    }

    /**
     * 获取内容审核优先级
     * SVIP: 优先级 3
     * VIP: 优先级 2
     * 普通用户: 优先级 1
     */
    public Integer getAuditPriority(Long userId) {
        Integer tier = getUserTier(userId);
        if (tier >= MembershipTier.SVIP.getCode()) {
            return 3;
        } else if (tier >= MembershipTier.VIP.getCode()) {
            return 2;
        }
        return 1;
    }
}
