package org.shining319.newsstand_backend_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("org.shining319.newsstand_backend_system.dao") // 扫描 MyBatis-Plus Mapper 接口
@EnableScheduling  // 启用定时任务
@EnableAspectJAutoProxy(exposeProxy = true)  // 暴露代理对象，使事务方法可以通过 AopContext 调用
@EnableAsync  // 启用异步处理，用于异步创建和推送通知
public class NewsstandBackendSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsstandBackendSystemApplication.class, args);
    }

}
