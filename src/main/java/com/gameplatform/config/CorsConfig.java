package com.gameplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:07
 * @description TODO
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)  // 确保CORS配置最先执行
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许cookies跨域
        config.setAllowCredentials(true);

        // 允许的源，在生产环境中应该只设置特定的域名
        // 开发环境允许前端开发服务器地址
        config.addAllowedOriginPattern("http://localhost:8081");  // 前端开发服务器
        config.addAllowedOriginPattern("http://localhost:*");     // 本地开发其他端口

        // 允许的HTTP方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");

        // 允许的头信息
        config.addAllowedHeader("*");

        // 特别允许的头信息
        config.addExposedHeader("Authorization");
        config.addExposedHeader("X-Total-Count");
        config.addExposedHeader("Link");
        config.addExposedHeader("Access-Control-Allow-Origin");
        config.addExposedHeader("Access-Control-Allow-Credentials");

        // 预检请求的有效期，单位为秒
        config.setMaxAge(3600L);

        // 配置所有接口都适用此CORS配置
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/ws/**", config);  // WebSocket端点

        return new CorsFilter(source);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("http://localhost:8081", "http://localhost:*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization", "X-Total-Count", "Link")
                        .allowCredentials(true)
                        .maxAge(3600);

                // WebSocket端点的CORS配置
                registry.addMapping("/ws/**")
                        .allowedOriginPatterns("http://localhost:8081", "http://localhost:*")
                        .allowedMethods("GET", "POST")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}