package org.shining319.newsstand_backend_system.exception;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 资源冲突异常
 **/
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
