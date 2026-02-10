package org.shining319.newsstand_backend_system.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.shining319.newsstand_backend_system.dto.response.Result;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 全局异常处理器
 * 统一处理系统中的各类异常，并返回标准的 Result 响应格式
 **/
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException业务异常拦截
     * 处理业务逻辑中抛出的业务异常
     *
     * @param e BusinessException
     * @return Result 响应给前端的统一结果
     */
    @ExceptionHandler(BusinessException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Business logic error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Result.class)
            )
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.fail(e.getMessage());
    }

    /**
     * NotFoundException资源不存在异常拦截
     * 处理查询、更新、删除操作中资源不存在的情况
     *
     * @param e NotFoundException
     * @return Result 响应给前端的统一结果
     */
    @ExceptionHandler(NotFoundException.class)
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - Resource does not exist",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Result.class)
            )
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Result<Void> handleNotFoundException(NotFoundException e) {
        return Result.fail(e.getMessage());
    }

    /**
     * ConflictException资源冲突异常拦截
     * 处理资源冲突的情况，例如：唯一约束冲突、重复创建等
     *
     * @param e ConflictException
     * @return Result 响应给前端的统一结果
     */
    @ExceptionHandler(ConflictException.class)
    @ApiResponse(
            responseCode = "409",
            description = "Conflict - Resource conflict (e.g., duplicate name, unique constraint violation)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConflictExceptionResult.class)
            )
    )
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public Result<Void> handleConflictException(ConflictException e) {
        return Result.fail(e.getMessage());
    }

    /**
     * 处理Bean Validation验证失败异常
     * 处理请求参数校验失败的情况（@Valid 注解触发）
     *
     * @param e MethodArgumentNotValidException
     * @return Result 响应给前端的统一结果，包含详细的字段错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation failed (field validation errors)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationExceptionResult.class)
            )
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Result.fail("参数验证失败", errors);
    }

    /**
     * 全局异常拦截
     * 处理所有未被其他异常处理器捕获的异常
     *
     * @param e Exception
     * @return Result 响应给前端的统一结果
     */
    @ExceptionHandler(Exception.class)
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Unexpected server error occurred",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RuntimeExceptionResult.class)
            )
    )
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result<Void> handleRuntimeException(Exception e) {
        log.error("未预期的异常: {}", e.getMessage(), e);
        return Result.fail(e.getMessage());
    }



    @Schema(description = "参数验证失败响应")
    public static class ValidationExceptionResult extends Result<Map<String, String>> {
        @Schema(description = "错误信息", example = "参数验证失败")
        @Override
        public String getErrorMsg() {
            return super.getErrorMsg();
        }
        @Schema(description = "字段错误信息",example = "{'name':'不能为空'}")
        @Override
        public Map<String, String> getData() {
            return super.getData();
        }
    }

    @Schema(description = "资源冲突响应")
    public static class ConflictExceptionResult extends Result<Void> {
        @Schema(description = "错误信息", example = "资源已存在")
        @Override
        public String getErrorMsg() {
            return super.getErrorMsg();
        }
    }

    @Schema(description = "运行时异常响应")
    public static class RuntimeExceptionResult extends Result<Void> {
        @Schema(description = "错误信息", example = "服务器异常信息")
        @Override
        public String getErrorMsg() {
            return super.getErrorMsg();
        }
    }





}
