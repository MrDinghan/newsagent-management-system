package org.shining319.newsstand_backend_system.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: SpringDoc OpenAPI 3.0 配置
 * 提供 Swagger UI 文档界面和 API 认证配置
 **/
@Configuration
public class SwaggerConfig {

    /**
     * 响应码排除规则配置
     * Key: 方法全限定名（格式：ControllerClassName.methodName）
     * Value: 需要排除的响应码集合
     */
    private static final Map<String, Set<String>> RESPONSE_CODE_EXCLUSIONS = new HashMap<>();

    static {
        // ProductController 相关接口
        RESPONSE_CODE_EXCLUSIONS.put("ProductController.createProduct", Set.of("404"));
        RESPONSE_CODE_EXCLUSIONS.put("ProductController.queryProducts", Set.of("404", "409"));
        RESPONSE_CODE_EXCLUSIONS.put("ProductController.updateProduct", Set.of("409"));
        // adjustStock 不进行唯一性检查，ConflictException(409) 不会发生
        RESPONSE_CODE_EXCLUSIONS.put("ProductController.adjustStock", Set.of("409"));
        // deleteProduct 不进行唯一性检查，ConflictException(409) 不会发生
        RESPONSE_CODE_EXCLUSIONS.put("ProductController.deleteProduct", Set.of("409","400"));
        // getProductById 不进行唯一性检查，ConflictException(409) 不会发生；无请求体验证，400 不会发生
        RESPONSE_CODE_EXCLUSIONS.put("ProductController.getProductById", Set.of("409","400"));
        // getLowStockProducts 不进行产品查找或唯一性检查，NotFoundException(404) 和 ConflictException(409) 不会发生
        RESPONSE_CODE_EXCLUSIONS.put("ProductController.getLowStockProducts", Set.of("404","409"));
        // checkStock 不进行唯一性检查，ConflictException(409) 不会发生
        RESPONSE_CODE_EXCLUSIONS.put("ProductController.checkStock", Set.of("409"));

        // SaleController 相关接口
        // createSale 不进行唯一性检查，ConflictException(409) 不会发生
        RESPONSE_CODE_EXCLUSIONS.put("SaleController.createSale", Set.of("409"));
    }

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
                        .url("https://github.com/MrDinghan/newsagent-management-system"));
                // 配置全局认证要求
                /*.addSecurityItem(new SecurityRequirement().addList("Bearer_Authentication"))*/
                // 配置认证方案
                /*.components(new Components()
                        .addSecuritySchemes("Bearer_Authentication", createBearerScheme()));*/
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
     * 自定义操作定制器
     * 用于移除由全局异常处理器自动添加的、但实际不会发生的响应码
     *
     * 规则：
     * - 根据 RESPONSE_CODE_EXCLUSIONS 配置，自动移除指定接口的响应码
     * - 配置格式：ControllerClassName.methodName -> Set<响应码>
     *
     * @return OperationCustomizer 实例
     */
    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            // 获取方法标识：ControllerClassName.methodName
            String className = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            String methodKey = className + "." + methodName;

            // 查找该方法的排除规则
            Set<String> excludedCodes = RESPONSE_CODE_EXCLUSIONS.get(methodKey);

            // 如果有排除规则，则移除对应的响应码
            if (excludedCodes != null && operation.getResponses() != null) {
                excludedCodes.forEach(code -> operation.getResponses().remove(code));
            }

            return operation;
        };
    }

}
