package com.moke.assistant.dto.speech;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 语音消息体（识别和合成共用）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeechPayload {
    
    // ========== 识别相关字段 ==========
    
    /**
     * 集群（如：aliyun, azure）
     */
    private String cluster;
    
    /**
     * 音频格式（如：pcm）
     */
    private String format;
    
    /**
     * 识别时长（毫秒）- 事件响应
     */
    private Long duration;
    
    /**
     * 识别结果文本 - 事件响应
     */
    private String result;
    
    /**
     * 原始识别结果 - 事件响应
     */
    @JsonProperty("raw_result")
    private String rawResult;
    
    // ========== 合成相关字段 ==========
    
    /**
     * 语音角色（如：azure:zh-CN-XiaoxiaoMultilingualNeural）
     */
    private String voice;
    
    /**
     * 待合成文本
     */
    private String text;
    
    /**
     * 文本类型（plain 或 ssml）
     */
    @JsonProperty("text_type")
    private String textType;
    
    /**
     * 是否启用字幕/时间戳
     */
    @JsonProperty("enable_subtitle")
    private Boolean enableSubtitle;
    
    /**
     * 额外参数（如 outputFormat）
     */
    private Map<String, String> extra;
    
    /**
     * 采样率 - AudioPlay 事件
     */
    @JsonProperty("sample_rate")
    private Integer sampleRate;
    
    /**
     * 字词边界信息 - MetaInfo 事件
     */
    @JsonProperty("word_boundary")
    private List<WordBoundary> wordBoundary;
    
    /**
     * 字词边界信息
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WordBoundary {
        private String word;
        
        @JsonProperty("start_time")
        private Double startTime;
        
        @JsonProperty("end_time")
        private Double endTime;
        
        @JsonProperty("start_index")
        private Integer startIndex;
        
        @JsonProperty("end_index")
        private Integer endIndex;
    }
}

