package com.moke.assistant.dto;

import lombok.Data;

/**
 * 教师画像请求DTO
 */
@Data
public class TeacherPortraitRequest {
    
    /**
     * 用户ID（可选，如果不提供则使用当前登录用户）
     */
    private Long userId;
}






