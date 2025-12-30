package com.basebackend.wheel.controller;

import com.basebackend.wheel.dto.ChatMessageDTO;
import com.basebackend.wheel.dto.ChatMessageVO;
import com.basebackend.wheel.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Controller
public class WebSocketChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO messageDTO, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            log.warn("会话属性为空，拒绝消息");
            return;
        }

        Long senderId = (Long) sessionAttributes.get("userId");
        if (senderId == null) {
            log.warn("发送者ID为空，拒绝消息");
            return;
        }

        try {
            ChatMessageVO messageVO = chatService.sendMessage(messageDTO, senderId);
            log.info("WebSocket消息发送成功: senderId={}, messageId={}", senderId, messageVO.getId());
        } catch (Exception e) {
            log.error("WebSocket消息发送失败: senderId={}, error={}", senderId, e.getMessage(), e);
        }
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headers.getSessionAttributes();

        if (sessionAttributes != null) {
            Long userId = (Long) sessionAttributes.get("userId");
            String sessionId = headers.getSessionId();

            if (userId != null) {
                chatService.updateOnlineStatus(userId, true, sessionId);
                log.info("用户上线: userId={}, sessionId={}", userId, sessionId);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headers.getSessionAttributes();

        if (sessionAttributes != null) {
            Long userId = (Long) sessionAttributes.get("userId");
            String sessionId = headers.getSessionId();

            if (userId != null) {
                chatService.updateOnlineStatus(userId, false, sessionId);
                log.info("用户下线: userId={}, sessionId={}", userId, sessionId);
            }
        }
    }
}
