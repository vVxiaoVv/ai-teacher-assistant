package com.moke.assistant.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教案数据传输对象
 */
@Data
public class LessonPlanDto {
    
    /**
     * 教案ID
     */
    private Long id;
    
    /**
     * 教案标题
     */
    private String title;
    
    /**
     * 教案内容摘要（前100字符）
     */
    private String contentSummary;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}