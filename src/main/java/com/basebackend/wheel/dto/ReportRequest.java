package com.basebackend.wheel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 举报请求DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class ReportRequest {

    /**
     * 内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 举报人ID
     */
    @NotNull(message = "举报人ID不能为空")
    private Long reporterId;

    /**
     * 举报类型：0-内容 1-用户 2-评论
     */
    private Integer reportType;

    /**
     * 举报原因：0-色情低俗，1-暴力血腥，2-政治敏感，3-广告骚扰，4-侵权内容，5-其他
     */
    @NotNull(message = "举报原因不能为空")
    private Integer reportReason;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 证据截图URL列表
     */
    private List<String> evidenceUrls;

    /**
     * 举报原因枚举
     */
    public static final int REASON_PORN = 0;           // 色情低俗
    public static final int REASON_VIOLENCE = 1;       // 暴力血腥
    public static final int REASON_POLITICAL = 2;      // 政治敏感
    public static final int REASON_AD = 3;             // 广告骚扰
    public static final int REASON_INFRINGEMENT = 4;   // 侵权内容
    public static final int REASON_OTHER = 5;          // 其他

    /**
     * 举报类型枚举
     */
    public static final int TYPE_CONTENT = 0;          // 内容举报
    public static final int TYPE_USER = 1;             // 用户举报
    public static final int TYPE_COMMENT = 2;          // 评论举报

    /**
     * 验证举报原因是否有效
     */
    public boolean isValidReason() {
        return reportReason != null && reportReason >= REASON_PORN && reportReason <= REASON_OTHER;
    }

    /**
     * 验证举报类型是否有效
     */
    public boolean isValidType() {
        return reportType == null || (reportType >= TYPE_CONTENT && reportType <= TYPE_COMMENT);
    }
}
