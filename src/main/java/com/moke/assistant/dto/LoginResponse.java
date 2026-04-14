package com.moke.assistant.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {

    private boolean success;
    private String token;
    private String username;
    private String role;
    private Long userId;
    private String message;
}
