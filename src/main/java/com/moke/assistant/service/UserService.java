package com.moke.assistant.service;

import com.moke.assistant.dto.LoginRequest;
import com.moke.assistant.dto.LoginResponse;
import com.moke.assistant.dto.RegisterRequest;
import com.moke.assistant.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    User findByUsername(String username);

    /**
     * 验证密码
     * @param inputPassword 输入的密码
     * @param storedPassword 存储的密码（明文）
     * @return 是否匹配
     */
    boolean verifyPassword(String inputPassword, String storedPassword);

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    LoginResponse register(RegisterRequest registerRequest);
    
    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户实体
     */
    User findById(Long id);
}
