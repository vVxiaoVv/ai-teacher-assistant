package com.moke.assistant.dto.speech;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

/**
 * 开始识别请求
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StartRecognitionRequest {
    
    private SpeechHeader header;
    private SpeechPayload payload;
    private SpeechContext context;
    
    /**
     * 创建开始识别请求
     * @param taskId 任务ID
     * @param appkey 应用密钥
     * @return 开始识别请求
     */
    public static StartRecognitionRequest create(String taskId, String appkey) {
        StartRecognitionRequest request = new StartRecognitionRequest();
        
        // 设置消息头
        SpeechHeader header = new SpeechHeader();
        header.setMessageId(generateId());
        header.setTaskId(taskId);
        header.setNamespace("SpeechRecognizer");
        header.setName("StartRecognition");
        header.setAppkey(appkey);
        request.setHeader(header);
        
        // 设置消息体
        SpeechPayload payload = new SpeechPayload();
        payload.setCluster("aliyun");
        payload.setFormat("pcm");
        request.setPayload(payload);
        
        // 设置上下文
        request.setContext(SpeechContext.createDefault());
        
        return request;
    }
    
    /**
     * 生成符合规范的ID（UUID去除中划线，小写）
     */
    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}

