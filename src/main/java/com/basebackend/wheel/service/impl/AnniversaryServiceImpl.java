package com.basebackend.wheel.service.impl;

import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.AnniversaryCreateDTO;
import com.basebackend.wheel.dto.AnniversaryUpdateDTO;
import com.basebackend.wheel.dto.AnniversaryVO;
import com.basebackend.wheel.entity.CoupleAnniversary;
import com.basebackend.wheel.entity.CoupleRelationship;
import com.basebackend.wheel.enums.AnniversaryType;
import com.basebackend.wheel.mapper.CoupleAnniversaryMapper;
import com.basebackend.wheel.mapper.CoupleRelationshipMapper;
import com.basebackend.wheel.service.AnniversaryService;
import com.basebackend.wheel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 纪念日服务实现类
 *
 * @author wheel-api
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnniversaryServiceImpl implements AnniversaryService {

    private final CoupleAnniversaryMapper coupleAnniversaryMapper;
    private final CoupleRelationshipMapper coupleRelationshipMapper;

    /**
     * 默认提醒天数
     */
    private static final int DEFAULT_REMIND_DAYS = 7;

    /**
     * 情侣关系状态：已确认
     */
    private static final int RELATIONSHIP_STATUS_CONFIRMED = 1;

    @Override
    @Transactional
    public void createDefaultAnniversary(Long coupleId, LocalDate confirmedDate) {
        log.info("创建默认纪念日: coupleId={}, confirmedDate={}", coupleId, confirmedDate);

        // 检查是否已存在恋爱纪念日
        CoupleAnniversary existing = coupleAnniversaryMapper.selectByTypeAndCoupleId(
                coupleId, AnniversaryType.RELATIONSHIP.getCode());
        if (existing != null) {
            log.info("恋爱纪念日已存在，跳过创建: coupleId={}", coupleId);
            return;
        }

        // 创建恋爱纪念日
        CoupleAnniversary anniversary = new CoupleAnniversary();
        anniversary.setCoupleId(coupleId);
        anniversary.setAnniversaryType(AnniversaryType.RELATIONSHIP.getCode());
        anniversary.setAnniversaryDate(confirmedDate);
        anniversary.setName(AnniversaryType.RELATIONSHIP.getName());
        anniversary.setRemindDays(DEFAULT_REMIND_DAYS);
        anniversary.setCreatedBy(null); // 系统自动创建

        AuditHelper.setCreateAuditFields(anniversary, null);
        coupleAnniversaryMapper.insert(anniversary);

        log.info("默认纪念日创建成功: anniversaryId={}, coupleId={}", anniversary.getId(), coupleId);
    }


    @Override
    @Transactional
    public CoupleAnniversary addAnniversary(Long userId, AnniversaryCreateDTO dto) {
        log.info("添加纪念日: userId={}, dto={}", userId, dto);

        // 1. 验证情侣关系
        Long coupleId = validateCoupleRelationship(userId);

        // 2. 验证纪念日类型
        validateAnniversaryType(dto.getAnniversaryType());

        // 3. 设置默认提醒天数
        if (dto.getRemindDays() == null) {
            dto.setRemindDays(DEFAULT_REMIND_DAYS);
        }

        // 4. 验证提醒天数范围（1-30天）
        if (dto.getRemindDays() < 1 || dto.getRemindDays() > 30) {
            throw new BusinessException("提醒天数必须在1-30天之间");
        }

        // 5. 创建纪念日实体
        CoupleAnniversary anniversary = new CoupleAnniversary();
        anniversary.setCoupleId(coupleId);
        anniversary.setAnniversaryType(dto.getAnniversaryType());
        anniversary.setAnniversaryDate(dto.getAnniversaryDate());
        anniversary.setName(dto.getName());
        anniversary.setRemindDays(dto.getRemindDays());
        anniversary.setCreatedBy(userId);

        AuditHelper.setCreateAuditFields(anniversary, userId);
        coupleAnniversaryMapper.insert(anniversary);

        log.info("纪念日添加成功: anniversaryId={}, coupleId={}", anniversary.getId(), coupleId);
        return anniversary;
    }

    @Override
    @Transactional
    public CoupleAnniversary updateAnniversary(Long userId, Long anniversaryId, AnniversaryUpdateDTO dto) {
        log.info("更新纪念日: userId={}, anniversaryId={}, dto={}", userId, anniversaryId, dto);

        // 1. 验证情侣关系
        Long coupleId = validateCoupleRelationship(userId);

        // 2. 查询纪念日（同时验证权限）
        CoupleAnniversary anniversary = coupleAnniversaryMapper.selectByIdAndCoupleId(anniversaryId, coupleId);
        if (anniversary == null) {
            throw new BusinessException("纪念日不存在");
        }

        // 3. 更新字段（只更新非空字段）
        if (dto.getAnniversaryType() != null) {
            validateAnniversaryType(dto.getAnniversaryType());
            anniversary.setAnniversaryType(dto.getAnniversaryType());
        }
        if (dto.getAnniversaryDate() != null) {
            anniversary.setAnniversaryDate(dto.getAnniversaryDate());
        }
        if (dto.getName() != null) {
            anniversary.setName(dto.getName());
        }
        if (dto.getRemindDays() != null) {
            if (dto.getRemindDays() < 1 || dto.getRemindDays() > 30) {
                throw new BusinessException("提醒天数必须在1-30天之间");
            }
            anniversary.setRemindDays(dto.getRemindDays());
        }

        AuditHelper.setUpdateAuditFields(anniversary, userId);
        coupleAnniversaryMapper.updateById(anniversary);

        log.info("纪念日更新成功: anniversaryId={}", anniversaryId);
        return anniversary;
    }

    @Override
    @Transactional
    public void deleteAnniversary(Long userId, Long anniversaryId) {
        log.info("删除纪念日: userId={}, anniversaryId={}", userId, anniversaryId);

        // 1. 验证情侣关系
        Long coupleId = validateCoupleRelationship(userId);

        // 2. 查询纪念日（同时验证权限）
        CoupleAnniversary anniversary = coupleAnniversaryMapper.selectByIdAndCoupleId(anniversaryId, coupleId);
        if (anniversary == null) {
            throw new BusinessException("纪念日不存在");
        }

        // 3. 逻辑删除
        anniversary.setDeleted(1);
        AuditHelper.setUpdateAuditFields(anniversary, userId);
        coupleAnniversaryMapper.updateById(anniversary);

        log.info("纪念日删除成功: anniversaryId={}", anniversaryId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AnniversaryVO> getAnniversaries(Long userId) {
        log.info("获取纪念日列表: userId={}", userId);

        // 1. 验证情侣关系
        Long coupleId = validateCoupleRelationship(userId);

        // 2. 查询所有纪念日
        List<CoupleAnniversary> anniversaries = coupleAnniversaryMapper.selectByCoupleId(coupleId);

        // 3. 转换为VO
        return anniversaries.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnniversaryVO> getUpcomingAnniversaries(Long coupleId) {
        log.info("获取即将到来的纪念日: coupleId={}", coupleId);

        // 查询即将到来的纪念日
        LocalDate currentDate = LocalDate.now();
        List<CoupleAnniversary> anniversaries = coupleAnniversaryMapper.selectUpcoming(coupleId, currentDate);

        // 转换为VO
        return anniversaries.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证情侣关系
     *
     * @param userId 用户ID
     * @return 情侣关系ID
     */
    private Long validateCoupleRelationship(Long userId) {
        CoupleRelationship relationship = coupleRelationshipMapper.selectConfirmedByUserId(userId);
        if (relationship == null) {
            throw new BusinessException("您还没有绑定情侣关系");
        }
        return relationship.getId();
    }

    /**
     * 验证纪念日类型
     *
     * @param anniversaryType 纪念日类型
     */
    private void validateAnniversaryType(Integer anniversaryType) {
        if (anniversaryType == null) {
            throw new BusinessException("纪念日类型不能为空");
        }
        try {
            AnniversaryType.fromCode(anniversaryType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的纪念日类型");
        }
    }

    /**
     * 将实体转换为VO
     *
     * @param anniversary 纪念日实体
     * @return 纪念日VO
     */
    private AnniversaryVO convertToVO(CoupleAnniversary anniversary) {
        if (anniversary == null) {
            return null;
        }

        AnniversaryVO vo = new AnniversaryVO();
        vo.setId(anniversary.getId());
        vo.setAnniversaryType(anniversary.getAnniversaryType());
        vo.setAnniversaryTypeName(getAnniversaryTypeName(anniversary.getAnniversaryType()));
        vo.setAnniversaryDate(anniversary.getAnniversaryDate());
        vo.setName(anniversary.getName());
        vo.setRemindDays(anniversary.getRemindDays());
        vo.setDaysUntil(calculateDaysUntil(anniversary.getAnniversaryDate()));

        return vo;
    }

    /**
     * 获取纪念日类型名称
     *
     * @param anniversaryType 纪念日类型
     * @return 类型名称
     */
    private String getAnniversaryTypeName(Integer anniversaryType) {
        if (anniversaryType == null) {
            return "未知";
        }
        try {
            return AnniversaryType.fromCode(anniversaryType).getName();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }

    /**
     * 计算距离纪念日还有多少天
     * 如果今年的纪念日已过，则计算到明年的纪念日
     *
     * @param anniversaryDate 纪念日日期
     * @return 距离天数
     */
    private Long calculateDaysUntil(LocalDate anniversaryDate) {
        if (anniversaryDate == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        
        // 计算今年的纪念日
        LocalDate thisYearAnniversary = anniversaryDate.withYear(today.getYear());
        
        // 如果今年的纪念日已过，计算明年的
        if (thisYearAnniversary.isBefore(today)) {
            thisYearAnniversary = thisYearAnniversary.plusYears(1);
        }

        return ChronoUnit.DAYS.between(today, thisYearAnniversary);
    }
}
