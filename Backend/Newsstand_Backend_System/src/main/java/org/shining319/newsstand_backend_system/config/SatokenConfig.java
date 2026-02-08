package org.shining319.newsstand_backend_system.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: Sa-Token 配置
 **/
@Configuration
public class SatokenConfig implements WebMvcConfigurer {

    // 注册 Sa-Token 拦截器，打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        InterceptorRegistration securityInterceptor = registry.addInterceptor(new SaInterceptor());
        // 排除不需要拦截的路径
        securityInterceptor.excludePathPatterns("/**");
    }

}
