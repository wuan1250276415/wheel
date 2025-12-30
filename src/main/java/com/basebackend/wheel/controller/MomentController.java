package com.basebackend.wheel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.CommentCreateDTO;
import com.basebackend.wheel.dto.CommentVO;
import com.basebackend.wheel.dto.MomentCreateDTO;
import com.basebackend.wheel.dto.MomentUpdateDTO;
import com.basebackend.wheel.dto.MomentVO;
import com.basebackend.wheel.dto.UserBriefVO;
import com.basebackend.wheel.service.MomentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 情侣动态控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/moments")
@Tag(name = "情侣动态管理", description = "动态发布、点赞、评论相关接口")
public class MomentController {

    @Autowired
    private MomentService momentService;

    @Operation(summary = "创建动态", description = "发布新的情侣动态")
    @PostMapping
    public Result<Long> createMoment(@Valid @RequestBody MomentCreateDTO createDTO) {
        try {
            Long userId = UserContextHolder.getUserId();
            Long momentId = momentService.createMoment(createDTO, userId);
            log.info("用户创建动态成功: userId={}, momentId={}", userId, momentId);
            return Result.success(momentId);
        } catch (Exception e) {
            log.error("创建动态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取动态详情", description = "获取指定动态的详细信息")
    @GetMapping("/{momentId}")
    public Result<MomentVO> getMomentDetail(@PathVariable Long momentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            MomentVO moment = momentService.getMomentDetail(momentId, userId);
            return Result.success(moment);
        } catch (Exception e) {
            log.error("获取动态详情失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取动态时间线", description = "分页获取情侣动态时间线")
    @GetMapping("/timeline")
    public Result<Page<MomentVO>> getMomentTimeline(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Long userId = UserContextHolder.getUserId();
            Page<MomentVO> timeline = momentService.getMomentTimeline(page, pageSize, userId);
            return Result.success(timeline);
        } catch (Exception e) {
            log.error("获取动态时间线失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "更新动态", description = "更新指定的动态")
    @PutMapping("/{momentId}")
    public Result<Void> updateMoment(
            @PathVariable Long momentId,
            @Valid @RequestBody MomentUpdateDTO updateDTO) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean success = momentService.updateMoment(momentId, updateDTO, userId);
            if (success) {
                log.info("更新动态成功: userId={}, momentId={}", userId, momentId);
                return Result.success();
            } else {
                return Result.error();
            }
        } catch (Exception e) {
            log.error("更新动态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "删除动态", description = "删除指定的动态")
    @DeleteMapping("/{momentId}")
    public Result<Void> deleteMoment(@PathVariable Long momentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean success = momentService.deleteMoment(momentId, userId);
            if (success) {
                log.info("删除动态成功: userId={}, momentId={}", userId, momentId);
                return Result.success();
            } else {
                return Result.error();
            }
        } catch (Exception e) {
            log.error("删除动态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "点赞动态", description = "为动态点赞")
    @PostMapping("/{momentId}/like")
    public Result<Void> likeMoment(@PathVariable Long momentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            momentService.likeMoment(momentId, userId);
            log.info("点赞动态成功: userId={}, momentId={}", userId, momentId);
            return Result.success();
        } catch (Exception e) {
            log.error("点赞动态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "取消点赞", description = "取消对动态的点赞")
    @DeleteMapping("/{momentId}/like")
    public Result<Void> unlikeMoment(@PathVariable Long momentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            momentService.unlikeMoment(momentId, userId);
            log.info("取消点赞成功: userId={}, momentId={}", userId, momentId);
            return Result.success();
        } catch (Exception e) {
            log.error("取消点赞失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "添加评论", description = "为动态添加评论")
    @PostMapping("/{momentId}/comments")
    public Result<Long> addComment(
            @PathVariable Long momentId,
            @Valid @RequestBody CommentCreateDTO createDTO) {
        try {
            Long userId = UserContextHolder.getUserId();
            Long commentId = momentService.addComment(momentId, createDTO, userId);
            log.info("添加评论成功: userId={}, momentId={}, commentId={}", userId, momentId, commentId);
            return Result.success(commentId);
        } catch (Exception e) {
            log.error("添加评论失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取评论列表", description = "分页获取动态的评论列表")
    @GetMapping("/{momentId}/comments")
    public Result<Page<CommentVO>> getComments(
            @PathVariable Long momentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Page<CommentVO> comments = momentService.getComments(momentId, page, pageSize);
            return Result.success(comments);
        } catch (Exception e) {
            log.error("获取评论列表失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取评论回复", description = "分页获取评论的回复列表")
    @GetMapping("/comments/{commentId}/replies")
    public Result<Page<CommentVO>> getCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Page<CommentVO> replies = ((com.basebackend.wheel.service.impl.MomentServiceImpl) momentService)
                    .getCommentReplies(commentId, page, pageSize);
            return Result.success(replies);
        } catch (Exception e) {
            log.error("获取评论回复失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "删除评论", description = "删除指定的评论")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean success = momentService.deleteComment(commentId, userId);
            if (success) {
                log.info("删除评论成功: userId={}, commentId={}", userId, commentId);
                return Result.success();
            } else {
                return Result.error();
            }
        } catch (Exception e) {
            log.error("删除评论失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取可提及用户列表", description = "获取评论时可以@的用户列表（情侣关系中的用户）")
    @GetMapping("/mentionable-users")
    public Result<List<UserBriefVO>> getMentionableUsers() {
        try {
            Long userId = UserContextHolder.getUserId();
            List<UserBriefVO> users = momentService.getMentionableUsers(userId);
            return Result.success(users);
        } catch (Exception e) {
            log.error("获取可提及用户列表失败: error={}", e.getMessage());
            throw e;
        }
    }
}
