package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MembershipPlanQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer tier;
    private Integer status;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
