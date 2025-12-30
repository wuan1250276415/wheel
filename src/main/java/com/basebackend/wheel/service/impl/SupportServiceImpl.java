package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.*;
import com.basebackend.wheel.enums.TicketPriority;
import com.basebackend.wheel.enums.TicketStatus;
import com.basebackend.wheel.mapper.*;
import com.basebackend.wheel.service.SupportService;
import com.basebackend.wheel.util.MembershipPrivilegeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客服支持服务实现
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Slf4j
@Service
public class SupportServiceImpl implements SupportService {

    @Autowired
    private SupportTicketMapper supportTicketMapper;

    @Autowired
    private SupportStaffMapper supportStaffMapper;

    @Autowired
    private TicketReplyMapper ticketReplyMapper;

    @Autowired
    private TicketRatingMapper ticketRatingMapper;

    @Autowired
    private FaqMapper faqMapper;

    @Autowired
    private MembershipPrivilegeHelper membershipPrivilegeHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketVO createTicket(Long userId, TicketCreateDTO dto) {
        // VIP权益检查 - 普通用户无法创建工单
        if (!membershipPrivilegeHelper.hasDedicatedSupport(userId) &&
            !membershipPrivilegeHelper.hasAdFree(userId)) {
            throw new BusinessException("创建工单需要VIP会员权限");
        }

        // 创建工单
        SupportTicket ticket = new SupportTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setUserId(userId);
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setCategory(dto.getCategory());
        ticket.setStatus(TicketStatus.PENDING);

        // SVIP用户自动高优先级
        if (membershipPrivilegeHelper.hasDedicatedSupport(userId)) {
            ticket.setPriority(TicketPriority.URGENT);
            // 分配VIP专属客服
            SupportStaff vipStaff = supportStaffMapper.selectAvailableVipStaff();
            if (vipStaff != null) {
                ticket.setAssignedTo(vipStaff.getId());
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                // 增加客服工单数
                supportStaffMapper.incrementCurrentTickets(vipStaff.getId());
                log.info("SVIP工单已分配VIP专属客服: ticketNo={}, staffId={}, staffName={}",
                        ticket.getTicketNo(), vipStaff.getId(), vipStaff.getStaffName());
            }
        } else {
            // VIP用户普通优先级
            ticket.setPriority(TicketPriority.MEDIUM);
            // 分配普通客服
            SupportStaff normalStaff = supportStaffMapper.selectAvailableNormalStaff();
            if (normalStaff != null) {
                ticket.setAssignedTo(normalStaff.getId());
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                supportStaffMapper.incrementCurrentTickets(normalStaff.getId());
            }
        }

        supportTicketMapper.insert(ticket);
        log.info("工单创建成功: userId={}, ticketNo={}, priority={}",
                userId, ticket.getTicketNo(), ticket.getPriority());

        return convertToVO(ticket);
    }

