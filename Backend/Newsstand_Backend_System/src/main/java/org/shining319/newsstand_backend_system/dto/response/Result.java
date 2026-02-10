package org.shining319.newsstand_backend_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 统一响应Result对象
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "unified API response results")
@SuppressWarnings("all")
public class Result<T> {
    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;
    @Schema(description = "错误信息", example = "操作失败的具体原因")
    private String errorMsg;
    @Schema(description = "响应数据", example = "返回的具体数据内容")
    private T data;
    @Schema(description = "数据总数量", example = "10")
    private Long total;
    @Schema(description = "总页数", example = "5")
    private Integer totalPages;

    public static Result ok() {
        return new Result(true, null, null, null, null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result(true, null, data, null, null);
    }

    public static <T> Result<List<T>> ok(List<T> data, Long total) {
        return new Result(true, null, data, total, null);
    }

    public static Result fail(String errorMsg) {
        return new Result(false, errorMsg, null, null, null);
    }

    public static <T> Result<T> fail(String errorMsg, T data) {
        return new Result(false, errorMsg, data, null, null);
    }

    /**
     * 分页查询成功响应（包含总页数）
     *
     * @param data 数据列表
     * @param total 总记录数
     * @param totalPages 总页数
     * @return Result对象
     */
    public static <T> Result<List<T>> ok(List<T> data, Long total, Integer totalPages) {
        return new Result(true, null, data, total, totalPages);
    }

}