package com.basebackend.wheel.service.impl;

import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.UserSyncDTO;
import com.basebackend.wheel.entity.WheelUser;
import com.basebackend.wheel.mapper.WheelUserMapper;
import com.basebackend.wheel.service.WheelUserService;
import com.basebackend.wheel.util.AuditHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户同步服务实现
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Service
public class WheelUserServiceImpl implements WheelUserService {

    @Autowired
    private WheelUserMapper wheelUserMapper;

    @Override
    @Transactional
    public WheelUser syncUser(UserSyncDTO syncDTO) {
        if (syncDTO == null || syncDTO.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        Long userId = syncDTO.getUserId();
        WheelUser user = wheelUserMapper.selectById(userId);

        if (user == null) {
            user = buildUser(syncDTO);
            AuditHelper.setCreateAuditFields(user, userId);
            wheelUserMapper.insert(user);
            log.info("同步新用户: userId={}, username={}", userId, syncDTO.getUsername());
        } else {
            applyUpdates(user, syncDTO);
            AuditHelper.setUpdateAuditFields(user, userId);
            wheelUserMapper.updateById(user);
            log.info("同步更新用户: userId={}, username={}", userId, syncDTO.getUsername());
        }

        return user;
    }

    private WheelUser buildUser(UserSyncDTO syncDTO) {
        WheelUser user = new WheelUser();
        user.setId(syncDTO.getUserId());
        user.setPhoneNumber(syncDTO.getPhone());
        user.setNickname(resolveNickname(syncDTO));
        user.setAvatarUrl(syncDTO.getAvatar());
        user.setGender(syncDTO.getGender());
        user.setStatus(syncDTO.getStatus() != null ? syncDTO.getStatus() : 1);
        user.setLastLoginTime(LocalDateTime.now());
        return user;
    }

    private void applyUpdates(WheelUser user, UserSyncDTO syncDTO) {
        if (StringUtils.hasText(syncDTO.getPhone())) {
            user.setPhoneNumber(syncDTO.getPhone());
        }
        if (StringUtils.hasText(syncDTO.getNickname()) || StringUtils.hasText(syncDTO.getUsername())) {
            user.setNickname(resolveNickname(syncDTO));
        }
        if (StringUtils.hasText(syncDTO.getAvatar())) {
            user.setAvatarUrl(syncDTO.getAvatar());
        }
        if (syncDTO.getGender() != null) {
            user.setGender(syncDTO.getGender());
        }
        if (syncDTO.getStatus() != null) {
            user.setStatus(syncDTO.getStatus());
        }
        user.setLastLoginTime(LocalDateTime.now());
    }

    private String resolveNickname(UserSyncDTO syncDTO) {
        if (StringUtils.hasText(syncDTO.getNickname())) {
            return syncDTO.getNickname();
        }
        if (StringUtils.hasText(syncDTO.getUsername())) {
            return syncDTO.getUsername();
        }
        return null;
    }
}
