package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.CommentCreateDTO;
import com.basebackend.wheel.dto.CommentVO;
import com.basebackend.wheel.dto.MomentCreateDTO;
import com.basebackend.wheel.dto.MomentUpdateDTO;
import com.basebackend.wheel.dto.MomentVO;
import com.basebackend.wheel.dto.UserBriefVO;
import com.basebackend.wheel.entity.CoupleMoment;
import com.basebackend.wheel.entity.CoupleRelationship;
import com.basebackend.wheel.entity.MomentComment;
import com.basebackend.wheel.entity.MomentLike;
import com.basebackend.wheel.entity.WheelUser;
import com.basebackend.wheel.enums.MomentVisibility;
import com.basebackend.wheel.mapper.CoupleMomentMapper;
import com.basebackend.wheel.mapper.CoupleRelationshipMapper;
import com.basebackend.wheel.mapper.MomentCommentMapper;
import com.basebackend.wheel.mapper.MomentLikeMapper;
import com.basebackend.wheel.mapper.WheelUserMapper;
import com.basebackend.wheel.service.MomentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态服务实现
 */
@Slf4j
@Service
public class MomentServiceImpl implements MomentService {

    @Autowired
    private CoupleMomentMapper coupleMomentMapper;

    @Autowired
    private MomentLikeMapper momentLikeMapper;

    @Autowired
    private MomentCommentMapper momentCommentMapper;

    @Autowired
    private CoupleRelationshipMapper coupleRelationshipMapper;

    @Autowired
    private WheelUserMapper wheelUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMoment(MomentCreateDTO createDTO, Long userId) {
        CoupleRelationship relationship = getCoupleRelationship(userId);

        CoupleMoment moment = new CoupleMoment();
        moment.setCoupleId(relationship.getId());
        moment.setUserId(userId);
        moment.setContent(createDTO.getContent());
        moment.setImages(createDTO.getImages());
        moment.setVisibility(createDTO.getVisibility() != null ? createDTO.getVisibility() : MomentVisibility.COUPLE_ONLY.getCode());
        moment.setLikeCount(0);
        moment.setCommentCount(0);

        coupleMomentMapper.insert(moment);
        log.info("用户创建动态成功: userId={}, momentId={}", userId, moment.getId());
        return moment.getId();
    }

    @Override
    public MomentVO getMomentDetail(Long momentId, Long userId) {
        CoupleMoment moment = coupleMomentMapper.selectById(momentId);
        if (moment == null ) {
            throw new BusinessException("动态不存在");
        }

        checkMomentAccessPermission(moment, userId);

        return convertToMomentVO(moment, userId);
    }

    @Override
    public Page<MomentVO> getMomentTimeline(Integer page, Integer pageSize, Long userId) {
        CoupleRelationship relationship = getCoupleRelationship(userId);

        Page<CoupleMoment> momentPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<CoupleMoment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoupleMoment::getCoupleId, relationship.getId())
                .orderByDesc(CoupleMoment::getCreateTime);

        coupleMomentMapper.selectPage(momentPage, wrapper);

        if (momentPage.getRecords().isEmpty()) {
            return new Page<>(page, pageSize);
        }

