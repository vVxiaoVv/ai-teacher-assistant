package com.moke.assistant.dto.speech;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 识别事件响应
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecognitionEvent {
    
    private SpeechHeader header;
    private SpeechPayload payload;
    
    /**
     * 判断是否为成功状态
     */
    public boolean isSuccess() {
        return header != null && header.getStatus() != null && header.getStatus() == 20000000;
    }
    
    /**
     * 获取事件名称
     */
    public String getEventName() {
        return header != null ? header.getName() : null;
    }
    
    /**
     * 判断是否为识别开始事件
     */
    public boolean isRecognitionStarted() {
        return "RecognitionStarted".equals(getEventName());
    }
    
    /**
     * 判断是否为识别完成事件
     */
    public boolean isRecognitionCompleted() {
        return "RecognitionCompleted".equals(getEventName());
    }
    
    /**
     * 判断是否为中间识别结果
     */
    public boolean isRecognitionResultChanged() {
        return "RecognitionResultChanged".equals(getEventName());
    }
    
    /**
     * 判断是否为句子开始
     */
    public boolean isSentenceBegin() {
        return "SentenceBegin".equals(getEventName());
    }
    
    /**
     * 判断是否为句子结束
     */
    public boolean isSentenceEnd() {
        return "SentenceEnd".equals(getEventName());
    }
    
    /**
     * 判断是否为任务失败
     */
    public boolean isTaskFailed() {
        return "TaskFailed".equals(getEventName());
    }
    
    /**
     * 获取识别结果文本
     */
    public String getResult() {
        return payload != null ? payload.getResult() : null;
    }
    
    /**
     * 获取原始识别结果文本（如果存在，通常比 result 更准确）
     */
    public String getRawResult() {
        return payload != null ? payload.getRawResult() : null;
    }
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        return header != null ? header.getStatusText() : null;
    }
}

