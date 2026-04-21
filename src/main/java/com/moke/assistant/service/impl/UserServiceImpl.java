package com.moke.assistant.service.impl;

import com.moke.assistant.common.exception.ServiceException;
import com.moke.assistant.dto.LoginRequest;
import com.moke.assistant.dto.LoginResponse;
import com.moke.assistant.dto.RegisterRequest;
import com.moke.assistant.dto.UpdateUserInfoRequest;
import com.moke.assistant.entity.User;
import com.moke.assistant.repository.UserRepository;
import com.moke.assistant.service.UserService;
import com.moke.assistant.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();
        
        // 根据用户名查询用户
        User user = userRepository.findByUsername(loginRequest.getUsername());
        
        // 验证用户是否存在以及密码是否正确
        if (user == null || !verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            response.setSuccess(false);
            response.setMessage("用户名或密码错误");
            return response;
        }
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        response.setSuccess(true);
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        response.setMessage("登录成功");
        
        return response;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean verifyPassword(String inputPassword, String storedPassword) {
        // 明文密码验证，直接比较字符串
        return inputPassword.equals(storedPassword);
    }

    @Override
    public LoginResponse register(RegisterRequest registerRequest) {
        LoginResponse response = new LoginResponse();
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            response.setSuccess(false);
            response.setMessage("用户名已存在");
            return response;
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword()); // 明文存储密码
        user.setRole("admin"); // 默认角色为admin
        user.setStatus("active"); // 默认状态为active
        
        // 保存用户到数据库
        userRepository.save(user);
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        response.setSuccess(true);
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setMessage("注册成功");
        
        return response;
    }
    
    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            logger.error("用户不存在，用户ID: {}", userId);
            throw new ServiceException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 更新用户名（如果提供）
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // 检查新用户名是否已被其他用户使用
            User existingUser = userRepository.findByUsername(request.getUsername());
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new ServiceException("用户名已被使用");
            }
            user.setUsername(request.getUsername());
        }
        
        // 更新头像URL
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        // 更新年龄
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        
        // 更新学科
        if (request.getSubject() != null) {
            user.setSubject(request.getSubject());
        }
        
        // 保存到数据库
        User savedUser = userRepository.save(user);
        logger.info("用户信息更新成功，用户ID: {}", userId);
        
        return savedUser;
    }
}
