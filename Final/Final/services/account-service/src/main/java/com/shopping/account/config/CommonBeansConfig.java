package com.shopping.account.config;

import com.shopping.common.security.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonBeansConfig {

    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        JwtTokenUtil util = new JwtTokenUtil();
        return util;
    }
}