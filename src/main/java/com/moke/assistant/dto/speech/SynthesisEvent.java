package com.moke.assistant.dto.speech;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 语音合成事件响应
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SynthesisEvent {
    
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
     * 判断是否为音频播放事件（PCM 首包）
     */
    public boolean isAudioPlay() {
        return "AudioPlay".equals(getEventName());
    }
    
    /**
     * 判断是否为时间戳元信息事件
     */
    public boolean isMetaInfo() {
        return "MetaInfo".equals(getEventName());
    }
    
    /**
     * 判断是否为合成完成事件
     */
    public boolean isSynthesisCompleted() {
        return "SynthesisCompleted".equals(getEventName());
    }
    
    /**
     * 判断是否为任务失败
     */
    public boolean isTaskFailed() {
        return "TaskFailed".equals(getEventName());
    }
    
    /**
     * 获取音频格式
     */
    public String getFormat() {
        return payload != null ? payload.getFormat() : null;
    }
    
    /**
     * 获取采样率
     */
    public Integer getSampleRate() {
        return payload != null ? payload.getSampleRate() : null;
    }
    
    /**
     * 获取字词边界信息
     */
    public List<SpeechPayload.WordBoundary> getWordBoundary() {
        return payload != null ? payload.getWordBoundary() : null;
    }
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        return header != null ? header.getStatusText() : null;
    }
}




