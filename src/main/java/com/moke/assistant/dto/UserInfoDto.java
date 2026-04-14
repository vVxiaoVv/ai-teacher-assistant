package com.moke.assistant.dto;

import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDto {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
}






