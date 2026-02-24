package org.shining319.newsstand_backend_system.util;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: UUID工具类 - 生成UUIDv7
 *
 * UUIDv7特性:
 * - 时间有序（前48位为Unix时间戳毫秒）
 * - 全局唯一（分布式环境下安全）
 * - B-Tree友好（适合数据库索引）
 * - 包含随机性（保证同一毫秒内的唯一性）
 **/
public final class UuidUtil {

    private UuidUtil() {
        // 私有构造函数，防止实例化
    }

    /**
     * 生成UUIDv7格式的ID字符串
     *
     * @return UUID字符串（36字符，包含连字符）
     */
    public static String generateUuidV7() {
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        return uuid.toString();
    }

    /**
     * 生成UUIDv7格式的ID字符串（无连字符）
     *
     * @return UUID字符串（32字符，无连字符）
     */
    public static String generateUuidV7WithoutHyphens() {
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        return uuid.toString().replace("-", "");
    }
}
