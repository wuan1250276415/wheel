package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.ChatMessageDTO;
import com.basebackend.wheel.dto.ChatMessageVO;
import com.basebackend.wheel.entity.ChatMessage;
import com.basebackend.wheel.entity.UserOnlineStatus;
import com.basebackend.wheel.entity.WheelUser;
import com.basebackend.wheel.mapper.ChatMessageMapper;
import com.basebackend.wheel.mapper.CoupleRelationshipMapper;
import com.basebackend.wheel.mapper.UserOnlineStatusMapper;
import com.basebackend.wheel.mapper.WheelUserMapper;
import com.basebackend.wheel.service.ChatService;
import com.basebackend.wheel.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserOnlineStatusMapper userOnlineStatusMapper;

    @Autowired
    private WheelUserMapper wheelUserMapper;

    @Autowired
    private CoupleRelationshipMapper coupleRelationshipMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired(required = false)
    private PushService pushService;

    private static final String UNREAD_KEY_PREFIX = "chat:unread:";
    private static final String ONLINE_KEY_PREFIX = "chat:online:";
    private static final int ONLINE_TTL_SECONDS = 30;
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_RECALLED = 2;
    private static final Duration RECALL_WINDOW = Duration.ofMinutes(2);

    @Override
    @Transactional
    public ChatMessageVO sendMessage(ChatMessageDTO messageDTO, Long senderId) {
        if (messageDTO.getReceiverId() == null) {
            throw new BusinessException("接收者ID不能为空");
        }

        Long coupleId = coupleRelationshipMapper.findCoupleIdByUserIds(senderId, messageDTO.getReceiverId());
        if (coupleId == null) {
            throw new BusinessException("您与对方不是情侣关系");
        }

        ChatMessage message = new ChatMessage();
        message.setCoupleId(coupleId);
        message.setSenderId(senderId);
        message.setReceiverId(messageDTO.getReceiverId());
        message.setMessageType(messageDTO.getMessageType());
        message.setContent(messageDTO.getContent());
        message.setExtraData(messageDTO.getExtraData());
        message.setIsRead(0);
        message.setStatus(STATUS_NORMAL);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        chatMessageMapper.insert(message);

        incrementUnreadCount(messageDTO.getReceiverId());

        ChatMessageVO messageVO = buildMessageVO(message);
        messageVO.setIsSelf(false); // 对于接收者，这不是自己发送的消息

        // 发送给接收者
        messagingTemplate.convertAndSendToUser(
                messageDTO.getReceiverId().toString(),
                "/queue/messages", // 不需要 /user 前缀，convertAndSendToUser 会自动添加
                messageVO);

        // 同时发送给发送者（用于多端同步）
        ChatMessageVO senderView = buildMessageVO(message);
        senderView.setIsSelf(true);
        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/messages",
                senderView);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                triggerOfflinePush(message, messageVO.getSenderNickname());
            }
        });

        log.info("消息已发送: senderId={}, receiverId={}, messageId={}",
                senderId, messageDTO.getReceiverId(), message.getId());

        return messageVO;
    }

    @Override
    public List<ChatMessageVO> getChatHistory(Long partnerId, Long currentUserId, Integer offset, Integer limit) {
        Long coupleId = coupleRelationshipMapper.findCoupleIdByUserIds(currentUserId, partnerId);
        if (coupleId == null) {
            return new ArrayList<>();
        }

        List<ChatMessage> messages = chatMessageMapper.selectByCoupleId(coupleId, offset, limit);
        List<ChatMessageVO> messageVOs = new ArrayList<>();

        for (ChatMessage message : messages) {
            ChatMessageVO vo = buildMessageVO(message);
            vo.setIsSelf(message.getSenderId().equals(currentUserId));
            messageVOs.add(vo);
        }

        return messageVOs;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long senderId, Long receiverId) {
        int count = chatMessageMapper.batchMarkAsRead(receiverId, senderId);
        if (count > 0) {
            decrementUnreadCount(receiverId, count);
            log.info("标记消息已读: receiverId={}, senderId={}, count={}", receiverId, senderId, count);
        }
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        String key = UNREAD_KEY_PREFIX + userId;
        Object count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            Integer dbCount = chatMessageMapper.countUnreadMessages(userId);
            redisTemplate.opsForValue().set(key, dbCount != null ? dbCount : 0, 24, TimeUnit.HOURS);
            return dbCount != null ? dbCount : 0;
        }

        return Integer.parseInt(count.toString());
    }

    @Override
    public void updateOnlineStatus(Long userId, boolean isOnline, String sessionId) {
        LambdaUpdateWrapper<UserOnlineStatus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserOnlineStatus::getUserId, userId);
        updateWrapper.eq(UserOnlineStatus::getIsOnline, isOnline ? 1 : 0);
        updateWrapper.eq(UserOnlineStatus::getSessionId, sessionId);
        UserOnlineStatus userOnlineStatus = new UserOnlineStatus();
        userOnlineStatus.setUserId(userId);
        userOnlineStatus.setIsOnline(isOnline ? 1 : 0);
        userOnlineStatus.setSessionId(sessionId);
        userOnlineStatusMapper.update(userOnlineStatus, updateWrapper);
        // userOnlineStatusMapper.updateOnlineStatus(userId, isOnline ? 1 : 0,
        // sessionId);

        String key = ONLINE_KEY_PREFIX + userId;
        if (isOnline) {
            redisTemplate.opsForValue().set(key, "1", ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
        } else {
            redisTemplate.delete(key);
        }

        log.info("更新在线状态: userId={}, isOnline={}", userId, isOnline);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        return redisTemplate.hasKey(key);
    }

    private void incrementUnreadCount(Long userId) {
        String key = UNREAD_KEY_PREFIX + userId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    private void decrementUnreadCount(Long userId, int count) {
        String key = UNREAD_KEY_PREFIX + userId;
        redisTemplate.opsForValue().decrement(key, count);
    }

    private ChatMessageVO buildMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setCoupleId(message.getCoupleId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setMessageType(message.getMessageType());
        vo.setContent(message.getContent());
        vo.setExtraData(message.getExtraData());
        vo.setIsRead(message.getIsRead());
        vo.setStatus(message.getStatus());
        vo.setRecalledBy(message.getRecalledBy());
        vo.setRecalledAt(message.getRecalledAt());
        vo.setReadTime(message.getReadTime());
        vo.setCreatedTime(message.getCreateTime());

        WheelUser sender = wheelUserMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderNickname(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatarUrl());
        }

        return vo;
    }

    @Override
    @Transactional
    public ChatMessageVO recallMessage(String messageId, Long operatorId) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getSenderId().equals(operatorId)) {
            throw new BusinessException("只能撤回自己发送的消息");
        }
        if (message.getStatus() == null || !message.getStatus().equals(STATUS_NORMAL)) {
            throw new BusinessException("消息无法撤回");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime created = message.getCreateTime();
        if (created == null || Duration.between(created, now).compareTo(RECALL_WINDOW) > 0) {
            throw new BusinessException("超过可撤回时间");
        }

        boolean wasUnread = message.getIsRead() == null || message.getIsRead() == 0;
        if (wasUnread) {
            message.setIsRead(1);
            message.setReadTime(now);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(UNREAD_KEY_PREFIX + message.getReceiverId()))) {
                decrementUnreadCount(message.getReceiverId(), 1);
            }
        }

        message.setStatus(STATUS_RECALLED);
        message.setRecalledBy(operatorId);
        message.setRecalledAt(now);
        message.setUpdateTime(now);
        message.setContent(null);
        message.setExtraData(null);

        int updated = chatMessageMapper.updateById(message);
        if (updated == 0) {
            throw new BusinessException("消息已被修改，请刷新后重试");
        }

        ChatMessageVO senderView = buildMessageVO(message);
        senderView.setIsSelf(true);
        messagingTemplate.convertAndSendToUser(message.getSenderId().toString(), "/queue/messages", senderView);

        ChatMessageVO receiverView = buildMessageVO(message);
        receiverView.setIsSelf(false);
        messagingTemplate.convertAndSendToUser(message.getReceiverId().toString(), "/queue/messages", receiverView);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                triggerRecallPush(message, senderView.getSenderNickname());
            }
        });

        log.info("消息已撤回: messageId={}, operatorId={}", messageId, operatorId);
        return senderView;
    }

    private void triggerOfflinePush(ChatMessage message, String senderNickname) {
        if (pushService == null) {
            return;
        }
        if (!isUserOnline(message.getReceiverId())) {
            pushService.sendOfflineMessageNotification(message, senderNickname);
        }
    }

    private void triggerRecallPush(ChatMessage message, String senderNickname) {
        if (pushService == null) {
            return;
        }
        if (!isUserOnline(message.getReceiverId())) {
            pushService.sendRecallNotification(message, senderNickname);
        }
    }
}
