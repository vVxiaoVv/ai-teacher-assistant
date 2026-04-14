package com.moke.assistant.service;

import com.moke.assistant.dto.VideoAnalysisResponse;

/**
 * 视频分析服务接口
 */
public interface VideoAnalysisService {
    
    /**
     * 分析视频内容，调用大模型API进行视频转文本分析
     * 
     * @param videoUrl 视频URL地址
     * @return 格式化后的分析结果
     */
    VideoAnalysisResponse analyzeVideo(String videoUrl);
}

