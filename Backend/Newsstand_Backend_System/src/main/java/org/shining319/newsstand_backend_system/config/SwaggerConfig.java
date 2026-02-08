package org.shining319.newsstand_backend_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: SpringDoc OpenAPI 3.0 配置
 * 提供 Swagger UI 文档界面和 API 认证配置
 **/
@Configuration
public class SwaggerConfig {

    /**
     * 自定义 OpenAPI 配置
     * 配置 API 文档的基本信息、认证方案和全局安全要求
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Newsstand Management System API")
                        .version("v1.0.0")
                        .description("Newsstand Management System RESTful API Documentation")
                        .contact(new Contact()
                                .name("shining319")
                                .email("lofterruyuan@gmail.com")
                                .url("https://github.com/MrDinghan/newsagent-management-system"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation")
                        .url("https://github.com/MrDinghan/newsagent-management-system"))
                // 配置全局认证要求
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                // 配置认证方案
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createBearerScheme()));
    }

    /**
     * 创建 Sa-Token 认证方案
     * 使用 API Key 方式在请求头中传递 Sa-Token
     *
     * 说明：
     * - Header 名称：satoken（与 application.yml 中的 token-name 配置一致）
     * - Token 格式：Bearer {tokenValue}（与 tokenPrefix 配置一致）
     * - Token 样式：UUID（与 token-style 配置一致）
     *
     * @return SecurityScheme 实例
     */
    private SecurityScheme createBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("satoken")
                .description("请在请求头中添加 Sa-Token，格式：satoken: Bearer {tokenValue}\n\n" +
                        "示例：satoken: Bearer 6d5e1f2a-3b4c-5d6e-7f8a-9b0c1d2e3f4a\n\n" +
                        "注意：在 Swagger UI 的 Authorize 对话框中，需要输入完整的 token 值（包含 'Bearer ' 前缀）");
    }

    /**
     * 配置 API 分组
     * 集成全局响应操作自定义器，自动为需要认证的接口添加 401 响应说明
     *
     * @param customizer 全局响应操作自定义器实例
     * @return GroupedOpenApi 实例
     */
    @Bean
    public GroupedOpenApi publicApi(GlobalResponseOperationCustomizer customizer) {
        return GroupedOpenApi.builder()
                .group("all-api")
                .pathsToExclude("/**")
                .addOperationCustomizer(customizer)
                .build();
    }

}
