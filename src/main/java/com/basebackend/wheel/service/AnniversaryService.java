package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.AnniversaryCreateDTO;
import com.basebackend.wheel.dto.AnniversaryUpdateDTO;
import com.basebackend.wheel.dto.AnniversaryVO;
import com.basebackend.wheel.entity.CoupleAnniversary;

import java.time.LocalDate;
import java.util.List;

/**
 * 纪念日服务接口
 *
 * @author wheel-api
 */
public interface AnniversaryService {

    /**
     * 创建默认纪念日（恋爱纪念日）
     * 在情侣关系确认时自动调用
     *
     * @param coupleId      情侣关系ID
     * @param confirmedDate 确认日期
     */
    void createDefaultAnniversary(Long coupleId, LocalDate confirmedDate);

    /**
     * 添加纪念日
     *
     * @param userId 当前用户ID
     * @param dto    创建参数
     * @return 创建的纪念日
     */
    CoupleAnniversary addAnniversary(Long userId, AnniversaryCreateDTO dto);

    /**
     * 更新纪念日
     *
     * @param userId        当前用户ID
     * @param anniversaryId 纪念日ID
     * @param dto           更新参数
     * @return 更新后的纪念日
     */
    CoupleAnniversary updateAnniversary(Long userId, Long anniversaryId, AnniversaryUpdateDTO dto);

    /**
     * 删除纪念日
     *
     * @param userId        当前用户ID
     * @param anniversaryId 纪念日ID
     */
    void deleteAnniversary(Long userId, Long anniversaryId);

    /**
     * 获取情侣的所有纪念日
     *
     * @param userId 当前用户ID
     * @return 纪念日列表
     */
    List<AnniversaryVO> getAnniversaries(Long userId);

    /**
     * 获取即将到来的纪念日（在提醒期内）
     *
     * @param coupleId 情侣关系ID
     * @return 即将到来的纪念日列表
     */
    List<AnniversaryVO> getUpcomingAnniversaries(Long coupleId);
}
