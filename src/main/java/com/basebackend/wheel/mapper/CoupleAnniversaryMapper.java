package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.CoupleAnniversary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 情侣纪念日Mapper
 *
 * @author wheel-api
 */
@Mapper
public interface CoupleAnniversaryMapper extends BaseMapper<CoupleAnniversary> {

    /**
     * 根据情侣ID查询所有纪念日
     *
     * @param coupleId 情侣关系ID
     * @return 纪念日列表
     */
    List<CoupleAnniversary> selectByCoupleId(@Param("coupleId") Long coupleId);

    /**
     * 查询即将到来的纪念日（在提醒期内）
     *
     * @param coupleId    情侣关系ID
     * @param currentDate 当前日期
     * @return 即将到来的纪念日列表
     */
    List<CoupleAnniversary> selectUpcoming(
            @Param("coupleId") Long coupleId,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * 根据纪念日ID和情侣ID查询（用于权限验证）
     *
     * @param anniversaryId 纪念日ID
     * @param coupleId      情侣关系ID
     * @return 纪念日
     */
    CoupleAnniversary selectByIdAndCoupleId(
            @Param("anniversaryId") Long anniversaryId,
            @Param("coupleId") Long coupleId
    );

    /**
     * 根据情侣ID和纪念日类型查询
     *
     * @param coupleId        情侣关系ID
     * @param anniversaryType 纪念日类型
     * @return 纪念日
     */
    CoupleAnniversary selectByTypeAndCoupleId(
            @Param("coupleId") Long coupleId,
            @Param("anniversaryType") Integer anniversaryType
    );

    /**
     * 查询指定日期的纪念日
     *
     * @param coupleId        情侣关系ID
     * @param anniversaryDate 纪念日日期
     * @return 纪念日列表
     */
    List<CoupleAnniversary> selectByDate(
            @Param("coupleId") Long coupleId,
            @Param("anniversaryDate") LocalDate anniversaryDate
    );
}
