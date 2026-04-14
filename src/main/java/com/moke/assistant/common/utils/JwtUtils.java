package com.moke.assistant.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    /**
     * 生成token
     */
    public String generateToken(Long userId, String username, String role) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析token
     */
    public Claims parseToken(String token) {
        // 验证token是否为空或格式不正确
        if (token == null || token.trim().isEmpty()) {
            logger.debug("Token为空，跳过解析");
            return null;
        }
        
        // JWT token应该包含两个点分隔符（header.payload.signature）
        if (token.split("\\.").length != 3) {
            logger.debug("Token格式不正确，跳过解析: token长度不符合JWT格式");
            return null;
        }
        
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 将错误日志级别改为DEBUG，因为无效token是正常情况（用户可能未登录）
            logger.debug("解析token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                return Long.parseLong(userIdObj.toString());
            }
        }
        return null;
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    /**
     * 从token中获取用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }

    /**
     * 判断token是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Date expireDate = claims.getExpiration();
            return expireDate.before(new Date());
        }
        return true;
    }

    /**
     * 刷新token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Long userId = Long.parseLong(claims.get("userId").toString());
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            return generateToken(userId, username, role);
        }
        return null;
    }
}