        // 批量查询用户信息 - 解决 N+1 问题
        List<Long> userIds = momentPage.getRecords().stream()
                .map(CoupleMoment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, WheelUser> userMap = wheelUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WheelUser::getId, user -> user));

        // 批量查询点赞状态 - 解决 N+1 问题
        List<Long> momentIds = momentPage.getRecords().stream()
                .map(CoupleMoment::getId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<MomentLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.in(MomentLike::getMomentId, momentIds)
                .eq(MomentLike::getUserId, userId);
        Map<Long, Boolean> likeMap = momentLikeMapper.selectList(likeWrapper).stream()
                .collect(Collectors.toMap(MomentLike::getMomentId, like -> true));

        Page<MomentVO> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(momentPage.getTotal());
        resultPage.setRecords(momentPage.getRecords().stream()
                .map(moment -> convertToMomentVO(moment, userMap.get(moment.getUserId()), likeMap.getOrDefault(moment.getId(), false)))
                .collect(Collectors.toList()));

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMoment(Long momentId, MomentUpdateDTO updateDTO, Long userId) {
        CoupleMoment moment = coupleMomentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }

        if (!moment.getUserId().equals(userId)) {
            throw new BusinessException("无权编辑此动态");
        }

        if (updateDTO.getContent() != null) {
            moment.setContent(updateDTO.getContent());
        }
        if (updateDTO.getImages() != null) {
            moment.setImages(updateDTO.getImages());
        }
        if (updateDTO.getVisibility() != null) {
            moment.setVisibility(updateDTO.getVisibility());
        }

        coupleMomentMapper.updateById(moment);
        log.info("用户更新动态成功: userId={}, momentId={}", userId, momentId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMoment(Long momentId, Long userId) {
        CoupleMoment moment = coupleMomentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }

        if (!moment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此动态");
        }

        moment.setDeleted(1);
        coupleMomentMapper.updateById(moment);
        log.info("用户删除动态成功: userId={}, momentId={}", userId, momentId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeMoment(Long momentId, Long userId) {
        CoupleMoment moment = coupleMomentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }

        checkMomentAccessPermission(moment, userId);

        LambdaQueryWrapper<MomentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MomentLike::getMomentId, momentId)
                .eq(MomentLike::getUserId, userId);

        if (momentLikeMapper.selectCount(wrapper) > 0) {
            return true;
        }

        MomentLike like = new MomentLike();
        like.setMomentId(momentId);
        like.setUserId(userId);
        momentLikeMapper.insert(like);

        coupleMomentMapper.updateLikeCount(momentId, 1);
        log.info("用户点赞动态: userId={}, momentId={}", userId, momentId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeMoment(Long momentId, Long userId) {
        LambdaQueryWrapper<MomentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MomentLike::getMomentId, momentId)
                .eq(MomentLike::getUserId, userId);

        MomentLike like = momentLikeMapper.selectOne(wrapper);
        if (like == null) {
            return true;
        }
        like.setDeleted(1);
        momentLikeMapper.updateById(like);

        coupleMomentMapper.updateLikeCount(momentId, -1);
        log.info("用户取消点赞动态: userId={}, momentId={}", userId, momentId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long momentId, CommentCreateDTO createDTO, Long userId) {
        CoupleMoment moment = coupleMomentMapper.selectById(momentId);
        if (moment == null ) {
            throw new BusinessException("动态不存在");
        }

        checkMomentAccessPermission(moment, userId);

        MomentComment comment = new MomentComment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setContent(createDTO.getContent());
        comment.setParentCommentId(createDTO.getParentCommentId());
        comment.setReplyToUserId(createDTO.getReplyToUserId());
        comment.setReplyToNickname(createDTO.getReplyToNickname());
        comment.setMentionedUserIds(createDTO.getMentionedUserIds());
        momentCommentMapper.insert(comment);

        coupleMomentMapper.updateCommentCount(momentId, 1);
        log.info("用户评论动态: userId={}, momentId={}, commentId={}, parentId={}, mentionedUsers={}",
                userId, momentId, comment.getId(), createDTO.getParentCommentId(), createDTO.getMentionedUserIds());
        return comment.getId();
    }

    @Override
    public Page<CommentVO> getComments(Long momentId, Integer page, Integer pageSize) {
        Page<MomentComment> commentPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<MomentComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MomentComment::getMomentId, momentId)
                .isNull(MomentComment::getParentCommentId)  // 只查询顶级评论
                .orderByAsc(MomentComment::getCreateTime);

        momentCommentMapper.selectPage(commentPage, wrapper);

        List<Long> userIds = commentPage.getRecords().stream()
                .map(MomentComment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 收集所有被提及的用户ID
        commentPage.getRecords().stream()
                .filter(c -> c.getMentionedUserIds() != null)
                .flatMap(c -> c.getMentionedUserIds().stream())
                .forEach(userIds::add);

        userIds = userIds.stream().distinct().collect(Collectors.toList());

        Map<Long, WheelUser> userMap = wheelUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WheelUser::getId, user -> user));

        // 统计每个评论的回复数
        List<Long> commentIds = commentPage.getRecords().stream()
                .map(MomentComment::getId)
                .collect(Collectors.toList());

        Map<Long, Long> replyCountMap = new java.util.HashMap<>();
        if (!commentIds.isEmpty()) {
            LambdaQueryWrapper<MomentComment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.in(MomentComment::getParentCommentId, commentIds)
                    .select(MomentComment::getParentCommentId);
            List<MomentComment> replies = momentCommentMapper.selectList(replyWrapper);
            replyCountMap = replies.stream()
                    .collect(Collectors.groupingBy(MomentComment::getParentCommentId, Collectors.counting()));
        }

        Map<Long, Long> finalReplyCountMap = replyCountMap;
        Page<CommentVO> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(commentPage.getTotal());
        resultPage.setRecords(commentPage.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = convertToCommentVO(comment, userMap.get(comment.getUserId()), userMap);
                    vo.setReplyCount(finalReplyCountMap.getOrDefault(comment.getId(), 0L).intValue());
                    return vo;
                })
                .collect(Collectors.toList()));

        return resultPage;
    }

    /**
     * 获取评论的回复列表
     */
    public Page<CommentVO> getCommentReplies(Long commentId, Integer page, Integer pageSize) {
        Page<MomentComment> replyPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<MomentComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MomentComment::getParentCommentId, commentId)
                .orderByAsc(MomentComment::getCreateTime);

        momentCommentMapper.selectPage(replyPage, wrapper);

        List<Long> userIds = replyPage.getRecords().stream()
                .map(MomentComment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 收集所有被提及的用户ID
        replyPage.getRecords().stream()
                .filter(c -> c.getMentionedUserIds() != null)
                .flatMap(c -> c.getMentionedUserIds().stream())
                .forEach(userIds::add);

        userIds = userIds.stream().distinct().collect(Collectors.toList());

        Map<Long, WheelUser> userMap = wheelUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WheelUser::getId, user -> user));

        Page<CommentVO> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(replyPage.getTotal());
        resultPage.setRecords(replyPage.getRecords().stream()
                .map(reply -> convertToCommentVO(reply, userMap.get(reply.getUserId()), userMap))
                .collect(Collectors.toList()));

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId, Long userId) {
        MomentComment comment = momentCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }

        comment.setDeleted(1);
        momentCommentMapper.updateById(comment);

        coupleMomentMapper.updateCommentCount(comment.getMomentId(), -1);
        log.info("用户删除评论: userId={}, commentId={}", userId, commentId);
        return true;
    }

    @Override
    public List<UserBriefVO> getMentionableUsers(Long userId) {
        CoupleRelationship relationship = getCoupleRelationship(userId);

        // 获取情侣关系中的另一个用户ID
        Long partnerId = relationship.getUserId1().equals(userId)
                ? relationship.getUserId2()
                : relationship.getUserId1();

        WheelUser partner = wheelUserMapper.selectById(partnerId);
        if (partner != null) {
            return List.of(new UserBriefVO(
                    partner.getId(),
                    partner.getNickname(),
                    partner.getAvatarUrl()
            ));
        }

        return new ArrayList<>();
    }

    private CoupleRelationship getCoupleRelationship(Long userId) {
        LambdaQueryWrapper<CoupleRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(CoupleRelationship::getUserId1, userId)
                        .or()
                        .eq(CoupleRelationship::getUserId2, userId))
                .eq(CoupleRelationship::getStatus, 1);

        CoupleRelationship relationship = coupleRelationshipMapper.selectOne(wrapper);
        if (relationship == null) {
            throw new BusinessException("未绑定情侣关系");
        }
        return relationship;
    }

    private void checkMomentAccessPermission(CoupleMoment moment, Long userId) {
        if (moment.getVisibility().equals(MomentVisibility.PUBLIC.getCode())) {
            return;
        }

        CoupleRelationship relationship = getCoupleRelationship(userId);
        if (!moment.getCoupleId().equals(relationship.getId())) {
            throw new BusinessException("无权访问此动态");
        }
    }

    private MomentVO convertToMomentVO(CoupleMoment moment, WheelUser user, Boolean isLiked) {
        MomentVO vo = new MomentVO();
        BeanUtils.copyProperties(moment, vo);

        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
        }

        vo.setIsLiked(isLiked);

        return vo;
    }

    private MomentVO convertToMomentVO(CoupleMoment moment, Long currentUserId) {
        WheelUser user = wheelUserMapper.selectById(moment.getUserId());

        LambdaQueryWrapper<MomentLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(MomentLike::getMomentId, moment.getId())
                .eq(MomentLike::getUserId, currentUserId);
        Boolean isLiked = momentLikeMapper.selectCount(likeWrapper) > 0;

        return convertToMomentVO(moment, user, isLiked);
    }

    private CommentVO convertToCommentVO(MomentComment comment, WheelUser user, Map<Long, WheelUser> userMap) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);

        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
        }

        // 填充被提及用户信息
        if (comment.getMentionedUserIds() != null && !comment.getMentionedUserIds().isEmpty()) {
            List<UserBriefVO> mentionedUsers = comment.getMentionedUserIds().stream()
                    .map(userId -> {
                        WheelUser mentionedUser = userMap.get(userId);
                        if (mentionedUser != null) {
                            return new UserBriefVO(
                                    mentionedUser.getId(),
                                    mentionedUser.getNickname(),
                                    mentionedUser.getAvatarUrl()
                            );
                        }
                        return null;
                    })
                    .filter(u -> u != null)
                    .collect(Collectors.toList());
            vo.setMentionedUsers(mentionedUsers);
        }

        return vo;
    }
}
