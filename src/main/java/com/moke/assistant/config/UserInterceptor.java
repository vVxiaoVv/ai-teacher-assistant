package com.moke.assistant.config;

import com.moke.assistant.common.utils.JwtUtils;
import com.moke.assistant.common.utils.UserContext;
import com.moke.assistant.entity.User;
import com.moke.assistant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户拦截器，从Cookie中获取JWT令牌并解析用户信息
 */
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从Cookie中获取token和userId
        String token = null;
        String userIdFromCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                } else if ("userId".equals(cookie.getName())) {
                    userIdFromCookie = cookie.getValue();
                }
            }
        }

        // 优先从Header中获取userId（uniapp通过Header传递）
        String userIdFromHeader = request.getHeader("X-User-Id");
        Long userId = null;
        if (userIdFromHeader != null && !userIdFromHeader.trim().isEmpty()) {
            try {
                userId = Long.parseLong(userIdFromHeader.trim());
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        } else if (userIdFromCookie != null && !userIdFromCookie.trim().isEmpty()) {
            try {
                userId = Long.parseLong(userIdFromCookie.trim());
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }

        // 如果token存在且格式正确，解析用户信息并保存到上下文
        if (token != null && !token.trim().isEmpty() && token.split("\\.").length == 3) {
            try {
                String username = jwtUtils.getUsernameFromToken(token);
                if (username != null) {
                    // 根据用户名查询用户信息
                    User user = userRepository.findByUsername(username);
                    if (user != null) {
                        UserContext.setUser(user);
                    }
                }
            } catch (Exception e) {
                // 解析token失败，尝试使用userId直接查询
                if (userId != null) {
                    try {
                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null) {
                            UserContext.setUser(user);
                        }
                    } catch (Exception ex) {
                        // 清除用户上下文
                        UserContext.clear();
                    }
                } else {
                    // 清除用户上下文
                    UserContext.clear();
                }
            }
        } else if (userId != null) {
            // 如果没有token但有userId，直接查询用户信息
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    UserContext.setUser(user);
                }
            } catch (Exception e) {
                // 清除用户上下文
                UserContext.clear();
            }
        }

        // 允许请求继续执行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文，避免内存泄漏
        UserContext.clear();
    }
}