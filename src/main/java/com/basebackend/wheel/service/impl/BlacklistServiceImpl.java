package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.cache.BlacklistCacheService;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.Blacklist;
import com.basebackend.wheel.mapper.BlacklistMapper;
import com.basebackend.wheel.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 黑名单管理服务实现
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final BlacklistMapper blacklistMapper;
    private final BlacklistCacheService blacklistCacheService;

    // ==================== 添加黑名单 ====================

    @Override
    @Transactional
    public Long addUserToBlacklist(BlacklistEntryDTO entry) {
        entry.setType(BlacklistEntryDTO.TYPE_USER);
        return addToBlacklist(entry);
    }

    @Override
    @Transactional
    public Long addIpToBlacklist(BlacklistEntryDTO entry) {
        entry.setType(BlacklistEntryDTO.TYPE_IP);
        return addToBlacklist(entry);
    }

    @Override
    @Transactional
    public Long addDeviceToBlacklist(BlacklistEntryDTO entry) {
        entry.setType(BlacklistEntryDTO.TYPE_DEVICE);
        return addToBlacklist(entry);
    }

    @Override
    @Transactional
    public Long addToBlacklist(BlacklistEntryDTO entry) {
        // 检查是否已存在有效的黑名单记录
        Blacklist existing = blacklistMapper.selectActiveByTypeAndTarget(
                entry.getType(), entry.getTargetId());
        
        if (existing != null) {
            log.warn("目标已在黑名单中: type={}, targetId={}", entry.getType(), entry.getTargetId());
            // 更新现有记录
            existing.setReason(entry.getReason());
            existing.setDuration(entry.getDuration() != null ? entry.getDuration() : 0);
            existing.setOperatorId(entry.getOperatorId());
            
            // 计算过期时间
            if (entry.isPermanent()) {
                existing.setExpireAt(null);
            } else {
                existing.setExpireAt(LocalDateTime.now().plusMinutes(entry.getDuration()));
            }
            
            blacklistMapper.updateById(existing);
            return existing.getId();
        }

        // 创建新的黑名单记录
        Blacklist blacklist = new Blacklist();
        blacklist.setType(entry.getType());
        blacklist.setTargetId(entry.getTargetId());
        blacklist.setReason(entry.getReason());
        blacklist.setDuration(entry.getDuration() != null ? entry.getDuration() : 0);
        blacklist.setOperatorId(entry.getOperatorId());
        blacklist.setStatus(Blacklist.STATUS_ACTIVE);
        blacklist.setAppealStatus(Blacklist.APPEAL_NONE);

        // 计算过期时间
        if (entry.isPermanent()) {
            blacklist.setExpireAt(null);
        } else {
            blacklist.setExpireAt(LocalDateTime.now().plusMinutes(entry.getDuration()));
        }

        blacklistMapper.insert(blacklist);
        
        // 清除缓存
        clearCacheByType(entry.getType(), entry.getTargetId());
        
        log.info("添加黑名单成功: type={}, targetId={}, duration={}, operatorId={}",
                entry.getType(), entry.getTargetId(), entry.getDuration(), entry.getOperatorId());
        
        return blacklist.getId();
    }

    // ==================== 检查黑名单 ====================

    @Override
    public BlacklistCheckResult checkUser(Long userId) {
        if (userId == null) {
            return BlacklistCheckResult.notBlocked();
        }
        
        // 先检查缓存
        BlacklistCheckResult cached = blacklistCacheService.getUserBlacklistCache(userId);
        if (cached != null) {
            return cached;
        }
        
        Blacklist blacklist = blacklistMapper.selectActiveByUserId(userId);
        BlacklistCheckResult result = buildCheckResult(blacklist);
        
        // 设置缓存
        blacklistCacheService.setUserBlacklistCache(userId, result);
        
        return result;
    }

    @Override
    public BlacklistCheckResult checkIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return BlacklistCheckResult.notBlocked();
        }
        
        // 先检查缓存
        BlacklistCheckResult cached = blacklistCacheService.getIpBlacklistCache(ip);
        if (cached != null) {
            return cached;
        }
        
        Blacklist blacklist = blacklistMapper.selectActiveByIp(ip);
        BlacklistCheckResult result = buildCheckResult(blacklist);
        
        // 设置缓存
        blacklistCacheService.setIpBlacklistCache(ip, result);
        
        return result;
    }

    @Override
    public BlacklistCheckResult checkDevice(String deviceId) {
        if (!StringUtils.hasText(deviceId)) {
            return BlacklistCheckResult.notBlocked();
        }
        
        // 先检查缓存
        BlacklistCheckResult cached = blacklistCacheService.getDeviceBlacklistCache(deviceId);
        if (cached != null) {
            return cached;
        }
        
        Blacklist blacklist = blacklistMapper.selectActiveByDeviceId(deviceId);
        BlacklistCheckResult result = buildCheckResult(blacklist);
        
        // 设置缓存
        blacklistCacheService.setDeviceBlacklistCache(deviceId, result);
        
        return result;
    }

    @Override
    public BlacklistCheckResult checkAll(Long userId, String ip, String deviceId) {
        // 按优先级检查：用户 > IP > 设备
        BlacklistCheckResult result = checkUser(userId);
        if (result.isBlocked()) {
            return result;
        }
        
        result = checkIp(ip);
        if (result.isBlocked()) {
            return result;
        }
        
        return checkDevice(deviceId);
    }

    /**
     * 构建检查结果
     */
    private BlacklistCheckResult buildCheckResult(Blacklist blacklist) {
        if (blacklist == null) {
            return BlacklistCheckResult.notBlocked();
        }
        
        // 检查是否已过期
        if (blacklist.isExpired()) {
            // 自动解除过期的黑名单
            blacklist.setStatus(Blacklist.STATUS_RELEASED);
            blacklistMapper.updateById(blacklist);
            return BlacklistCheckResult.notBlocked();
        }
        
        return BlacklistCheckResult.blocked(
                blacklist.getId(),
                blacklist.getReason(),
                blacklist.getExpireAt(),
                blacklist.canAppeal(),
                blacklist.getType()
        );
    }

    // ==================== 解除黑名单 ====================

    @Override
    @Transactional
    public void removeFromBlacklist(Long blacklistId, Long operatorId) {
        Blacklist blacklist = blacklistMapper.selectById(blacklistId);
        if (blacklist == null) {
            log.warn("黑名单记录不存在: id={}", blacklistId);
            return;
        }
        
        blacklist.setStatus(Blacklist.STATUS_RELEASED);
        blacklist.setUpdateBy(operatorId);
        blacklistMapper.updateById(blacklist);
        
        // 清除缓存
        blacklistCacheService.clearCacheByBlacklist(blacklist);
        
        log.info("解除黑名单成功: id={}, operatorId={}", blacklistId, operatorId);
    }

    @Override
    @Transactional
    public void removeUserFromBlacklist(Long userId, Long operatorId) {
        Blacklist blacklist = blacklistMapper.selectActiveByUserId(userId);
        if (blacklist != null) {
            removeFromBlacklist(blacklist.getId(), operatorId);
        }
    }

    @Override
    @Transactional
    public void removeIpFromBlacklist(String ip, Long operatorId) {
        Blacklist blacklist = blacklistMapper.selectActiveByIp(ip);
        if (blacklist != null) {
            removeFromBlacklist(blacklist.getId(), operatorId);
        }
    }

    @Override
    @Transactional
    public void removeDeviceFromBlacklist(String deviceId, Long operatorId) {
        Blacklist blacklist = blacklistMapper.selectActiveByDeviceId(deviceId);
        if (blacklist != null) {
            removeFromBlacklist(blacklist.getId(), operatorId);
        }
    }

    // ==================== 申诉功能 ====================

    @Override
    @Transactional
    public void submitAppeal(BlacklistAppealDTO appealDTO) {
        Blacklist blacklist = blacklistMapper.selectById(appealDTO.getBlacklistId());
        if (blacklist == null) {
            throw new IllegalArgumentException("黑名单记录不存在");
        }
        
        if (!blacklist.canAppeal()) {
            throw new IllegalStateException("当前状态不允许申诉");
        }
        
        blacklist.setAppealStatus(Blacklist.APPEAL_PENDING);
        blacklist.setAppealReason(appealDTO.getAppealReason());
        blacklist.setAppealTime(LocalDateTime.now());
        blacklistMapper.updateById(blacklist);
        
        log.info("提交申诉成功: blacklistId={}, appealReason={}", 
                appealDTO.getBlacklistId(), appealDTO.getAppealReason());
    }

    @Override
    @Transactional
    public void handleAppeal(BlacklistAppealHandleDTO handleDTO) {
        Blacklist blacklist = blacklistMapper.selectById(handleDTO.getBlacklistId());
        if (blacklist == null) {
            throw new IllegalArgumentException("黑名单记录不存在");
        }
        
        if (blacklist.getAppealStatus() != Blacklist.APPEAL_PENDING) {
            throw new IllegalStateException("当前申诉状态不允许处理");
        }
        
        if (handleDTO.getApproved()) {
            // 申诉通过，解除黑名单
            blacklist.setAppealStatus(Blacklist.APPEAL_APPROVED);
            blacklist.setStatus(Blacklist.STATUS_RELEASED);
        } else {
            // 申诉驳回
            blacklist.setAppealStatus(Blacklist.APPEAL_REJECTED);
        }
        
        blacklist.setAppealHandlerId(handleDTO.getHandlerId());
        blacklist.setAppealHandledAt(LocalDateTime.now());
        blacklist.setAppealResult(handleDTO.getResult());
        blacklistMapper.updateById(blacklist);
        
        // 清除缓存
        blacklistCacheService.clearCacheByBlacklist(blacklist);
        
        log.info("处理申诉完成: blacklistId={}, approved={}, handlerId={}",
                handleDTO.getBlacklistId(), handleDTO.getApproved(), handleDTO.getHandlerId());
    }

    @Override
    public List<BlacklistVO> getPendingAppeals() {
        List<Blacklist> appeals = blacklistMapper.selectPendingAppeals();
        return appeals.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ==================== 查询功能 ====================

    @Override
    public BlacklistVO getById(Long blacklistId) {
        Blacklist blacklist = blacklistMapper.selectById(blacklistId);
        if (blacklist == null) {
            return null;
        }
        return convertToVO(blacklist);
    }

    @Override
    public Page<BlacklistVO> getBlacklistPage(int page, int pageSize, Integer type,
                                               Integer status, String keyword) {
        Page<Blacklist> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        
        if (type != null) {
            wrapper.eq(Blacklist::getType, type);
        }
        
        if (status != null) {
            wrapper.eq(Blacklist::getStatus, status);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Blacklist::getTargetId, keyword)
                    .or()
                    .like(Blacklist::getReason, keyword)
            );
        }
        
        wrapper.orderByDesc(Blacklist::getCreateTime);
        
        Page<Blacklist> resultPage = blacklistMapper.selectPage(pageParam, wrapper);
        
        // 转换为VO
        Page<BlacklistVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }

    // ==================== 过期处理 ====================

    @Override
    @Transactional
    public int releaseExpiredBlacklist() {
        LocalDateTime now = LocalDateTime.now();
        int count = blacklistMapper.batchReleaseExpired(now);
        
        if (count > 0) {
            log.info("自动解除过期黑名单: count={}", count);
        }
        
        return count;
    }

    @Override
    public List<Blacklist> getExpiringBlacklist(int minutesBeforeExpire) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireThreshold = now.plusMinutes(minutesBeforeExpire);
        
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getStatus, Blacklist.STATUS_ACTIVE)
                .isNotNull(Blacklist::getExpireAt)
                .gt(Blacklist::getExpireAt, now)
                .le(Blacklist::getExpireAt, expireThreshold);
        
        return blacklistMapper.selectList(wrapper);
    }

    // ==================== 统计功能 ====================

    @Override
    public Map<Integer, Long> countByType() {
        List<Map<String, Object>> stats = blacklistMapper.countByType();
        Map<Integer, Long> result = new HashMap<>();
        
        for (Map<String, Object> stat : stats) {
            Integer type = (Integer) stat.get("type");
            Long count = ((Number) stat.get("count")).longValue();
            result.put(type, count);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总数
        LambdaQueryWrapper<Blacklist> totalWrapper = new LambdaQueryWrapper<>();
        statistics.put("total", blacklistMapper.selectCount(totalWrapper));
        
        // 生效中数量
        LambdaQueryWrapper<Blacklist> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Blacklist::getStatus, Blacklist.STATUS_ACTIVE);
        statistics.put("active", blacklistMapper.selectCount(activeWrapper));
        
        // 已解除数量
        LambdaQueryWrapper<Blacklist> releasedWrapper = new LambdaQueryWrapper<>();
        releasedWrapper.eq(Blacklist::getStatus, Blacklist.STATUS_RELEASED);
        statistics.put("released", blacklistMapper.selectCount(releasedWrapper));
        
        // 待处理申诉数量
        LambdaQueryWrapper<Blacklist> pendingAppealWrapper = new LambdaQueryWrapper<>();
        pendingAppealWrapper.eq(Blacklist::getAppealStatus, Blacklist.APPEAL_PENDING);
        statistics.put("pendingAppeals", blacklistMapper.selectCount(pendingAppealWrapper));
        
        // 各类型统计
        statistics.put("byType", countByType());
        
        return statistics;
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为VO
     */
    private BlacklistVO convertToVO(Blacklist blacklist) {
        BlacklistVO vo = new BlacklistVO();
        vo.setId(blacklist.getId());
        vo.setType(blacklist.getType());
        vo.setTypeName(BlacklistVO.getTypeName(blacklist.getType()));
        vo.setTargetId(blacklist.getTargetId());
        vo.setReason(blacklist.getReason());
        vo.setDuration(blacklist.getDuration());
        vo.setExpireAt(blacklist.getExpireAt());
        vo.setOperatorId(blacklist.getOperatorId());
        vo.setStatus(blacklist.getStatus());
        vo.setStatusName(BlacklistVO.getStatusName(blacklist.getStatus()));
        vo.setAppealStatus(blacklist.getAppealStatus());
        vo.setAppealStatusName(BlacklistVO.getAppealStatusName(blacklist.getAppealStatus()));
        vo.setAppealReason(blacklist.getAppealReason());
        vo.setAppealTime(blacklist.getAppealTime());
        vo.setAppealHandlerId(blacklist.getAppealHandlerId());
        vo.setAppealHandledAt(blacklist.getAppealHandledAt());
        vo.setAppealResult(blacklist.getAppealResult());
        vo.setCreateTime(blacklist.getCreateTime());
        vo.setPermanent(blacklist.isPermanent());
        vo.setCanAppeal(blacklist.canAppeal());
        return vo;
    }

    /**
     * 根据类型清除缓存
     */
    private void clearCacheByType(Integer type, String targetId) {
        switch (type) {
            case Blacklist.TYPE_USER:
                blacklistCacheService.clearUserBlacklistCache(Long.parseLong(targetId));
                break;
            case Blacklist.TYPE_IP:
                blacklistCacheService.clearIpBlacklistCache(targetId);
                break;
            case Blacklist.TYPE_DEVICE:
                blacklistCacheService.clearDeviceBlacklistCache(targetId);
                break;
            default:
                log.warn("未知的黑名单类型: type={}", type);
        }
    }
}
