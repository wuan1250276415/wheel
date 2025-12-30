package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.MembershipPlan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MembershipPlanMapper extends BaseMapper<MembershipPlan> {

    List<MembershipPlan> selectEnabledPlans();

    MembershipPlan selectByPlanKey(String planKey);
}
