package com.moke.assistant.common.utils;

import com.moke.assistant.entity.User;

/**
 * 用户上下文工具类，用于存储和获取当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     */
    public static void setUser(User user) {
        userThreadLocal.set(user);
    }

    /**
     * 获取当前用户信息
     */
    public static User getUser() {
        return userThreadLocal.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        User user = userThreadLocal.get();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        User user = userThreadLocal.get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        userThreadLocal.remove();
    }
}