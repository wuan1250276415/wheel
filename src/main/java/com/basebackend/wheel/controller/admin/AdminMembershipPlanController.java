package com.basebackend.wheel.controller.admin;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.common.model.Result;
import com.basebackend.security.annotation.RequiresPermission;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.MembershipPlan;
import com.basebackend.wheel.enums.MembershipTier;
import com.basebackend.wheel.mapper.MembershipPlanMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin/membership/plans")
@Tag(name = "管理端-会员套餐管理", description = "管理员管理会员套餐")
public class AdminMembershipPlanController {

    @Autowired
    private MembershipPlanMapper membershipPlanMapper;

    @Operation(summary = "分页查询套餐列表")
    @GetMapping
    @RequiresPermission("membership:plan:view")
    public Result<IPage<MembershipPlanAdminVO>> queryPlans(MembershipPlanQueryDTO queryDTO) {
        try {
            Page<MembershipPlan> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

            LambdaQueryWrapper<MembershipPlan> wrapper = new LambdaQueryWrapper<>();

            if (queryDTO.getTier() != null) {
                wrapper.eq(MembershipPlan::getTier, queryDTO.getTier());
            }

            if (queryDTO.getStatus() != null) {
                wrapper.eq(MembershipPlan::getStatus, queryDTO.getStatus());
            }

            if (StringUtils.hasText(queryDTO.getKeyword())) {
                wrapper.and(w -> w.like(MembershipPlan::getPlanName, queryDTO.getKeyword())
                    .or().like(MembershipPlan::getPlanKey, queryDTO.getKeyword()));
            }

            wrapper.orderByAsc(MembershipPlan::getSortOrder);

            IPage<MembershipPlan> planPage = membershipPlanMapper.selectPage(page, wrapper);

            IPage<MembershipPlanAdminVO> result = planPage.convert(plan -> {
                MembershipPlanAdminVO vo = new MembershipPlanAdminVO();
                BeanUtils.copyProperties(plan, vo);
                vo.setTierName(MembershipTier.fromCode(plan.getTier()).getDescription());

                if (plan.getBenefits() != null) {
                    vo.setBenefits(JSON.parseObject(plan.getBenefits(), Map.class));
                }

                return vo;
            });

            return Result.success(result);
        } catch (Exception e) {
            log.error("查询套餐列表失败: error={}", e.getMessage(), e);
            return Result.error(500, "查询套餐列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取套餐详情")
    @GetMapping("/{id}")
    @RequiresPermission("membership:plan:view")
    public Result<MembershipPlanAdminVO> getPlanDetail(@PathVariable Long id) {
        try {
            MembershipPlan plan = membershipPlanMapper.selectById(id);

            if (plan == null) {
                return Result.error(404, "套餐不存在");
            }

            MembershipPlanAdminVO vo = new MembershipPlanAdminVO();
            BeanUtils.copyProperties(plan, vo);
            vo.setTierName(MembershipTier.fromCode(plan.getTier()).getDescription());

            if (plan.getBenefits() != null) {
                vo.setBenefits(JSON.parseObject(plan.getBenefits(), Map.class));
            }

            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取套餐详情失败: id={}, error={}", id, e.getMessage(), e);
            return Result.error(500, "获取套餐详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建套餐")
    @PostMapping
    @RequiresPermission("membership:plan:create")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> createPlan(@RequestBody MembershipPlanCreateDTO createDTO) {
        try {
            MembershipPlan existingPlan = membershipPlanMapper.selectByPlanKey(createDTO.getPlanKey());
            if (existingPlan != null) {
                return Result.error(400, "套餐标识已存在");
            }

            MembershipPlan plan = new MembershipPlan();
            BeanUtils.copyProperties(createDTO, plan);
            plan.setStatus(1);

            membershipPlanMapper.insert(plan);

            log.info("创建套餐成功: planId={}, planKey={}", plan.getId(), plan.getPlanKey());
            return Result.success(plan.getId());
        } catch (Exception e) {
            log.error("创建套餐失败: planKey={}, error={}", createDTO.getPlanKey(), e.getMessage(), e);
            return Result.error(500, "创建套餐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新套餐")
    @PutMapping("/{id}")
    @RequiresPermission("membership:plan:update")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updatePlan(@PathVariable Long id, @RequestBody MembershipPlanUpdateDTO updateDTO) {
        try {
            MembershipPlan plan = membershipPlanMapper.selectById(id);

            if (plan == null) {
                return Result.error(404, "套餐不存在");
            }

            BeanUtils.copyProperties(updateDTO, plan);
            membershipPlanMapper.updateById(plan);

            log.info("更新套餐成功: planId={}", id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新套餐失败: planId={}, error={}", id, e.getMessage(), e);
            return Result.error(500, "更新套餐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    @RequiresPermission("membership:plan:delete")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deletePlan(@PathVariable Long id) {
        try {
            MembershipPlan plan = membershipPlanMapper.selectById(id);

            if (plan == null) {
                return Result.error(404, "套餐不存在");
            }

            membershipPlanMapper.deleteById(id);

            log.info("删除套餐成功: planId={}", id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除套餐失败: planId={}, error={}", id, e.getMessage(), e);
            return Result.error(500, "删除套餐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "启用/禁用套餐")
    @PutMapping("/{id}/status")
    @RequiresPermission("membership:plan:update")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updatePlanStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            MembershipPlan plan = membershipPlanMapper.selectById(id);

            if (plan == null) {
                return Result.error(404, "套餐不存在");
            }

            plan.setStatus(status);
            membershipPlanMapper.updateById(plan);

            log.info("更新套餐状态成功: planId={}, status={}", id, status);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新套餐状态失败: planId={}, error={}", id, e.getMessage(), e);
            return Result.error(500, "更新套餐状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "修改套餐排序")
    @PutMapping("/{id}/sort")
    @RequiresPermission("membership:plan:update")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updatePlanSort(@PathVariable Long id, @RequestParam Integer sortOrder) {
        try {
            MembershipPlan plan = membershipPlanMapper.selectById(id);

            if (plan == null) {
                return Result.error(404, "套餐不存在");
            }

            plan.setSortOrder(sortOrder);
            membershipPlanMapper.updateById(plan);

            log.info("更新套餐排序成功: planId={}, sortOrder={}", id, sortOrder);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新套餐排序失败: planId={}, error={}", id, e.getMessage(), e);
            return Result.error(500, "更新套餐排序失败: " + e.getMessage());
        }
    }
}
