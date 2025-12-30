package com.basebackend.wheel.controller;

import com.basebackend.common.model.Result;
import com.basebackend.jwt.JwtUtil;
import com.basebackend.wheel.dto.ChatMessageVO;
import com.basebackend.wheel.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@Tag(name = "聊天管理", description = "聊天消息相关接口")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtService;

    @Operation(summary = "获取聊天历史")
    @GetMapping("/history")
    public Result<List<ChatMessageVO>> getChatHistory(
            @RequestParam Long partnerId,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<ChatMessageVO> messages = chatService.getChatHistory(partnerId, currentUserId, offset, limit);
        return Result.success(messages);
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/read")
    public Result<Void> markAsRead(@RequestParam Long senderId, HttpServletRequest request) {
        Long receiverId = getCurrentUserId(request);
        chatService.markMessagesAsRead(senderId, receiverId);
        return Result.success();
    }

    @Operation(summary = "撤回消息")
    @PostMapping("/messages/{messageId}/recall")
    public Result<ChatMessageVO> recallMessage(@PathVariable String messageId, HttpServletRequest request) {
        Long operatorId = getCurrentUserId(request);
        ChatMessageVO recalled = chatService.recallMessage(messageId, operatorId);
        return Result.success(recalled);
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Integer count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "获取用户在线状态")
    @GetMapping("/status/{userId}")
    public Result<Map<String, Object>> getUserStatus(@PathVariable Long userId) {
        boolean isOnline = chatService.isUserOnline(userId);
        Map<String, Object> status = new HashMap<>();
        status.put("userId", userId);
        status.put("isOnline", isOnline);
        return Result.success(status);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtService.getUserIdFromToken(token);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
