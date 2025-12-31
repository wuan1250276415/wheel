package com.basebackend.wheel.config;

import com.basebackend.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security 配置
 * 禁用 CSRF 并配置 JWT Filter
 */
@Configuration
@EnableWebSecurity
public class CustomSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean(name = "customSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（REST API 使用 Token 认证，不需要 CSRF）
            .csrf(AbstractHttpConfigurer::disable)
            // 无状态会话
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers(
                    new AntPathRequestMatcher("/api/wheel/categories"),
                    new AntPathRequestMatcher("/api/wheel/contents"),
                    // WebSocket 端点 (SockJS 握手需要 HTTP 请求)
                    new AntPathRequestMatcher("/ws/**"),
                    new AntPathRequestMatcher("/doc.html"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/webjars/**"),
                    new AntPathRequestMatcher("/actuator/health")
                ).permitAll()
                // 需要认证的接口（包括但不限于）:
                // - /api/report/** : 情侣报告相关接口，需要JWT认证，情侣关系验证在服务层处理
                // - /api/couple/** : 情侣关系管理接口
                // - /api/moment/** : 动态相关接口
                // - /api/chat/** : 聊天相关接口
                // - /api/recommendations/** : AI推荐相关接口，需要JWT认证
                //   - GET /api/recommendations/personal : 个性化推荐
                //   - GET /api/recommendations/couple : 情侣推荐
                //   - GET /api/recommendations/homepage : 首页推荐
                //   - POST /api/recommendations/feedback : 推荐反馈
                //   - POST /api/recommendations/survey : 偏好调查
                // 其他接口需要认证
                .anyRequest().authenticated())
            // 添加 JWT Filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
