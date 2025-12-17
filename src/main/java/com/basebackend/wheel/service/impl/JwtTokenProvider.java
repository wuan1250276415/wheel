//package com.basebackend.wheel.service.impl;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//
///**
// * JWT令牌提供者
// *
// * @author wheel-api
// * @since 2025-12-16
// */
//@Slf4j
//@Component
//public class JwtTokenProvider {
//
//    @Value("${wheel.jwt.secret:mySecretKeyForWheelApi2025MustBeVeryLongToEnsureSecurity}")
//    private String jwtSecret;
//
//    @Value("${wheel.jwt.access-token-validity-in-seconds:1800}")
//    private long accessTokenValidityInSeconds; // 30分钟
//
//    @Value("${wheel.jwt.refresh-token-validity-in-seconds:604800}")
//    private long refreshTokenValidityInSeconds; // 7天
//
//    private SecretKey getSigningKey() {
//        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//    }
//
//    /**
//     * 生成访问令牌
//     *
//     * @param userId 用户ID
//     * @return JWT令牌
//     */
//    public String generateAccessToken(Long userId) {
//        Date now = new Date();
//        Date validity = new Date(now.getTime() + accessTokenValidityInSeconds * 1000);
//
//        return Jwts.builder()
//                .setSubject(userId.toString())
//                .claim("type", "access")
//                .setIssuedAt(now)
//                .setExpiration(validity)
//                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
//                .compact();
//    }
//
//    /**
//     * 生成刷新令牌
//     *
//     * @param userId 用户ID
//     * @return JWT令牌
//     */
//    public String generateRefreshToken(Long userId) {
//        Date now = new Date();
//        Date validity = new Date(now.getTime() + refreshTokenValidityInSeconds * 1000);
//
//        return Jwts.builder()
//                .setSubject(userId.toString())
//                .claim("type", "refresh")
//                .setIssuedAt(now)
//                .setExpiration(validity)
//                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
//                .compact();
//    }
//
//    /**
//     * 从令牌中获取用户ID
//     *
//     * @param token JWT令牌
//     * @return 用户ID
//     */
//    public Long getUserIdFromToken(String token) {
//        Claims claims = getClaims(token);
//        return Long.valueOf(claims.getSubject());
//    }
//
//    /**
//     * 从刷新令牌中获取用户ID
//     *
//     * @param refreshToken 刷新令牌
//     * @return 用户ID
//     */
//    public Long getUserIdFromRefreshToken(String refreshToken) {
//        Claims claims = getClaims(refreshToken);
//        String type = claims.get("type", String.class);
//        if (!"refresh".equals(type)) {
//            throw new RuntimeException("令牌类型不正确");
//        }
//        return Long.valueOf(claims.getSubject());
//    }
//
//    /**
//     * 验证访问令牌
//     *
//     * @param token JWT令牌
//     * @return 是否有效
//     */
//    public boolean validateAccessToken(String token) {
//        return validateToken(token, "access");
//    }
//
//    /**
//     * 验证刷新令牌
//     *
//     * @param refreshToken 刷新令牌
//     * @return 是否有效
//     */
//    public boolean validateRefreshToken(String refreshToken) {
//        return validateToken(refreshToken, "refresh");
//    }
//
//    /**
//     * 验证令牌
//     *
//     * @param token JWT令牌
//     * @param expectedType 期望的令牌类型
//     * @return 是否有效
//     */
//    private boolean validateToken(String token, String expectedType) {
//        try {
//            Claims claims = getClaims(token);
//            String type = claims.get("type", String.class);
//            return expectedType.equals(type);
//        } catch (JwtException | IllegalArgumentException e) {
//            log.warn("JWT令牌验证失败: {}, error: {}", expectedType, e.getMessage());
//            return false;
//        }
//    }
//
//    /**
//     * 解析Claims
//     *
//     * @param token JWT令牌
//     * @return Claims
//     */
//    private Claims getClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    /**
//     * 获取访问令牌有效期（秒）
//     *
//     * @return 有效期
//     */
//    public long getAccessTokenValidityInSeconds() {
//        return accessTokenValidityInSeconds;
//    }
//
//    /**
//     * 获取刷新令牌有效期（秒）
//     *
//     * @return 有效期
//     */
//    public long getRefreshTokenValidityInSeconds() {
//        return refreshTokenValidityInSeconds;
//    }
//}
