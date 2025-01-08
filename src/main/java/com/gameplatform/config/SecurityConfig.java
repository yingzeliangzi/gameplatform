package com.gameplatform.config;

import com.gameplatform.security.JwtAuthenticationFilter;
import com.gameplatform.security.JwtAuthenticationProvider;
import com.gameplatform.security.CustomAccessDeniedHandler;
import com.gameplatform.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    private static final String[] AUTH_WHITELIST = {
            // -- 静态资源
            "/static/**",
            "/favicon.ico",
            // -- Swagger UI
            "/swagger-ui/**",
            "/v3/api-docs/**",
            // -- 认证相关
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verification-code",
            "/api/auth/reset-password",
            // -- WebSocket
            "/ws/**",
            // -- 公开API
            "/api/games/public/**",
            "/api/posts/public/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .authorizeRequests()
                // 白名单放行
                .antMatchers(AUTH_WHITELIST).permitAll()
                // OPTIONS请求放行
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 管理接口需要管理员权限
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // 用户相关接口需要认证
                .antMatchers("/api/users/**").authenticated()
                .antMatchers("/api/notifications/**").authenticated()
                // 其他写操作需要认证
                .antMatchers(HttpMethod.POST, "/api/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/**").authenticated()
                // 其他请求默认放行
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}