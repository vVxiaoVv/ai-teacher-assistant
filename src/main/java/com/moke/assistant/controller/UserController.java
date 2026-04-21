package com.moke.assistant.controller;

import com.moke.assistant.common.R;
import com.moke.assistant.common.annotation.SysLog;
import com.moke.assistant.common.utils.UserContext;
import com.moke.assistant.dto.LoginRequest;
import com.moke.assistant.dto.LoginResponse;
import com.moke.assistant.dto.RegisterRequest;
import com.moke.assistant.dto.UpdateUserInfoRequest;
import com.moke.assistant.dto.UserInfoDto;
import com.moke.assistant.entity.User;
import com.moke.assistant.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @SysLog("用户登录")
    public LoginResponse login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(loginRequest);
        
        // 登录成功，将token和userId设置到cookie中
        if (loginResponse.isSuccess()) {
            // 设置token到cookie（使用ResponseCookie支持SameSite属性）
            if (loginResponse.getToken() != null && !loginResponse.getToken().isEmpty()) {
                String tokenValue = loginResponse.getToken();
                ResponseCookie tokenCookie = ResponseCookie.from("token", tokenValue)
                        .path("/")
                        .maxAge(86400) // 过期时间24小时
                        .httpOnly(true) // 防止XSS攻击
                        .sameSite("Lax") // 开发环境使用Lax，生产环境HTTPS使用None
                        .build();
                response.addHeader("Set-Cookie", tokenCookie.toString());
            }
            
            // 设置userId到cookie
            if (loginResponse.getUserId() != null) {
                String userIdValue = String.valueOf(loginResponse.getUserId());
                ResponseCookie userIdCookie = ResponseCookie.from("userId", userIdValue)
                        .path("/")
                        .maxAge(86400) // 过期时间24小时
                        .httpOnly(false) // 允许前端JavaScript访问
                        .sameSite("Lax") // 开发环境使用Lax
                        .build();
                response.addHeader("Set-Cookie", userIdCookie.toString());
            }
        }
        
        return loginResponse;
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R getCurrentUserInfo(HttpServletRequest request) {
        // 优先从UserContext获取
        User user = UserContext.getUser();
        
        // 如果UserContext中没有，尝试从请求中获取userId并查询
        if (user == null) {
            Long userId = null;
            
            // 从Header获取
            String userIdFromHeader = request.getHeader("X-User-Id");
            if (userIdFromHeader != null && !userIdFromHeader.trim().isEmpty()) {
                try {
                    userId = Long.parseLong(userIdFromHeader.trim());
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
            
            // 从Cookie获取
            if (userId == null) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("userId".equals(cookie.getName())) {
                            try {
                                userId = Long.parseLong(cookie.getValue());
                                break;
                            } catch (NumberFormatException e) {
                                // 忽略
                            }
                        }
                    }
                }
            }
            
            // 根据userId查询用户
            if (userId != null) {
                user = userService.findById(userId);
            }
        }
        
        if (user == null) {
            return R.error(401, "未登录或用户不存在");
        }
        
        // 转换为DTO
        UserInfoDto userInfo = convertToUserInfoDto(user);
        
        return R.ok().put("data", userInfo);
    }
    
    /**
     * 获取指定用户信息
     */
    @GetMapping("/info/{userId}")
    public R getUserInfo(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return R.error(404, "用户不存在");
        }
        
        UserInfoDto userInfo = convertToUserInfoDto(user);
        return R.ok().put("data", userInfo);
    }
    
    /**
     * 更新当前用户信息
     */
    @PutMapping("/info")
    @SysLog("更新用户信息")
    public R updateCurrentUserInfo(@RequestBody UpdateUserInfoRequest request, HttpServletRequest httpRequest) {
        // 获取当前用户ID
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return R.error(401, "未登录");
        }
        
        try {
            User updatedUser = userService.updateUserInfo(userId, request);
            UserInfoDto userInfo = convertToUserInfoDto(updatedUser);
            return R.ok().put("data", userInfo);
        } catch (Exception e) {
            return R.error(500, "更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新指定用户信息（管理员使用）
     */
    @PutMapping("/info/{userId}")
    @SysLog("更新用户信息")
    public R updateUserInfo(@PathVariable Long userId, @RequestBody UpdateUserInfoRequest request) {
        try {
            User updatedUser = userService.updateUserInfo(userId, request);
            UserInfoDto userInfo = convertToUserInfoDto(updatedUser);
            return R.ok().put("data", userInfo);
        } catch (Exception e) {
            return R.error(500, "更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 将User实体转换为UserInfoDto
     */
    private UserInfoDto convertToUserInfoDto(User user) {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRole(user.getRole());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setAge(user.getAge());
        userInfo.setSubject(user.getSubject());
        return userInfo;
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // 优先从UserContext获取
        User user = UserContext.getUser();
        if (user != null) {
            return user.getId();
        }
        
        // 从Header获取
        String userIdFromHeader = request.getHeader("X-User-Id");
        if (userIdFromHeader != null && !userIdFromHeader.trim().isEmpty()) {
            try {
                return Long.parseLong(userIdFromHeader.trim());
            } catch (NumberFormatException e) {
                // 忽略
            }
        }
        
        // 从Cookie获取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("userId".equals(cookie.getName())) {
                    try {
                        return Long.parseLong(cookie.getValue());
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }
        }
        
        return null;
    }
}
