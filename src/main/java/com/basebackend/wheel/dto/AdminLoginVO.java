package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class AdminLoginVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String refreshToken;
    private AdminInfoVO adminInfo;
    private Set<String> permissions;
    private Set<String> roles;
}
