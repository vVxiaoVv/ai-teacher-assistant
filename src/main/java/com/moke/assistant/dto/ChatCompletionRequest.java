package com.moke.assistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 大模型聊天完成请求DTO
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    @JsonProperty("stream")
    private Boolean stream = false;
    
    @JsonProperty("model")
    private String model = "doubao-seed-1.6-vision";
    
    @JsonProperty("stream_options")
    private StreamOptions streamOptions;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        @JsonProperty("role")
        private String role;
        
        @JsonProperty("content")
        private List<ContentItem> content;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentItem {
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("text")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String text;
        
        @JsonProperty("video_url")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private VideoUrl videoUrl;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VideoUrl {
        @JsonProperty("url")
        private String url;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StreamOptions {
        @JsonProperty("include_usage")
        private Boolean includeUsage = false;
    }
}

