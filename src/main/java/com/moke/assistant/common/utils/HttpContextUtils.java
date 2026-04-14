package com.moke.assistant.common.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * HTTP上下文工具类
 */
public class HttpContextUtils {

    /**
     * 获取当前请求
     */
    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }

    /**
     * 获取当前响应
     */
    public static HttpServletResponse getHttpServletResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getResponse();
        }
        return null;
    }

    /**
     * 获取当前会话
     */
    public static HttpSession getHttpSession() {
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            return request.getSession();
        }
        return null;
    }

    /**
     * 获取请求头信息
     */
    public static String getHeader(String name) {
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            return request.getHeader(name);
        }
        return null;
    }

    /**
     * 获取请求参数
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            return request.getParameter(name);
        }
        return null;
    }
}