package com.shopping.account.config;

import com.shopping.account.service.CustomUserDetailsService;
import com.shopping.common.security.JwtTokenUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    // 新增：模拟 JwtTokenUtil（解决过滤器第一个依赖）
    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return org.mockito.Mockito.mock(JwtTokenUtil.class);
    }

    // 新增：模拟 CustomUserDetailsService（解决过滤器第二个依赖）
    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return org.mockito.Mockito.mock(CustomUserDetailsService.class);
    }

    // 保留原有的安全过滤链配置（关键，不能删）
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .anonymous().and()
                .servletApi().and()
                .securityContext().disable()
                .sessionManagement().disable()
                .requestCache().disable()
                .headers().disable();
        return http.build();
    }
}
