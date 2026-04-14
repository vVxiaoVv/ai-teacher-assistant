package com.moke.assistant.dto;

import lombok.Data;

/**
 * 视频分析响应DTO
 */
@Data
public class VideoAnalysisResponse {
    
    /**
     * 格式化后的消息内容（已去除冗余标识符，自动换行）
     */
    private String formattedMessage;
    
    /**
     * 原始消息内容
     */
    private String rawMessage;
}

