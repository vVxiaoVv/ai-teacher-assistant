package com.moke.assistant.dto.speech;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 开始语音合成请求
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StartSynthesisRequest {
    
    private SpeechHeader header;
    private SpeechPayload payload;
    private SpeechContext context;
    
    /**
     * 创建开始合成请求
     * @param taskId 任务ID
     * @param appkey 应用密钥
     * @param text 待合成文本
     * @param voice 语音角色
     * @return 开始合成请求
     */
    public static StartSynthesisRequest create(String taskId, String appkey, String text, String voice) {
        return create(taskId, appkey, text, voice, false);
    }
    
    /**
     * 创建开始合成请求
     * @param taskId 任务ID
     * @param appkey 应用密钥
     * @param text 待合成文本
     * @param voice 语音角色
     * @param isLongText 是否为长文本流式合成
     * @return 开始合成请求
     */
    public static StartSynthesisRequest create(String taskId, String appkey, String text, String voice, boolean isLongText) {
        StartSynthesisRequest request = new StartSynthesisRequest();
        
        // 设置消息头
        SpeechHeader header = new SpeechHeader();
        header.setMessageId(generateId());
        header.setTaskId(taskId);
        // 一句话合成用 SpeechSynthesizer，长文本用 SpeechLongSynthesizer
        header.setNamespace(isLongText ? "SpeechLongSynthesizer" : "SpeechSynthesizer");
        header.setName("StartSynthesis");
        header.setAppkey(appkey);
        request.setHeader(header);
        
        // 设置消息体
        SpeechPayload payload = new SpeechPayload();
        payload.setVoice(voice != null ? voice : "azure:zh-CN-XiaoxiaoMultilingualNeural");
        payload.setText(text);
        payload.setTextType("plain");
        payload.setEnableSubtitle(true);
        
        // 设置额外参数
        Map<String, String> extra = new HashMap<>();
        extra.put("outputFormat", "audio-16khz-32kbitrate-mono-mp3");
        payload.setExtra(extra);
        
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




