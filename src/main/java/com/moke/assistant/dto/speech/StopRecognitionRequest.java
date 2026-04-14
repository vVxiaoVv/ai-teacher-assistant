package com.moke.assistant.dto.speech;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 停止识别请求
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopRecognitionRequest {
    
    private SpeechHeader header;
    private SpeechContext context;
    
    /**
     * 创建停止识别请求
     * @param taskId 任务ID
     * @param appkey 应用密钥
     * @return 停止识别请求
     */
    public static StopRecognitionRequest create(String taskId, String appkey) {
        StopRecognitionRequest request = new StopRecognitionRequest();
        
        // 设置消息头
        SpeechHeader header = new SpeechHeader();
        header.setMessageId(StartRecognitionRequest.generateId());
        header.setTaskId(taskId);
        header.setNamespace("SpeechRecognizer");
        header.setName("StopRecognition");
        header.setAppkey(appkey);
        request.setHeader(header);
        
        // 设置上下文
        request.setContext(SpeechContext.createDefault());
        
        return request;
    }
}

