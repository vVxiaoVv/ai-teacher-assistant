package com.moke.assistant.dto;

import lombok.Data;

/**
 * 教师画像响应DTO
 */
@Data
public class TeacherPortraitResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 文字描述（教师特点分析）
     */
    private String description;
    
    /**
     * 六芒星矩阵分数
     */
    private HexagramScoreDto hexagramScore;
    
    /**
     * 打分规则说明
     */
    private String scoringRule;
    
    /**
     * 使用的历史记录数量
     */
    private Integer historyCount;
    
    /**
     * 用户头像URL
     */
    private String avatarUrl;
}






