package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.ThemeVO;
import com.basebackend.wheel.entity.UserTheme;
import com.basebackend.wheel.entity.WheelTheme;
import com.basebackend.wheel.mapper.UserThemeMapper;
import com.basebackend.wheel.mapper.WheelThemeMapper;
import com.basebackend.wheel.service.ThemeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 主题服务实现
 */
@Slf4j
@Service
public class ThemeServiceImpl implements ThemeService {

    @Autowired
    private WheelThemeMapper wheelThemeMapper;

    @Autowired
    private UserThemeMapper userThemeMapper;

    @Autowired
    private com.basebackend.wheel.util.MembershipPrivilegeHelper membershipPrivilegeHelper;

    @Override
    public List<ThemeVO> getAllThemes(Long userId) {
        List<WheelTheme> themes = wheelThemeMapper.selectEnabledThemes();

        // 根据用户会员等级过滤主题
        Integer userTier = membershipPrivilegeHelper.getUserTier(userId);
        themes = themes.stream()
                .filter(theme -> theme.getRequiredTier() == null
                        || theme.getRequiredTier() == 0
                        || userTier >= theme.getRequiredTier())
                .collect(Collectors.toList());

        List<UserTheme> userThemes = userThemeMapper.selectByUserId(userId);
        Map<Long, UserTheme> userThemeMap = userThemes.stream()
                .collect(Collectors.toMap(UserTheme::getThemeId, ut -> ut));

        return themes.stream().map(theme -> {
            ThemeVO vo = new ThemeVO();
            BeanUtils.copyProperties(theme, vo);

            UserTheme userTheme = userThemeMap.get(theme.getId());
            vo.setOwned(userTheme != null || theme.getPrice() == 0);
            vo.setActive(userTheme != null && userTheme.getIsActive());

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ThemeVO getCurrentTheme(Long userId) {
        UserTheme activeUserTheme = userThemeMapper.selectActiveTheme(userId);

        WheelTheme theme;
        if (activeUserTheme != null) {
            theme = wheelThemeMapper.selectById(activeUserTheme.getThemeId());
        } else {
            theme = wheelThemeMapper.selectDefaultTheme();
        }

        if (theme == null) {
            throw new RuntimeException("未找到可用主题");
        }

        ThemeVO vo = new ThemeVO();
        BeanUtils.copyProperties(theme, vo);
        vo.setOwned(true);
        vo.setActive(true);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purchaseTheme(Long userId, Long themeId) {
        WheelTheme theme = wheelThemeMapper.selectById(themeId);
        if (theme == null || theme.getStatus() != 1) {
            throw new RuntimeException("主题不存在或已禁用");
        }

        // 检查会员等级是否满足要求
        if (!membershipPrivilegeHelper.canAccessTheme(userId, theme.getRequiredTier())) {
            throw new RuntimeException("该主题需要更高的会员等级");
        }

        UserTheme existingUserTheme = userThemeMapper.selectByUserIdAndThemeId(userId, themeId);
        if (existingUserTheme != null) {
            throw new RuntimeException("您已拥有该主题");
        }

        if (theme.getPrice() > 0) {
            log.info("用户购买主题: userId={}, themeId={}, price={}", userId, themeId, theme.getPrice());
        }

        UserTheme userTheme = new UserTheme();
        userTheme.setUserId(userId);
        userTheme.setThemeId(themeId);
        userTheme.setIsActive(false);
        userTheme.setPurchasedAt(new Date());
        userTheme.setPurchaseType(theme.getPrice() == 0 ? 0 : 1);
        userTheme.setCreateBy(userId);
        userTheme.setUpdateBy(userId);

        userThemeMapper.insert(userTheme);
        log.info("主题购买成功: userId={}, themeId={}", userId, themeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyTheme(Long userId, Long themeId) {
        WheelTheme theme = wheelThemeMapper.selectById(themeId);
        if (theme == null || theme.getStatus() != 1) {
            throw new RuntimeException("主题不存在或已禁用");
        }

        // 检查会员等级是否满足要求
        if (!membershipPrivilegeHelper.canAccessTheme(userId, theme.getRequiredTier())) {
            throw new RuntimeException("该主题需要更高的会员等级");
        }

        UserTheme userTheme = userThemeMapper.selectByUserIdAndThemeId(userId, themeId);
        if (userTheme == null && theme.getPrice() > 0) {
            throw new RuntimeException("您尚未拥有该主题，请先购买");
        }

        userThemeMapper.deactivateAllThemes(userId);

        if (userTheme == null) {
            UserTheme newUserTheme = new UserTheme();
            newUserTheme.setUserId(userId);
            newUserTheme.setThemeId(themeId);
            newUserTheme.setIsActive(true);
            newUserTheme.setPurchasedAt(new Date());
            newUserTheme.setPurchaseType(0);
            newUserTheme.setCreateBy(userId);
            newUserTheme.setUpdateBy(userId);
            userThemeMapper.insert(newUserTheme);
        } else {
            userTheme.setIsActive(true);
            userTheme.setUpdateBy(userId);
            userThemeMapper.updateById(userTheme);
        }

        log.info("主题应用成功: userId={}, themeId={}", userId, themeId);
    }

    @Override
    public List<Long> getOwnedThemeIds(Long userId) {
        List<UserTheme> userThemes = userThemeMapper.selectByUserId(userId);

        List<Long> ownedIds = userThemes.stream()
                .map(UserTheme::getThemeId)
                .collect(Collectors.toList());

        List<WheelTheme> freeThemes = wheelThemeMapper.selectEnabledThemes().stream()
                .filter(theme -> theme.getPrice() == 0)
                .collect(Collectors.toList());

        freeThemes.forEach(theme -> {
            if (!ownedIds.contains(theme.getId())) {
                ownedIds.add(theme.getId());
            }
        });

        return ownedIds;
    }
}
