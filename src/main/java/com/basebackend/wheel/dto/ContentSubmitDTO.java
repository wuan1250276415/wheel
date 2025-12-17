package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 内容提交DTO
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
public class ContentSubmitDTO {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 内容文本
     */
    @NotBlank(message = "内容文本不能为空")
    @Size(max = 200, message = "内容文本长度不能超过200字符")
    private String contentText;

    /**
     * 权重值
     */
    @NotNull(message = "权重值不能为空")
    private Double weight;

    /**
     * 标签列表
     */
    private List<String> tags;
}
