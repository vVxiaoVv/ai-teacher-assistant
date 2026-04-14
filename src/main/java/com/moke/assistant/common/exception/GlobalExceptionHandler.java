package com.moke.assistant.common.exception;

import com.moke.assistant.common.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleServiceException(ServiceException e) {
        logger.error("业务异常: {}", e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理数据库唯一约束异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public R handleDuplicateKeyException(DuplicateKeyException e) {
        logger.error("数据库唯一约束异常: {}", e.getMessage());
        return R.error("数据库中已存在该记录");
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error("参数验证异常: {}", e.getMessage());
        
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        
        return R.error(400, "参数验证失败").put("errors", errors);
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("参数异常: {}", e.getMessage());
        return R.error(400, e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: {}", e.getMessage(), e);
        return R.error("系统异常，请联系管理员");
    }

    /**
     * 处理客户端断开连接异常（Broken pipe）
     * 这种情况通常是客户端在服务器响应完成前就关闭了连接，不需要记录为错误
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        // 客户端主动断开连接，这是正常情况，不需要记录为错误
        // 使用 debug 级别，避免在日志中产生噪音
        if (logger.isDebugEnabled()) {
            logger.debug("客户端断开连接: {}", e.getMessage());
        }
    }

    /**
     * 处理IO异常（包括 Broken pipe）
     */
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e) {
        // 如果是 Broken pipe，说明客户端断开连接，这是正常情况
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            if (logger.isDebugEnabled()) {
                logger.debug("客户端断开连接（Broken pipe）: {}", e.getMessage());
            }
        } else {
            logger.error("IO异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleException(Exception e) {
        // 如果是 ClientAbortException 或其包装的异常，忽略
        Throwable cause = e.getCause();
        if (e instanceof ClientAbortException || 
            (cause != null && cause instanceof ClientAbortException) ||
            (e.getMessage() != null && e.getMessage().contains("Broken pipe"))) {
            if (logger.isDebugEnabled()) {
                logger.debug("客户端断开连接: {}", e.getMessage());
            }
            // 客户端已断开，返回一个简单的响应（虽然客户端可能收不到）
            return R.ok();
        }
        logger.error("系统异常: {}", e.getMessage(), e);
        return R.error("系统异常，请联系管理员");
    }
}