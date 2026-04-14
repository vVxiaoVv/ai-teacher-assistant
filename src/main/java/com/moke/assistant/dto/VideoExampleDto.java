package com.moke.assistant.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频范例数据传输对象
 */
@Data
public class VideoExampleDto {
    
    /**
     * 视频范例ID
     */
    private Long id;
    
    /**
     * 关联的教案ID
     */
    private Long lessonPlanId;
    
    /**
     * 教案标题
     */
    private String lessonPlanTitle;
    
    /**
     * 视频URL
     */
    private String videoUrl;
    
    /**
     * 视频主题
     */
    private String topic;
    
    /**
     * 视频描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}