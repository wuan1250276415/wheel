package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * 分享图片结果VO
 */
@Data
public class ShareImageVO {

    /**
     * Base64编码的图片数据
     */
    private String imageBase64;

    /**
     * 使用的主题
     */
    private String theme;
}
