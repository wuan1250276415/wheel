package com.basebackend.wheel.service.impl;

import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.CoupleInviteDTO;
import com.basebackend.wheel.entity.CoupleRelationship;
import com.basebackend.wheel.entity.WheelUser;
import com.basebackend.wheel.mapper.CoupleRelationshipMapper;
import com.basebackend.wheel.mapper.WheelUserMapper;
import com.basebackend.wheel.service.CoupleService;
import com.basebackend.wheel.util.AuditHelper;
import com.basebackend.wheel.entity.WheelSpinRecord;
import com.basebackend.wheel.mapper.WheelSpinRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 情侣服务实现
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional
public class CoupleServiceImpl implements CoupleService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final int INVITE_CODE_MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private CoupleRelationshipMapper coupleMapper;

    @Autowired
    private WheelUserMapper userMapper;

    @Autowired
    private WheelSpinRecordMapper spinRecordMapper;

    @Override
    public InviteResult inviteCouple(Long userId, CoupleInviteDTO inviteDTO) {
        // 1. 检查用户是否已有情侣关系
        CoupleRelationship existingRelationship = coupleMapper.selectConfirmedByUserId(userId);
        if (existingRelationship != null) {
            throw new BusinessException("您已有情侣关系，无法发起新邀请");
        }

        // 2. 检查对方手机号是否存在
        WheelUser partner = userMapper.selectByPhoneNumber(inviteDTO.getPartnerPhoneNumber());
        if (partner == null) {
            throw new BusinessException("对方手机号未注册");
        }

        // 3. 检查不能邀请自己
        if (partner.getId().equals(userId)) {
            throw new BusinessException("不能邀请自己");
        }

        // 4. 检查对方是否已有情侣关系
        CoupleRelationship partnerRelationship = coupleMapper.selectConfirmedByUserId(partner.getId());
        if (partnerRelationship != null) {
            throw new BusinessException("对方已有情侣关系");
        }

        // 5. 检查是否已经邀请过对方（存在待确认的关系）
        List<CoupleRelationship> pendingRelationships = coupleMapper.selectByUserId(userId);
        boolean alreadyInvited = pendingRelationships.stream()
                .anyMatch(rel -> rel.getUserId2().equals(partner.getId()) && rel.getStatus() == 0);
        if (alreadyInvited) {
            throw new BusinessException("您已经邀请过对方，请等待对方确认");
        }

        // 6. 创建新的情侣关系
        CoupleRelationship relationship = new CoupleRelationship();
        relationship.setUserId1(userId);
        relationship.setUserId2(partner.getId());
        relationship.setStatus(0); // 待确认
        relationship.setInviteCode(generateInviteCode());

        // 设置审计字段
        AuditHelper.setCreateAuditFields(relationship, userId);

        coupleMapper.insert(relationship);

        // 7. 构建返回结果
        InviteResult result = new InviteResult();
        result.setInviteCode(relationship.getInviteCode());
        result.setPartnerPhoneNumber(inviteDTO.getPartnerPhoneNumber());
        result.setInviteMessage(inviteDTO.getInviteMessage());
        result.setStatus("pending");

        log.info("用户发起情侣邀请: inviterId={}, inviteeId={}, inviteCode={}",
                userId, partner.getId(), relationship.getInviteCode());

        return result;
    }

    @Override
    public AcceptResult acceptInvite(Long userId, String inviteCode) {
        // 1. 验证邀请码
        if (!StringUtils.hasText(inviteCode)) {
            throw new BusinessException("邀请码不能为空");
        }

        // 2. 查找邀请关系
        CoupleRelationship relationship = coupleMapper.selectByInviteCode(inviteCode);
        if (relationship == null) {
            throw new BusinessException("邀请码无效或已过期");
        }

        // 3. 检查邀请关系状态
        if (relationship.getStatus() != 0) {
            throw new BusinessException("邀请已失效");
        }

        // 4. 检查当前用户是否是受邀方
        if (!relationship.getUserId2().equals(userId)) {
            throw new BusinessException("邀请码不属于您");
        }

        // 5. 检查用户是否已有其他情侣关系
        CoupleRelationship existingRelationship = coupleMapper.selectConfirmedByUserId(userId);
        if (existingRelationship != null) {
            throw new BusinessException("您已有情侣关系，无法接受新邀请");
        }

        // 6. 更新关系状态为已确认
        relationship.setStatus(1); // 已确认
        relationship.setConfirmedAt(LocalDateTime.now());

        // 设置更新审计字段
        AuditHelper.setUpdateAuditFields(relationship, userId);

        coupleMapper.updateById(relationship);

        // 7. 获取发起方信息
        WheelUser inviter = userMapper.selectById(relationship.getUserId1());

        // 8. 构建返回结果
        AcceptResult result = new AcceptResult();
        result.setRelationshipId(relationship.getId());
        result.setPartnerNickname(inviter.getNickname());
        result.setStatus("confirmed");
        result.setConfirmedAt(relationship.getConfirmedAt().toString());

        log.info("用户接受情侣邀请: userId={}, inviterId={}, relationshipId={}",
                userId, relationship.getUserId1(), relationship.getId());

        return result;
    }

    @Override
    public CoupleStatus getCoupleStatus(Long userId) {
        // 1. 查询用户的所有情侣关系
        List<CoupleRelationship> relationships = coupleMapper.selectByUserId(userId);

        CoupleStatus status = new CoupleStatus();

        // 2. 查找已确认的关系
        CoupleRelationship confirmed = relationships.stream()
                .filter(rel -> rel.getStatus() == 1)
                .findFirst()
                .orElse(null);

        if (confirmed != null) {
            // 已确认关系
            status.setStatus(2);
            status.setStatusText("已确认");

            // 确定对方用户ID
            Long partnerUserId = confirmed.getUserId1().equals(userId) ?
                    confirmed.getUserId2() : confirmed.getUserId1();
            status.setPartnerUserId(partnerUserId);

            // 获取对方信息
            WheelUser partner = userMapper.selectById(partnerUserId);
            if (partner != null) {
                status.setPartnerNickname(partner.getNickname());
            }

            status.setInviteCode(confirmed.getInviteCode());
            status.setCreatedAt(confirmed.getCreateTime().toString());
        } else {
            // 查找待确认的关系
            CoupleRelationship pending = relationships.stream()
                    .filter(rel -> rel.getStatus() == 0)
                    .findFirst()
                    .orElse(null);

            if (pending != null) {
                // 待确认关系
                status.setStatus(1);
                status.setStatusText("待确认");

                // 确定对方用户ID
                Long partnerUserId = pending.getUserId1().equals(userId) ?
                        pending.getUserId2() : pending.getUserId1();
                status.setPartnerUserId(partnerUserId);

                // 获取对方信息
                WheelUser partner = userMapper.selectById(partnerUserId);
                if (partner != null) {
                    status.setPartnerNickname(partner.getNickname());
                }

                status.setInviteCode(pending.getInviteCode());
                status.setCreatedAt(pending.getCreateTime().toString());
            } else {
                // 无关系
                status.setStatus(0);
                status.setStatusText("无关系");
            }
        }

        return status;
    }

    @Override
    public boolean unbindCouple(Long userId) {
        // 1. 查找已确认的关系
        CoupleRelationship relationship = coupleMapper.selectConfirmedByUserId(userId);
        if (relationship == null) {
            throw new BusinessException("您没有情侣关系，无需解除");
        }

        // 2. 更新关系状态为已解除
        relationship.setStatus(2); // 已解除
        relationship.setUnboundAt(LocalDateTime.now());

        // 设置更新审计字段
        AuditHelper.setUpdateAuditFields(relationship, userId);

        int result = coupleMapper.updateById(relationship);

        log.info("用户解除情侣关系: userId={}, relationshipId={}", userId, relationship.getId());

        return result > 0;
    }

    @Override
    public CoupleInfo getCoupleInfo(Long userId) {
        // 1. 查找已确认的关系
        CoupleRelationship relationship = coupleMapper.selectConfirmedByUserId(userId);
        if (relationship == null) {
            return null;
        }

        // 2. 获取双方用户信息
        WheelUser user1 = userMapper.selectById(relationship.getUserId1());
        WheelUser user2 = userMapper.selectById(relationship.getUserId2());

        // 3. 构建情侣信息
        CoupleInfo info = new CoupleInfo();
        info.setUserId1(relationship.getUserId1());
        info.setNickname1(user1.getNickname());
        info.setAvatarUrl1(user1.getAvatarUrl());
        info.setUserId2(relationship.getUserId2());
        info.setNickname2(user2.getNickname());
        info.setAvatarUrl2(user2.getAvatarUrl());
        info.setStatus("confirmed");
        info.setConfirmedAt(relationship.getConfirmedAt().toString());
        info.setCreatedAt(relationship.getCreateTime().toString());

        return info;
    }

    @Override
    public boolean validateInviteCode(String inviteCode) {
        if (!StringUtils.hasText(inviteCode)) {
            return false;
        }

        CoupleRelationship relationship = coupleMapper.selectByInviteCode(inviteCode);
        return relationship != null && relationship.getStatus() == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public CoupleSpinHistory getCoupleSpinHistory(Long userId, Integer pageNum, Integer pageSize) {
        CoupleSpinHistory history = new CoupleSpinHistory();
        history.setList(List.of());
        history.setTotal(0L);

        CoupleRelationship relationship = coupleMapper.selectConfirmedByUserId(userId);
        if (relationship == null) {
            return history;
        }

        Long userId1 = relationship.getUserId1();
        Long userId2 = relationship.getUserId2();

        WheelUser user1 = userMapper.selectById(userId1);
        WheelUser user2 = userMapper.selectById(userId2);

        String user1Nickname = user1 != null ? user1.getNickname() : "用户";
        String user2Nickname = user2 != null ? user2.getNickname() : "用户";

        int pageIndex = pageNum != null && pageNum > 0 ? pageNum : 1;
        int size = pageSize != null && pageSize > 0 ? pageSize : 20;

        Page<WheelSpinRecord> page = new Page<>(pageIndex, size);
        LambdaQueryWrapper<WheelSpinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WheelSpinRecord::getUserId, userId1, userId2);
        wrapper.orderByDesc(WheelSpinRecord::getSpinTime);
        spinRecordMapper.selectPage(page, wrapper);

        List<CoupleSpinHistoryItem> items = page.getRecords().stream().map(record -> {
            CoupleSpinHistoryItem item = new CoupleSpinHistoryItem();
            item.setId(record.getId());
            item.setResultText(record.getResultText());
            item.setSpinTime(record.getSpinTime() != null ? record.getSpinTime().toString() : null);
            boolean isUser1 = record.getUserId() != null && record.getUserId().equals(userId1);
            String partnerNickname = isUser1 ? user2Nickname : user1Nickname;
            item.setPartnerNickname(partnerNickname);
            return item;
        }).toList();

        history.setList(items);
        history.setTotal(page.getTotal());
        return history;
    }

    private String generateInviteCode() {
        for (int attempt = 0; attempt < INVITE_CODE_MAX_ATTEMPTS; attempt++) {
            StringBuilder builder = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                int index = random.nextInt(INVITE_CODE_CHARS.length());
                builder.append(INVITE_CODE_CHARS.charAt(index));
            }
            String code = builder.toString();
            if (coupleMapper.selectByInviteCode(code) == null) {
                return code;
            }
        }
        throw new BusinessException("邀请码生成失败，请稍后重试");
    }
}
