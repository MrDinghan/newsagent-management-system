package org.shining319.newsstand_backend_system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: CORS 跨域配置
 * 支持前后端分离架构下的跨域请求，包括普通 HTTP 请求
 **/
@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 允许的前端域名列表，多个用逗号分隔
     */
    private String allowedOrigins;

    /**
     * 是否允许携带认证信息（cookies, authorization headers）
     */
    private Boolean allowCredentials;

    /**
     * 预检请求的有效期（秒）
     */
    private Long maxAge;

    /**
     * 配置全局 CORS 映射
     * 适用于所有 HTTP 请求，包括 SSE 长连接
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 对所有路径生效
                .allowedOriginPatterns(allowedOrigins.split(","))  // 允许的源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")  // 允许的 HTTP 方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(allowCredentials)  // 允许携带认证信息
                .maxAge(maxAge);  // 预检请求缓存时间
    }
}

