package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * FAQ VO
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
public class FaqVO {
    /**
     * FAQ ID
     */
    private Long id;

    /**
     * 问题分类
     */
    private String category;

    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

    /**
     * 是否有帮助（用户已点击）
     */
    private Boolean isHelpful;
}
