package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.ChatMessageDTO;
import com.basebackend.wheel.dto.ChatMessageVO;

import java.util.List;

public interface ChatService {

    ChatMessageVO sendMessage(ChatMessageDTO messageDTO, Long senderId);

    List<ChatMessageVO> getChatHistory(Long partnerId, Long currentUserId, Integer offset, Integer limit);

    void markMessagesAsRead(Long senderId, Long receiverId);

    Integer getUnreadCount(Long userId);

    void updateOnlineStatus(Long userId, boolean isOnline, String sessionId);

    boolean isUserOnline(Long userId);

    ChatMessageVO recallMessage(String messageId, Long operatorId);
}
