package org.shining319.newsstand_backend_system.config;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Map;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: swagger全局响应操作自定义器
 * 为需要认证的接口自动添加 401 响应说明
 **/
@Component
@SuppressWarnings("all")
public class GlobalResponseOperationCustomizer implements OperationCustomizer {


    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        // 检查方法是否有 Sa-Token 注解
        boolean hasSaTokenAnnotation =
                handlerMethod.hasMethodAnnotation(SaCheckLogin.class) ||
                        handlerMethod.hasMethodAnnotation(SaCheckPermission.class) ||
                        handlerMethod.hasMethodAnnotation(SaCheckRole.class);

        if (hasSaTokenAnnotation) {
            // 添加 401 响应
            operation.getResponses().addApiResponse("401",
                    new ApiResponse()
                            .description("Authentication failed - Invalid or missing token")
                            .content(new Content()
                                    .addMediaType("application/json",
                                            new MediaType()
                                                    .schema(new Schema<SaResult>().$ref("#/components/schemas" +
                                                            "/SaResult"))
                                                    .example(Map.of(
                                                            "code", 401,
                                                            "message", "The current session is not logged in",
                                                            "data", new Object()
                                                    )))));


        }
        return operation;
    }
}