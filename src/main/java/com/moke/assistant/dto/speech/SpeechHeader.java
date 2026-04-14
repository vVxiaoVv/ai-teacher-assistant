package com.moke.assistant.dto.speech;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 语音识别消息头
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeechHeader {
    
    /**
     * 消息ID，使用UUID生成，去除中划线，小写
     */
    @JsonProperty("message_id")
    private String messageId;
    
    /**
     * 任务ID，使用UUID生成，去除中划线，小写
     */
    @JsonProperty("task_id")
    private String taskId;
    
    /**
     * 命名空间
     */
    private String namespace;
    
    /**
     * 消息名称/事件名称
     */
    private String name;
    
    /**
     * 应用密钥
     */
    private String appkey;
    
    /**
     * 状态码（事件响应）
     */
    private Integer status;
    
    /**
     * 状态文本（事件响应）
     */
    @JsonProperty("status_text")
    private String statusText;
}

