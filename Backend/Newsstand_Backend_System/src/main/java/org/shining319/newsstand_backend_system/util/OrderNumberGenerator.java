package org.shining319.newsstand_backend_system.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: shining319
 * @Date: 2026/2/25
 * @Description: 订单号生成器
 *
 * 生成规则：SO + yyyyMMddHHmmss + 3位序号
 * 示例：SO20260127203001001
 *
 * 唯一性保证：
 * - 同一秒内通过递增序号（001~999）区分并发请求
 * - generate() 方法使用 synchronized 保证线程安全
 * - Spring 单例确保应用内全局只有一个计数器实例
 **/
@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AtomicInteger sequence = new AtomicInteger(0);
    private String lastSecond = "";

    /**
     * 生成唯一订单号
     *
     * @return 订单号，格式：SOyyyyMMddHHmmss + 3位序号，例如 SO20260225143025001
     */
    public synchronized String generate() {
        String currentSecond = LocalDateTime.now().format(FORMATTER);
        if (!currentSecond.equals(lastSecond)) {
            lastSecond = currentSecond;
            sequence.set(1);
        } else {
            sequence.incrementAndGet();
        }
        return "SO" + currentSecond + String.format("%03d", sequence.get());
    }
}
