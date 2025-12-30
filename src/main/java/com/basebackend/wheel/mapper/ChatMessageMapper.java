package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    /**
     * 根据情侣ID查询聊天记录（分页）
     *
     * @param coupleId 情侣关系ID
     * @param offset   偏移量
     * @param limit    限制数量
     * @return 聊天记录列表
     */
    List<ChatMessage> selectByCoupleId(@Param("coupleId") Long coupleId,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    /**
     * 查询未读消息数量
     *
     * @param receiverId 接收者ID
     * @return 未读消息数量
     */
    Integer countUnreadMessages(@Param("receiverId") Long receiverId);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param receiverId 接收者ID（校验权限）
     * @return 影响行数
     */
    int markAsRead(@Param("messageId") Long messageId, @Param("receiverId") Long receiverId);

    /**
     * 批量标记消息为已读
     *
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @return 影响行数
     */
    int batchMarkAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}
