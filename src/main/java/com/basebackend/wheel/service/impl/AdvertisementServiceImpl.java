package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basebackend.wheel.dto.AdVO;
import com.basebackend.wheel.entity.AdImpression;
import com.basebackend.wheel.entity.AdPlacement;
import com.basebackend.wheel.entity.Advertisement;
import com.basebackend.wheel.enums.ImpressionType;
import com.basebackend.wheel.mapper.AdImpressionMapper;
import com.basebackend.wheel.mapper.AdPlacementMapper;
import com.basebackend.wheel.mapper.AdvertisementMapper;
import com.basebackend.wheel.service.AdvertisementService;
import com.basebackend.wheel.util.MembershipPrivilegeHelper;
import com.basebackend.wheel.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 广告服务实现
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Slf4j
@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    private AdvertisementMapper advertisementMapper;

    @Autowired
    private AdPlacementMapper adPlacementMapper;

    @Autowired
    private AdImpressionMapper adImpressionMapper;

    @Autowired
    private MembershipPrivilegeHelper membershipPrivilegeHelper;

    @Override
    public List<AdVO> getAdsForPlacement(Long userId, String placementKey) {
        // VIP用户去广告
        if (userId != null && membershipPrivilegeHelper.hasAdFree(userId)) {
            log.info("VIP用户无广告: userId={}, placementKey={}", userId, placementKey);
            return Collections.emptyList();
        }

        // 查询广告位配置
        LambdaQueryWrapper<AdPlacement> placementQuery = new LambdaQueryWrapper<>();
        placementQuery.eq(AdPlacement::getPlacementKey, placementKey)
                .eq(AdPlacement::getStatus, 1);
        AdPlacement placement = adPlacementMapper.selectOne(placementQuery);

        if (placement == null) {
            log.warn("广告位不存在或已禁用: placementKey={}", placementKey);
            return Collections.emptyList();
        }

        // 查询有效广告
        List<Advertisement> ads = advertisementMapper.selectActiveAdsByPlacement(
                placementKey,
                placement.getMaxAds()
        );

        // 转换为VO
        return ads.stream().map(ad -> {
            AdVO vo = new AdVO();
            vo.setId(ad.getId());
            vo.setAdType(ad.getAdType());
            vo.setContentUrl(ad.getContentUrl());
            vo.setContentText(ad.getContentText());
            vo.setLinkUrl(ad.getLinkUrl());
            vo.setPlacementKey(placementKey);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void recordImpression(Long adId, Long userId, String placementKey, ImpressionType impressionType) {
        try {
            AdImpression impression = new AdImpression();
            impression.setAdId(adId);
            impression.setUserId(userId);
            impression.setPlacementKey(placementKey);
            impression.setImpressionType(impressionType);

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                impression.setIpAddress(RequestUtils.getClientIp(request));
                impression.setUserAgent(request.getHeader("User-Agent"));
                impression.setDeviceType(RequestUtils.getDeviceType(request).getDescription());
            }

            adImpressionMapper.insert(impression);

            // 更新广告统计计数
            if (impressionType == ImpressionType.VIEW) {
                advertisementMapper.updateById(
                        advertisementMapper.selectById(adId)
                );
            } else if (impressionType == ImpressionType.CLICK) {
                Advertisement ad = advertisementMapper.selectById(adId);
                if (ad != null) {
                    ad.setClickCount((ad.getClickCount() == null ? 0 : ad.getClickCount()) + 1);
                    advertisementMapper.updateById(ad);
                }
            }

            log.info("记录广告展示: adId={}, userId={}, type={}", adId, userId, impressionType);
        } catch (Exception e) {
            log.error("记录广告展示失败: adId={}, error={}", adId, e.getMessage(), e);
        }
    }
}
