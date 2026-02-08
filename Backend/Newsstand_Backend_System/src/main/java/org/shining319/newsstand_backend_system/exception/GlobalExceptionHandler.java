package org.shining319.newsstand_backend_system.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.shining319.newsstand_backend_system.dto.response.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 全局异常处理
 **/
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException业务异常拦截
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
     *
     * @param e ConflictException
     * @return Result 响应给前端的统一结果
     */
    @ExceptionHandler(ConflictException.class)
    @ApiResponse(
            responseCode = "409",
            description = "Conflict - Resource conflict",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Result.class)
            )
    )
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public Result<Void> handleConflictException(ConflictException e) {
        return Result.fail(e.getMessage());
    }

    /**
     * RuntimeException全局异常拦截
     *
     * @param e RuntimeException
     * @return Result 响应给前端的统一结果
     */
    @ExceptionHandler(Exception.class)
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Runtime exception occurred",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Result.class)
            )
    )
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result handleRuntimeException(Exception e) {
        return Result.fail("Server abnormalities");
    }

    /**
     * Satoken全局异常拦截（拦截项目中的NotLoginException异常）
     *
     * @param nle NotLoginException
     * @return SaResult 响应给前端的统一结果
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public SaResult handlerNotLoginException(NotLoginException nle, HttpServletResponse response) {

        // 打印堆栈，以供调试
        nle.printStackTrace();

        // 判断场景值，定制化异常信息
        String message = "";
        int code = SaResult.CODE_NOT_LOGIN;
        if (nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "Token is missing";
            code = SaResult.CODE_ERROR;
        } else if (nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "Token is invalid";
            code = SaResult.CODE_ERROR;
        } else if (nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "The token has expired";
            code = SaResult.CODE_ERROR;
        } else if (nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "Token has been topped down";
            code = SaResult.CODE_ERROR;
        } else if (nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "The token has been kicked offline";
            code = SaResult.CODE_ERROR;
        } else if (nle.getType().equals(NotLoginException.TOKEN_FREEZE)) {
            message = "The token has been frozen";
            code = SaResult.CODE_ERROR;
        } else if (nle.getType().equals(NotLoginException.NO_PREFIX)) {
            message = "Tokens are not submitted according to the specified prefix";
            code = SaResult.CODE_ERROR;
        } else {
            message = "The current session is not logged in";
        }

        // 返回给前端
        return SaResult.error(message).setCode(code);
    }
}
