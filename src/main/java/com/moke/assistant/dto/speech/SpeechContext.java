package com.moke.assistant.dto.speech;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 语音识别上下文信息
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeechContext {
    
    /**
     * SDK信息
     */
    private SdkInfo sdk;
    
    /**
     * 应用信息
     */
    private Map<String, Object> app = new HashMap<>();
    
    /**
     * 系统信息
     */
    private Map<String, Object> system = new HashMap<>();
    
    /**
     * 设备信息
     */
    private Map<String, Object> device = new HashMap<>();
    
    /**
     * 网络信息
     */
    private Map<String, Object> network = new HashMap<>();
    
    /**
     * 地理信息
     */
    private Map<String, Object> geography = new HashMap<>();
    
    /**
     * 桥接信息
     */
    private Map<String, Object> bridge = new HashMap<>();
    
    /**
     * 自定义信息
     */
    private Map<String, Object> custom = new HashMap<>();
    
    /**
     * SDK信息内部类
     */
    @Data
    public static class SdkInfo {
        /**
         * SDK名称
         */
        private String name = "moke-speech-sdk";
        
        /**
         * SDK版本
         */
        private String version = "1.0.0";
        
        /**
         * 开发语言
         */
        private String language = "java";
    }
    
    /**
     * 创建默认上下文
     */
    public static SpeechContext createDefault() {
        SpeechContext context = new SpeechContext();
        context.setSdk(new SdkInfo());
        return context;
    }
}

