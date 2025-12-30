package com.basebackend.wheel.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

/**
 * STOMP 消息拦截器
 * 用于在 STOMP CONNECT 时设置用户的 Principal
 * 这是使 convertAndSendToUser 能够正确工作的关键
 */
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从 session attributes 中获取在 HandshakeInterceptor 中设置的 userId
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Object userId = sessionAttributes.get("userId");
                if (userId != null) {
                    // 创建一个简单的 Principal，使用 userId 作为 name
                    Principal principal = new StompPrincipal(userId.toString());
                    accessor.setUser(principal);
                }
            }
        }

        return message;
    }

    /**
     * 简单的 Principal 实现
     */
    public static class StompPrincipal implements Principal {
        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
