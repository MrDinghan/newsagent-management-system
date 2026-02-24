package org.shining319.newsstand_backend_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 静态资源映射配置
 **/
@Configuration
public class ResourcesMapping implements WebMvcConfigurer {

    /**
     * 配置静态资源处理
     * 允许通过URL直接访问static目录下的文件
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射 - 映射 /static/** 路径到 classpath:/static/
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0); // 禁用缓存，确保获取最新文件
    }

}
