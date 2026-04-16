package com.moke.assistant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonPlanDto {
    
    private Long id;
    
    private String title;
    
    private String contentSummary;
    
    private LocalDateTime uploadTime;
    
    private Long classroomId;
}
