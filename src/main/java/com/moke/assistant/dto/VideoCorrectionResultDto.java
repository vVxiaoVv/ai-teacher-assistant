package com.moke.assistant.dto;

import lombok.Data;

/**
 * 视频纠偏结果DTO
 */
@Data
public class VideoCorrectionResultDto {

    private String taskId;
    
    /**
     * 状态：PROCESSING / DONE / FAILED
     */
    private String status;
    
    private String summary;
    private String structureAdvice;
    private String interactionAdvice;
    private String languageAdvice;
}


