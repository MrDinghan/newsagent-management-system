package org.shining319.newsstand_backend_system.exception;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 业务异常基类
 **/
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
