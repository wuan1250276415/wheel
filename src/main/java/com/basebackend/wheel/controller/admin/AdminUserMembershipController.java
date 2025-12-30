package com.basebackend.wheel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.common.model.Result;
import com.basebackend.security.annotation.RequiresPermission;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.MembershipPlan;
import com.basebackend.wheel.entity.UserMembership;
import com.basebackend.wheel.enums.MembershipStatus;
import com.basebackend.wheel.enums.MembershipTier;
import com.basebackend.wheel.mapper.MembershipPlanMapper;
import com.basebackend.wheel.mapper.UserMembershipMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/membership/users")
@Tag(name = "管理端-用户会员管理", description = "管理员管理用户会员")
public class AdminUserMembershipController {

    @Autowired
    private UserMembershipMapper userMembershipMapper;

    @Autowired
    private MembershipPlanMapper membershipPlanMapper;

    @Operation(summary = "分页查询用户会员列表")
    @GetMapping
    @RequiresPermission("membership:user:view")
    public Result<IPage<UserMembershipAdminVO>> queryUserMemberships(UserMembershipQueryDTO queryDTO) {
        try {
            Page<UserMembership> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

            LambdaQueryWrapper<UserMembership> wrapper = new LambdaQueryWrapper<>();

            if (queryDTO.getUserId() != null) {
                wrapper.eq(UserMembership::getUserId, queryDTO.getUserId());
            }

            if (queryDTO.getTier() != null) {
                wrapper.eq(UserMembership::getTier, queryDTO.getTier());
            }

            if (queryDTO.getStatus() != null) {
                wrapper.eq(UserMembership::getStatus, queryDTO.getStatus());
            }

            if (queryDTO.getStartTime() != null) {
                wrapper.ge(UserMembership::getCreateTime, queryDTO.getStartTime());
            }

            if (queryDTO.getEndTime() != null) {
                wrapper.le(UserMembership::getCreateTime, queryDTO.getEndTime());
            }

            wrapper.orderByDesc(UserMembership::getCreateTime);

            IPage<UserMembership> membershipPage = userMembershipMapper.selectPage(page, wrapper);

            IPage<UserMembershipAdminVO> result = membershipPage.convert(membership -> {
                UserMembershipAdminVO vo = new UserMembershipAdminVO();
                BeanUtils.copyProperties(membership, vo);

                vo.setTierName(MembershipTier.fromCode(membership.getTier()).getDescription());

                MembershipPlan plan = membershipPlanMapper.selectById(membership.getPlanId());
                if (plan != null) {
                    vo.setPlanName(plan.getPlanName());
                }

                long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), membership.getEndTime());
                vo.setRemainingDays(Math.max(0, remainingDays));

                return vo;
            });

