package org.shining319.newsstand_backend_system.exception;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 资源不存在异常
 **/
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
