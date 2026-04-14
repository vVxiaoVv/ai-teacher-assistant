package com.moke.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的源（支持开发和生产环境）
        config.addAllowedOrigin("http://localhost:5173"); // 前端开发服务器
        config.addAllowedOrigin("http://localhost:3000"); // 备用端口
        config.addAllowedOriginPattern("http://localhost:*"); // 允许所有localhost端口
        config.setAllowCredentials(true); // 允许携带Cookie
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}


