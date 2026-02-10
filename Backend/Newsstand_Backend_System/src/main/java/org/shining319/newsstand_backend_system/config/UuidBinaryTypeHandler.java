package org.shining319.newsstand_backend_system.config;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.StringTypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author: shining319
 * @Date: 2026/2/9
 * @Description: UUID字符串与MySQL BINARY(16)类型转换处理器
 *
 * 功能说明：
 * - Java侧使用String类型存储UUID（36字符，如"018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"）
 * - MySQL侧使用BINARY(16)类型存储（16字节二进制）
 * - 自动处理UUID_TO_BIN()和BIN_TO_UUID()的转换逻辑
 *
 * 使用场景：
 * - 在Product等实体的id字段上应用此TypeHandler
 * - 配合@TableField(typeHandler = UuidBinaryTypeHandler.class)使用
 **/
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.BINARY)
public class UuidBinaryTypeHandler extends StringTypeHandler {

    /**
     * 设置非空参数：将UUID字符串转换为BINARY(16)
     * 相当于MySQL的UUID_TO_BIN(uuid)函数
     *
     * @param ps PreparedStatement
     * @param i 参数索引
     * @param parameter UUID字符串（36字符，带连字符）
     * @param jdbcType JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setBytes(i, uuidStringToBytes(parameter));
    }

    /**
     * 获取可空结果（按列名）：将BINARY(16)转换为UUID字符串
     *
     * @param rs ResultSet
     * @param columnName 列名
     * @return UUID字符串
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        byte[] bytes = rs.getBytes(columnName);
        return bytesToUuidString(bytes);
    }

    /**
     * 获取可空结果（按列索引）：将BINARY(16)转换为UUID字符串
     *
     * @param rs ResultSet
     * @param columnIndex 列索引
     * @return UUID字符串
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        return bytesToUuidString(bytes);
    }

    /**
     * 获取可空结果（存储过程）：将BINARY(16)转换为UUID字符串
     *
     * @param cs CallableStatement
     * @param columnIndex 列索引
     * @return UUID字符串
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] bytes = cs.getBytes(columnIndex);
        return bytesToUuidString(bytes);
    }

    /**
     * 将UUID字符串转换为16字节的byte数组
     *
     * 转换逻辑：
     * 1. 移除UUID中的连字符（36字符 → 32字符）
     * 2. 将32位十六进制字符串转换为16字节的byte数组
     *
     * 示例：
     * 输入: "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"
     * 输出: [01, 8d, 5e, 8a, 3d, 8c, 70, 00, 8b, 2f, 3e, 4a, 5b, 6c, 7d, 8e]
     *
     * @param uuid UUID字符串（36字符，带连字符）
     * @return 16字节的byte数组
     */
    private byte[] uuidStringToBytes(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }

        // 移除连字符：018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e → 018d5e8a3d8c70008b2f3e4a5b6c7d8e
        String hex = uuid.replace("-", "");

        // 验证长度（必须是32个十六进制字符）
        if (hex.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid);
        }

        // 将32个十六进制字符转换为16个字节
        byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }

        return bytes;
    }

    /**
     * 将16字节的byte数组转换为UUID字符串
     *
     * 转换逻辑：
     * 1. 将16字节转换为32位十六进制字符串
     * 2. 在指定位置插入连字符（8-4-4-4-12格式）
     *
     * 示例：
     * 输入: [01, 8d, 5e, 8a, 3d, 8c, 70, 00, 8b, 2f, 3e, 4a, 5b, 6c, 7d, 8e]
     * 输出: "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"
     *
     * @param bytes 16字节的byte数组
     * @return UUID字符串（36字符，带连字符）
     */
    private String bytesToUuidString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        // 验证长度（必须是16字节）
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Invalid binary UUID length: " + bytes.length);
        }

        // 将16字节转换为32位十六进制字符串
        StringBuilder hex = new StringBuilder(32);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }

        // 插入连字符，格式：8-4-4-4-12
        // 018d5e8a3d8c70008b2f3e4a5b6c7d8e → 018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e
        return hex.substring(0, 8) + "-" +
                hex.substring(8, 12) + "-" +
                hex.substring(12, 16) + "-" +
                hex.substring(16, 20) + "-" +
                hex.substring(20, 32);
    }
}
