package com.gameplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class GameplatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameplatformApplication.class, args);
    }

    // 添加全局异常处理配置
    @Bean
    public ErrorPageRegistrar errorPageRegistrar() {
        return registry -> {
            registry.addErrorPages(new ErrorPage(HttpStatus.UNAUTHORIZED, "/api/error/401"));
            registry.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/api/error/403"));
            registry.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/api/error/404"));
            registry.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/api/error/500"));
        };
    }
}