package com.basebackend.wheel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 内容申诉DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class ContentAppealDTO {

    /**
     * 内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 申诉理由
     */
    @NotBlank(message = "申诉理由不能为空")
    @Size(min = 10, max = 500, message = "申诉理由长度应在10-500字之间")
    private String appealReason;

    /**
     * 补充说明
     */
    @Size(max = 200, message = "补充说明不能超过200字")
    private String additionalInfo;

    /**
     * 证据截图URL列表
     */
    private List<String> evidenceUrls;
}
