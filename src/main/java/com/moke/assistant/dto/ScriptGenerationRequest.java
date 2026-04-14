package com.moke.assistant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 逐字稿生成AI接口请求DTO
 */
@Data
public class ScriptGenerationRequest {
    
    @JsonProperty("model")
    private String model = "gpt-5.1";
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    @JsonProperty("reasoning_effort")
    private String reasoningEffort = "high";
    
    @Data
    public static class Message {
        @JsonProperty("role")
        private String role;
        
        @JsonProperty("content")
        private String content;
    }
}

