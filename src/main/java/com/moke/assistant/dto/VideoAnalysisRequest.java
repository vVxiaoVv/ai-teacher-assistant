package com.moke.assistant.dto;

import lombok.Data;

/**
 * 视频分析请求DTO
 */
@Data
public class VideoAnalysisRequest {
    
    private String videoUrl;
    
    public String getVideoUrl() {
        return videoUrl;
    }
    
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}

