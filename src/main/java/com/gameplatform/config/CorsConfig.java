package com.gameplatform.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:07
 * @description TODO
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许cookies跨域
        config.setAllowCredentials(true);
        // 允许的源，在生产环境中应该只设置特定的域名
        config.addAllowedOriginPattern("*");
        // 允许的HTTP方法
        config.addAllowedMethod("*");
        // 允许的头信息
        config.addAllowedHeader("*");
        // 暴露的头信息
        config.addExposedHeader("Authorization");
        // 缓存时间
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
