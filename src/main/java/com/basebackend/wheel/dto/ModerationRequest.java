package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容审核请求
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationRequest {

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文本内容
     */
    private String contentText;

    /**
     * 图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 内容类型：1-动态 2-评论 3-聊天消息 4-其他
     */
    private Integer contentType;

    /**
     * 用户IP地址
     */
    private String userIp;

    /**
     * 用户设备ID
     */
    private String deviceId;

    /**
     * 是否为VIP用户
     */
    private Boolean vipUser;

    /**
     * 会员等级：1-VIP 2-SVIP
     */
    private Integer membershipTier;

    /**
     * 内容类型常量
     */
    public static final int CONTENT_TYPE_MOMENT = 1;
    public static final int CONTENT_TYPE_COMMENT = 2;
    public static final int CONTENT_TYPE_CHAT = 3;
    public static final int CONTENT_TYPE_OTHER = 4;

    /**
     * 检查是否有文本内容
     */
    public boolean hasText() {
        return contentText != null && !contentText.trim().isEmpty();
    }

    /**
     * 检查是否有图片内容
     */
    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    /**
     * 检查是否为VIP用户
     */
    public boolean isVip() {
        return Boolean.TRUE.equals(vipUser) || (membershipTier != null && membershipTier >= 1);
    }

    /**
     * 检查是否为SVIP用户
     */
    public boolean isSvip() {
        return membershipTier != null && membershipTier >= 2;
    }
}
