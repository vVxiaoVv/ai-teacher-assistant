package com.moke.assistant.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频分析历史查询DTO
 */
@Data
public class VideoAnalysisHistoryQueryDto {
    
    /**
     * 视频URL（模糊查询）
     */
    private String videoUrl;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 页码（从0开始）
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
}

