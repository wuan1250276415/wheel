package com.basebackend.wheel.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 手动备份DTO
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
public class ManualBackupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 备份描述
     */
    @Size(max = 500, message = "备份描述最多500个字符")
    private String description;
}
