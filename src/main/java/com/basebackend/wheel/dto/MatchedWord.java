package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 匹配到的敏感词
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedWord {

    /**
     * 敏感词
     */
    private String word;

    /**
     * 分类：1-色情 2-暴力 3-政治 4-广告 5-其他
     */
    private Integer category;

    /**
     * 风险等级：1-低 2-中 3-高
     */
    private Integer level;

    /**
     * 起始位置
     */
    private Integer startIndex;

    /**
     * 结束位置
     */
    private Integer endIndex;
}