    @Override
    public List<TicketVO> getMyTickets(Long userId) {
        LambdaQueryWrapper<SupportTicket> query = new LambdaQueryWrapper<>();
        query.eq(SupportTicket::getUserId, userId)
                .eq(SupportTicket::getDeleted, 0)
                .orderByDesc(SupportTicket::getCreateTime);

        List<SupportTicket> tickets = supportTicketMapper.selectList(query);
        return tickets.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public TicketVO getTicketDetail(Long userId, Long ticketId) {
        SupportTicket ticket = supportTicketMapper.selectById(ticketId);
        if (ticket == null || ticket.getDeleted() == 1) {
            throw new BusinessException("工单不存在");
        }

        if (!ticket.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该工单");
        }

        return convertToVO(ticket);
    }

    @Override
    public List<ReplyVO> getTicketReplies(Long userId, Long ticketId) {
        // 验证工单权限
        SupportTicket ticket = supportTicketMapper.selectById(ticketId);
        if (ticket == null || !ticket.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该工单");
        }

        LambdaQueryWrapper<TicketReply> query = new LambdaQueryWrapper<>();
        query.eq(TicketReply::getTicketId, ticketId)
                .eq(TicketReply::getDeleted, 0)
                .orderByAsc(TicketReply::getCreateTime);

        List<TicketReply> replies = ticketReplyMapper.selectList(query);
        return replies.stream().map(this::convertReplyToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReplyVO replyTicket(Long userId, Long ticketId, String content) {
        // 验证工单
        SupportTicket ticket = supportTicketMapper.selectById(ticketId);
        if (ticket == null || !ticket.getUserId().equals(userId)) {
            throw new BusinessException("无权回复该工单");
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessException("工单已关闭，无法回复");
        }

        // 创建回复
        TicketReply reply = new TicketReply();
        reply.setTicketId(ticketId);
        reply.setIsStaff(0);
        reply.setSenderId(userId);
        reply.setContent(content);

        ticketReplyMapper.insert(reply);

        // 更新工单状态为待回复
        if (ticket.getStatus() != TicketStatus.AWAITING_REPLY) {
            ticket.setStatus(TicketStatus.AWAITING_REPLY);
            supportTicketMapper.updateById(ticket);
        }

        return convertReplyToVO(reply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateTicket(Long userId, Long ticketId, Integer rating, String comment) {
        // 验证工单
        SupportTicket ticket = supportTicketMapper.selectById(ticketId);
        if (ticket == null || !ticket.getUserId().equals(userId)) {
            throw new BusinessException("无权评价该工单");
        }

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new BusinessException("只能评价已解决的工单");
        }

        // 检查是否已评价
        LambdaQueryWrapper<TicketRating> query = new LambdaQueryWrapper<>();
        query.eq(TicketRating::getTicketId, ticketId);
        TicketRating existingRating = ticketRatingMapper.selectOne(query);

        if (existingRating != null) {
            // 更新评价
            existingRating.setRating(rating);
            existingRating.setComment(comment);
            ticketRatingMapper.updateById(existingRating);
        } else {
            // 创建评价
            TicketRating ticketRating = new TicketRating();
            ticketRating.setTicketId(ticketId);
            ticketRating.setUserId(userId);
            ticketRating.setRating(rating);
            ticketRating.setComment(comment);
            ticketRatingMapper.insert(ticketRating);
        }

        // 更新工单评分
        ticket.setRating(rating);
        ticket.setRatingComment(comment);
        supportTicketMapper.updateById(ticket);

        log.info("工单评价成功: ticketId={}, rating={}", ticketId, rating);
    }

    @Override
    public List<FaqVO> getFaqList(String category) {
        LambdaQueryWrapper<Faq> query = new LambdaQueryWrapper<>();
        query.eq(Faq::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            query.eq(Faq::getCategory, category);
        }
        query.orderByDesc(Faq::getSortOrder);

        List<Faq> faqs = faqMapper.selectList(query);
        return faqs.stream().map(this::convertFaqToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFaqHelpful(Long faqId) {
        Faq faq = faqMapper.selectById(faqId);
        if (faq != null) {
            faq.setHelpfulCount((faq.getHelpfulCount() == null ? 0 : faq.getHelpfulCount()) + 1);
            faqMapper.updateById(faq);
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 生成工单编号
     */
    private String generateTicketNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 10000));
        return "TK" + timestamp + String.format("%04d", Integer.parseInt(random));
    }

    /**
     * 转换为VO
     */
    private TicketVO convertToVO(SupportTicket ticket) {
        TicketVO vo = new TicketVO();
        BeanUtils.copyProperties(ticket, vo);

        // 查询客服信息
        if (ticket.getAssignedTo() != null) {
            SupportStaff staff = supportStaffMapper.selectById(ticket.getAssignedTo());
            if (staff != null) {
                vo.setAssignedStaffName(staff.getStaffName());
                vo.setIsVipDedicated(staff.getIsVipDedicated() == 1);
            }
        }

        // 查询未读回复数（简化实现，可后续优化）
        vo.setUnreadCount(0);

        return vo;
    }

    /**
     * 转换回复为VO
     */
    private ReplyVO convertReplyToVO(TicketReply reply) {
        ReplyVO vo = new ReplyVO();
        vo.setId(reply.getId());
        vo.setIsStaff(reply.getIsStaff() == 1);
        vo.setSenderName(reply.getSenderName());
        vo.setContent(reply.getContent());
        vo.setCreateTime(reply.getCreateTime());
        return vo;
    }

    /**
     * 转换FAQ为VO
     */
    private FaqVO convertFaqToVO(Faq faq) {
        FaqVO vo = new FaqVO();
        BeanUtils.copyProperties(faq, vo);
        vo.setIsHelpful(false);
        return vo;
    }
}
