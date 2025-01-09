package com.gameplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:08
 * @description TODO
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("游戏社区平台 API文档")
                        .description("基于Spring Boot和Vue的游戏服务社区平台API文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("support@example.com")
                                .url("https://github.com/yingzeliangzi/gameplatform"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .addServersItem(new Server().url(contextPath).description("本地开发环境"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createSecurityScheme())
                        .addSchemas("Error", createErrorSchema())
                        .addSchemas("PageResult", createPageResultSchema())
                );
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }

    private io.swagger.v3.oas.models.media.Schema createErrorSchema() {
        return new io.swagger.v3.oas.models.media.Schema<>()
                .type("object")
                .addProperties("code", new io.swagger.v3.oas.models.media.Schema<>().type("integer"))
                .addProperties("message", new io.swagger.v3.oas.models.media.Schema<>().type("string"))
                .addProperties("data", new io.swagger.v3.oas.models.media.Schema<>().type("object"));
    }

    private io.swagger.v3.oas.models.media.Schema createPageResultSchema() {
        return new io.swagger.v3.oas.models.media.Schema<>()
                .type("object")
                .addProperties("content", new io.swagger.v3.oas.models.media.ArraySchema()
                        .items(new io.swagger.v3.oas.models.media.Schema<>().type("object")))
                .addProperties("totalElements", new io.swagger.v3.oas.models.media.Schema<>().type("integer"))
                .addProperties("totalPages", new io.swagger.v3.oas.models.media.Schema<>().type("integer"))
                .addProperties("size", new io.swagger.v3.oas.models.media.Schema<>().type("integer"))
                .addProperties("number", new io.swagger.v3.oas.models.media.Schema<>().type("integer"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi privateApi() {
        return GroupedOpenApi.builder()
                .group("private-api")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/public/**")
                .build();
    }
}