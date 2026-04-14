package com.moke.assistant.common.exception;

/**
 * 业务异常类
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Integer code;

    public ServiceException(String message) {
        super(message);
        this.code = 500;
    }

    public ServiceException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message, Throwable e) {
        super(message, e);
        this.code = 500;
    }

    public ServiceException(String message, Integer code, Throwable e) {
        super(message, e);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}