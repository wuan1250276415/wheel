package com.basebackend.wheel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.dto.CommentCreateDTO;
import com.basebackend.wheel.dto.CommentVO;
import com.basebackend.wheel.dto.MomentCreateDTO;
import com.basebackend.wheel.dto.MomentUpdateDTO;
import com.basebackend.wheel.dto.MomentVO;
import com.basebackend.wheel.dto.UserBriefVO;

import java.util.List;

/**
 * 动态服务接口
 */
public interface MomentService {

    /**
     * 创建动态
     *
     * @param createDTO 创建请求
     * @param userId 用户ID
     * @return 动态ID
     */
    Long createMoment(MomentCreateDTO createDTO, Long userId);

    /**
     * 获取动态详情
     *
     * @param momentId 动态ID
     * @param userId 当前用户ID
     * @return 动态详情
     */
    MomentVO getMomentDetail(Long momentId, Long userId);

    /**
     * 获取动态时间线（分页）
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param userId 当前用户ID
     * @return 动态列表
     */
    Page<MomentVO> getMomentTimeline(Integer page, Integer pageSize, Long userId);

    /**
     * 更新动态
     *
     * @param momentId 动态ID
     * @param updateDTO 更新请求
     * @param userId 当前用户ID
     * @return 是否成功
     */
    boolean updateMoment(Long momentId, MomentUpdateDTO updateDTO, Long userId);

    /**
     * 删除动态
     *
     * @param momentId 动态ID
     * @param userId 当前用户ID
     * @return 是否成功
     */
    boolean deleteMoment(Long momentId, Long userId);

    /**
     * 点赞动态
     *
     * @param momentId 动态ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean likeMoment(Long momentId, Long userId);

    /**
     * 取消点赞
     *
     * @param momentId 动态ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unlikeMoment(Long momentId, Long userId);

    /**
     * 添加评论
     *
     * @param momentId 动态ID
     * @param createDTO 评论内容
     * @param userId 用户ID
     * @return 评论ID
     */
    Long addComment(Long momentId, CommentCreateDTO createDTO, Long userId);

    /**
     * 获取评论列表（分页）
     *
     * @param momentId 动态ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 评论列表
     */
    Page<CommentVO> getComments(Long momentId, Integer page, Integer pageSize);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * 获取可提及用户列表（情侣关系中的用户）
     *
     * @param userId 当前用户ID
     * @return 用户列表
     */
    List<UserBriefVO> getMentionableUsers(Long userId);
}