            return Result.success(result);
        } catch (Exception e) {
            log.error("查询用户会员列表失败: error={}", e.getMessage(), e);
            return Result.error(500, "查询用户会员列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户会员历史")
    @GetMapping("/{userId}/history")
    @RequiresPermission("membership:user:view")
    public Result<List<UserMembershipAdminVO>> getUserMembershipHistory(@PathVariable Long userId) {
        try {
            LambdaQueryWrapper<UserMembership> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserMembership::getUserId, userId);
            wrapper.orderByDesc(UserMembership::getCreateTime);

            List<UserMembership> memberships = userMembershipMapper.selectList(wrapper);

            List<UserMembershipAdminVO> result = memberships.stream().map(membership -> {
                UserMembershipAdminVO vo = new UserMembershipAdminVO();
                BeanUtils.copyProperties(membership, vo);

                vo.setTierName(MembershipTier.fromCode(membership.getTier()).getDescription());

                MembershipPlan plan = membershipPlanMapper.selectById(membership.getPlanId());
                if (plan != null) {
                    vo.setPlanName(plan.getPlanName());
                }

                long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), membership.getEndTime());
                vo.setRemainingDays(Math.max(0, remainingDays));

                return vo;
            }).toList();

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户会员历史失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "获取用户会员历史失败: " + e.getMessage());
        }
    }

    @Operation(summary = "延期用户会员")
    @PostMapping("/{userId}/extend")
    @RequiresPermission("membership:user:extend")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> extendUserMembership(@PathVariable Long userId, @RequestBody UserMembershipExtendDTO extendDTO) {
        try {
            UserMembership membership = userMembershipMapper.selectActiveByUserId(userId);

            if (membership == null) {
                return Result.error(404, "未找到用户有效会员");
            }

            LocalDateTime newEndTime = membership.getEndTime().plusDays(extendDTO.getDays());
            membership.setEndTime(newEndTime);

            userMembershipMapper.updateById(membership);

            log.info("延期用户会员成功: userId={}, days={}, reason={}", userId, extendDTO.getDays(), extendDTO.getReason());
            return Result.success(null);
        } catch (Exception e) {
            log.error("延期用户会员失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "延期用户会员失败: " + e.getMessage());
        }
    }

    @Operation(summary = "升级用户会员")
    @PostMapping("/{userId}/upgrade")
    @RequiresPermission("membership:user:upgrade")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> upgradeUserMembership(@PathVariable Long userId, @RequestBody UserMembershipUpgradeDTO upgradeDTO) {
        try {
            UserMembership existingMembership = userMembershipMapper.selectActiveByUserId(userId);

            if (existingMembership != null) {
                existingMembership.setStatus(MembershipStatus.EXPIRED.getCode());
                userMembershipMapper.updateById(existingMembership);
            }

            UserMembership newMembership = new UserMembership();
            newMembership.setUserId(userId);
            newMembership.setTier(upgradeDTO.getTargetTier());
            newMembership.setStartTime(LocalDateTime.now());
            newMembership.setEndTime(LocalDateTime.now().plusDays(upgradeDTO.getDurationDays()));
            newMembership.setStatus(MembershipStatus.ACTIVE.getCode());
            newMembership.setAutoRenew(false);

            userMembershipMapper.insert(newMembership);

            log.info("升级用户会员成功: userId={}, targetTier={}, durationDays={}, reason={}",
                userId, upgradeDTO.getTargetTier(), upgradeDTO.getDurationDays(), upgradeDTO.getReason());
            return Result.success(null);
        } catch (Exception e) {
            log.error("升级用户会员失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "升级用户会员失败: " + e.getMessage());
        }
    }

    @Operation(summary = "修改自动续费状态")
    @PutMapping("/{userId}/auto-renew")
    @RequiresPermission("membership:user:update")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateAutoRenew(@PathVariable Long userId, @RequestParam Boolean autoRenew) {
        try {
            UserMembership membership = userMembershipMapper.selectActiveByUserId(userId);

            if (membership == null) {
                return Result.error(404, "未找到用户有效会员");
            }

            membership.setAutoRenew(autoRenew);
            userMembershipMapper.updateById(membership);

            log.info("修改自动续费状态成功: userId={}, autoRenew={}", userId, autoRenew);
            return Result.success(null);
        } catch (Exception e) {
            log.error("修改自动续费状态失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "修改自动续费状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "取消用户会员")
    @PostMapping("/{userId}/cancel")
    @RequiresPermission("membership:user:update")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelUserMembership(@PathVariable Long userId) {
        try {
            UserMembership membership = userMembershipMapper.selectActiveByUserId(userId);

            if (membership == null) {
                return Result.error(404, "未找到用户有效会员");
            }

            membership.setStatus(MembershipStatus.CANCELLED.getCode());
            membership.setEndTime(LocalDateTime.now());
            userMembershipMapper.updateById(membership);

            log.info("取消用户会员成功: userId={}", userId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("取消用户会员失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "取消用户会员失败: " + e.getMessage());
        }
    }
}
