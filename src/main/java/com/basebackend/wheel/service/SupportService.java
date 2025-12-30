package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.*;

import java.util.List;

/**
 * 客服支持服务
 *
 * @author wheel-api
 * @since 2025-01-29
 */
public interface SupportService {

    /**
     * 创建工单
     * VIP用户可创建，SVIP用户自动高优先级并分配专属客服
     *
     * @param userId 用户ID
     * @param dto    工单信息
     * @return 工单VO
     */
    TicketVO createTicket(Long userId, TicketCreateDTO dto);

    /**
     * 获取我的工单列表
     *
     * @param userId 用户ID
     * @return 工单列表
     */
    List<TicketVO> getMyTickets(Long userId);

    /**
     * 获取工单详情
     *
     * @param userId   用户ID
     * @param ticketId 工单ID
     * @return 工单VO
     */
    TicketVO getTicketDetail(Long userId, Long ticketId);

    /**
     * 获取工单回复列表
     *
     * @param userId   用户ID
     * @param ticketId 工单ID
     * @return 回复列表
     */
    List<ReplyVO> getTicketReplies(Long userId, Long ticketId);

    /**
     * 回复工单
     *
     * @param userId   用户ID
     * @param ticketId 工单ID
     * @param content  回复内容
     * @return 回复VO
     */
    ReplyVO replyTicket(Long userId, Long ticketId, String content);

    /**
     * 评价工单
     *
     * @param userId   用户ID
     * @param ticketId 工单ID
     * @param rating   评分（1-5星）
     * @param comment  评价内容
     */
    void rateTicket(Long userId, Long ticketId, Integer rating, String comment);

    /**
     * 获取FAQ列表
     *
     * @param category 分类（可选）
     * @return FAQ列表
     */
    List<FaqVO> getFaqList(String category);

    /**
     * 标记FAQ为有帮助
     *
     * @param faqId FAQ ID
     */
    void markFaqHelpful(Long faqId);
}
