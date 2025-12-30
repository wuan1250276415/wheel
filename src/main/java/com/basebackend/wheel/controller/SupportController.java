package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客服支持控制器
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Slf4j
@RestController
@RequestMapping("/api/support")
@Tag(name = "客服支持", description = "客服工单和FAQ相关接口")
public class SupportController {

    @Autowired
    private SupportService supportService;

    @Operation(summary = "创建工单", description = "创建客服工单（需要VIP权限，SVIP自动高优先级）")
    @PostMapping("/tickets")
    public Result<TicketVO> createTicket(@RequestBody TicketCreateDTO dto) {
        try {
            Long userId = UserContextHolder.getUserId();
            TicketVO ticket = supportService.createTicket(userId, dto);
            return Result.success(ticket);
        } catch (Exception e) {
            log.error("创建工单失败: userId={}, error={}",
                    UserContextHolder.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "我的工单列表", description = "获取当前用户的所有工单")
    @GetMapping("/tickets")
    public Result<List<TicketVO>> getMyTickets() {
        try {
            Long userId = UserContextHolder.getUserId();
            List<TicketVO> tickets = supportService.getMyTickets(userId);
            return Result.success(tickets);
        } catch (Exception e) {
            log.error("获取工单列表失败: userId={}, error={}",
                    UserContextHolder.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "工单详情", description = "获取工单详细信息")
    @GetMapping("/tickets/{id}")
    public Result<TicketVO> getTicketDetail(@PathVariable Long id) {
        try {
            Long userId = UserContextHolder.getUserId();
            TicketVO ticket = supportService.getTicketDetail(userId, id);
            return Result.success(ticket);
        } catch (Exception e) {
            log.error("获取工单详情失败: userId={}, ticketId={}, error={}",
                    UserContextHolder.getUserId(), id, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "工单回复列表", description = "获取工单的所有回复")
    @GetMapping("/tickets/{id}/replies")
    public Result<List<ReplyVO>> getTicketReplies(@PathVariable Long id) {
        try {
            Long userId = UserContextHolder.getUserId();
            List<ReplyVO> replies = supportService.getTicketReplies(userId, id);
            return Result.success(replies);
        } catch (Exception e) {
            log.error("获取工单回复失败: userId={}, ticketId={}, error={}",
                    UserContextHolder.getUserId(), id, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "回复工单", description = "用户回复工单")
    @PostMapping("/tickets/{id}/reply")
    public Result<ReplyVO> replyTicket(@PathVariable Long id, @RequestParam String content) {
        try {
            Long userId = UserContextHolder.getUserId();
            ReplyVO reply = supportService.replyTicket(userId, id, content);
            return Result.success(reply);
        } catch (Exception e) {
            log.error("回复工单失败: userId={}, ticketId={}, error={}",
                    UserContextHolder.getUserId(), id, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "评价工单", description = "对已解决的工单进行满意度评价")
    @PostMapping("/tickets/{id}/rating")
    public Result<Void> rateTicket(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        try {
            Long userId = UserContextHolder.getUserId();
            supportService.rateTicket(userId, id, rating, comment);
            return Result.success();
        } catch (Exception e) {
            log.error("评价工单失败: userId={}, ticketId={}, rating={}, error={}",
                    UserContextHolder.getUserId(), id, rating, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "FAQ列表", description = "获取常见问题列表")
    @GetMapping("/faq")
    public Result<List<FaqVO>> getFaqList(@RequestParam(required = false) String category) {
        try {
            List<FaqVO> faqs = supportService.getFaqList(category);
            return Result.success(faqs);
        } catch (Exception e) {
            log.error("获取FAQ列表失败: category={}, error={}", category, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "标记FAQ有帮助", description = "标记某个FAQ为有帮助")
    @PostMapping("/faq/{id}/helpful")
    public Result<Void> markFaqHelpful(@PathVariable Long id) {
        try {
            supportService.markFaqHelpful(id);
            return Result.success();
        } catch (Exception e) {
            log.error("标记FAQ有帮助失败: faqId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
}
