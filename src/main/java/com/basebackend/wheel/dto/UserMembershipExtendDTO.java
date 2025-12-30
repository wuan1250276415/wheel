package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserMembershipExtendDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer days;
    private String reason;
}
