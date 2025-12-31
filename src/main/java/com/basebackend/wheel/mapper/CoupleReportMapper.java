package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.entity.CoupleReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 情侣报告Mapper
 *
 * @author wheel-api
 */
@Mapper
public interface CoupleReportMapper extends BaseMapper<CoupleReport> {

    /**
     * 分页查询情侣报告（支持类型和日期范围筛选）
     *
     * @param page      分页参数
     * @param coupleId  情侣关系ID
     * @param reportType 报告类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 分页结果
     */
    IPage<CoupleReport> selectPageByCondition(
            Page<CoupleReport> page,
            @Param("coupleId") Long coupleId,
            @Param("reportType") Integer reportType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据情侣ID和报告类型查询最新报告
     *
     * @param coupleId   情侣关系ID
     * @param reportType 报告类型
     * @return 最新报告
     */
    CoupleReport selectLatestByType(
            @Param("coupleId") Long coupleId,
            @Param("reportType") Integer reportType
    );

    /**
     * 根据情侣ID查询所有报告
     *
     * @param coupleId 情侣关系ID
     * @return 报告列表
     */
    List<CoupleReport> selectByCoupleId(@Param("coupleId") Long coupleId);

    /**
     * 根据报告ID和情侣ID查询报告（用于权限验证）
     *
     * @param reportId 报告ID
     * @param coupleId 情侣关系ID
     * @return 报告
     */
    CoupleReport selectByIdAndCoupleId(
            @Param("reportId") Long reportId,
            @Param("coupleId") Long coupleId
    );

    /**
     * 查询指定日期范围内是否已存在报告
     *
     * @param coupleId   情侣关系ID
     * @param reportType 报告类型
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 报告（如果存在）
     */
    CoupleReport selectByDateRange(
            @Param("coupleId") Long coupleId,
            @Param("reportType") Integer reportType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
