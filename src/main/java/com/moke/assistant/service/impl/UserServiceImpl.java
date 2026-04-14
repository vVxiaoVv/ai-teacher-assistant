package com.moke.assistant.service.impl;

import com.moke.assistant.dto.LoginRequest;
import com.moke.assistant.dto.LoginResponse;
import com.moke.assistant.dto.RegisterRequest;
import com.moke.assistant.entity.User;
import com.moke.assistant.repository.UserRepository;
import com.moke.assistant.service.UserService;
import com.moke.assistant.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

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
}
