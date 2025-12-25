package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.CoupleRelationship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CoupleRelationshipMapper extends BaseMapper<CoupleRelationship> {
    /**
     * 根据用户ID查询情侣关系
     *
     * @param userId 用户ID
     * @return 关系列表
     */
    List<CoupleRelationship> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据双方用户ID查询关系
     *
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 关系信息
     */
    CoupleRelationship selectByUserIds(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    /**
     * 根据邀请码查询关系
     *
     * @param inviteCode 邀请码
     * @return 关系信息
     */
    CoupleRelationship selectByInviteCode(@Param("inviteCode") String inviteCode);

    /**
     * 查询已确认的关系
     *
     * @param userId 用户ID
     * @return 已确认的关系
     */
    CoupleRelationship selectConfirmedByUserId(@Param("userId") Long userId);
}