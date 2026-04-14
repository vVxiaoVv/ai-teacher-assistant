package com.moke.assistant.config;

import com.moke.assistant.handler.SpeechRecognitionHandler;
import com.moke.assistant.handler.SpeechSynthesisHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private SpeechRecognitionHandler speechRecognitionHandler;
    
    @Autowired
    private SpeechSynthesisHandler speechSynthesisHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册语音识别WebSocket处理器
        registry.addHandler(speechRecognitionHandler, "/ws/speech")
                .setAllowedOrigins("*");  // 允许所有来源，生产环境需要限制
        
        // 注册语音合成(TTS)WebSocket处理器
        registry.addHandler(speechSynthesisHandler, "/ws/tts")
                .setAllowedOrigins("*");  // 允许所有来源，生产环境需要限制
    }
}

