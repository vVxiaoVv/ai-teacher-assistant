package com.moke.assistant.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频分析历史记录项DTO
 */
@Data
public class VideoAnalysisHistoryItemDto {
    
    private Long id;
    private String videoUrl;
    /**
     * 标题（课题+时间戳）
     */
    private String title;
    private String formattedMessage;
    private String rawMessage;
    private LocalDateTime createTime;
    /**
     * 提交人ID
     */
    private Long createUserId;
    /**
     * 提交人用户名
     */
    private String createUsername;
}

