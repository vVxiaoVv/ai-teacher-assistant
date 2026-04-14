package com.moke.assistant.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moke.assistant.dto.speech.StartSynthesisRequest;
import com.moke.assistant.dto.speech.SynthesisEvent;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音合成WebSocket处理器
 * 作为前端和语音服务之间的代理，用于文本转语音功能
 */
@Component
public class SpeechSynthesisHandler extends AbstractWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SpeechSynthesisHandler.class);
    
    @Value("${speech.service.url}")
    private String speechServiceUrl;
    
    @Value("${speech.service.api-key}")
    private String apiKey;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储前端session与语音服务client的映射
    private final Map<String, WebSocketClient> sessionClientMap = new ConcurrentHashMap<>();
    
    // 存储session与taskId的映射
    private final Map<String, String> sessionTaskIdMap = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("TTS前端WebSocket连接已建立: {}", session.getId());
        
        // 生成taskId
        String taskId = StartSynthesisRequest.generateId();
        sessionTaskIdMap.put(session.getId(), taskId);
        
        // 先发送连接成功消息给前端
        try {
            Map<String, Object> connectedMsg = new HashMap<>();
            connectedMsg.put("type", "connected");
            connectedMsg.put("taskId", taskId);
            connectedMsg.put("message", "WebSocket连接已建立，正在连接语音服务...");
            String response = objectMapper.writeValueAsString(connectedMsg);
            session.sendMessage(new TextMessage(response));
            logger.info("TTS已发送connected消息给前端, taskId: {}", taskId);
        } catch (Exception e) {
            logger.error("TTS发送connected消息失败", e);
        }
        
        // 创建到语音服务的WebSocket连接
        try {
            logger.info("TTS正在连接语音服务: {}", speechServiceUrl);
            WebSocketClient speechClient = createSpeechClient(session, taskId);
            speechClient.connectBlocking(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (speechClient.isOpen()) {
                sessionClientMap.put(session.getId(), speechClient);
                logger.info("TTS语音服务连接成功, taskId: {}", taskId);
            } else {
                logger.warn("TTS语音服务连接失败（超时）, taskId: {}", taskId);
                sendErrorToClient(session, "语音服务连接失败，请稍后重试");
            }
        } catch (Exception e) {
            logger.error("TTS连接语音服务异常: {}", e.getMessage(), e);
            sendErrorToClient(session, "语音服务连接异常: " + e.getMessage());
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("TTS收到前端文本消息: {}", payload);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String action = jsonNode.has("action") ? jsonNode.get("action").asText() : null;
            
            WebSocketClient speechClient = sessionClientMap.get(session.getId());
            String taskId = sessionTaskIdMap.get(session.getId());
            
            if (speechClient == null || !speechClient.isOpen()) {
                logger.warn("TTS语音服务连接未就绪");
                sendErrorToClient(session, "语音服务连接未就绪");
                return;
            }
            
            if ("synthesize".equals(action)) {
                // 获取合成参数
                String text = jsonNode.has("text") ? jsonNode.get("text").asText() : "";
                String voice = jsonNode.has("voice") ? jsonNode.get("voice").asText() : null;
                boolean isLongText = jsonNode.has("longText") && jsonNode.get("longText").asBoolean();
                
                if (text.isEmpty()) {
                    sendErrorToClient(session, "合成文本不能为空");
                    return;
                }
                
                // 发送开始合成指令
                StartSynthesisRequest startRequest = StartSynthesisRequest.create(taskId, apiKey, text, voice, isLongText);
                String startMessage = objectMapper.writeValueAsString(startRequest);
                speechClient.send(startMessage);
                logger.info("TTS发送开始合成指令, taskId: {}, text长度: {}, voice: {}", taskId, text.length(), voice);
            }
            
        } catch (Exception e) {
            logger.error("TTS处理前端消息失败", e);
            sendErrorToClient(session, "消息处理失败: " + e.getMessage());
        }
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // TTS 不需要处理前端发来的二进制数据
        logger.debug("TTS收到前端二进制消息，已忽略");
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("TTS前端WebSocket连接已关闭: {}, 状态: {}", session.getId(), status);
        
        // 关闭到语音服务的连接
        WebSocketClient speechClient = sessionClientMap.remove(session.getId());
        if (speechClient != null) {
            speechClient.close();
        }
        sessionTaskIdMap.remove(session.getId());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("TTS WebSocket传输错误: {}", session.getId(), exception);
        sendErrorToClient(session, "传输错误: " + exception.getMessage());
    }
    
    /**
     * 创建到语音服务的WebSocket客户端
     */
    private WebSocketClient createSpeechClient(WebSocketSession frontendSession, String taskId) throws Exception {
        URI uri = new URI(speechServiceUrl);
        logger.info("TTS连接语音服务 URL: {}", speechServiceUrl);
        
        // 添加认证头
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", apiKey);
        logger.info("TTS认证头: api-key={}", apiKey);
        
        return new WebSocketClient(uri, headers) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                logger.info("TTS语音服务连接已建立, taskId: {}", taskId);
                
                // 通知前端连接成功
                try {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("type", "connected");
                    responseMap.put("taskId", taskId);
                    responseMap.put("message", "语音服务连接成功");
                    String response = objectMapper.writeValueAsString(responseMap);
                    frontendSession.sendMessage(new TextMessage(response));
                } catch (Exception e) {
                    logger.error("TTS发送连接成功消息失败", e);
                }
            }
            
            @Override
            public void onMessage(String message) {
                logger.debug("TTS收到语音服务文本消息: {}", message);
                
                try {
                    // 解析事件
                    SynthesisEvent event = objectMapper.readValue(message, SynthesisEvent.class);
                    
                    // 转发给前端
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("type", event.getEventName());
                    responseMap.put("success", event.isSuccess());
                    responseMap.put("statusText", event.getStatusText() != null ? event.getStatusText() : "");
                    
                    // AudioPlay 事件包含音频格式信息
                    if (event.isAudioPlay()) {
                        responseMap.put("format", event.getFormat());
                        responseMap.put("sampleRate", event.getSampleRate());
                        logger.info("TTS AudioPlay事件, format: {}, sampleRate: {}", event.getFormat(), event.getSampleRate());
                    }
                    
                    // MetaInfo 事件包含字词边界信息
                    if (event.isMetaInfo() && event.getWordBoundary() != null) {
                        responseMap.put("wordBoundary", event.getWordBoundary());
                        logger.debug("TTS MetaInfo事件, wordBoundary数量: {}", event.getWordBoundary().size());
                    }
                    
                    // 记录事件
                    if (event.isSynthesisCompleted()) {
                        logger.info("TTS合成已完成, taskId: {}", taskId);
                    } else if (event.isTaskFailed()) {
                        logger.error("TTS合成任务失败, taskId: {}, statusText: {}", taskId, event.getStatusText());
                    }
                    
                    responseMap.put("rawMessage", message);
                    String response = objectMapper.writeValueAsString(responseMap);
                    
                    if (frontendSession.isOpen()) {
                        frontendSession.sendMessage(new TextMessage(response));
                    }
                    
                } catch (Exception e) {
                    logger.error("TTS处理语音服务消息失败", e);
                }
            }
            
            @Override
            public void onMessage(ByteBuffer bytes) {
                // 接收到二进制音频数据，转发给前端
                logger.debug("TTS收到音频数据, 大小: {} bytes", bytes.remaining());
                
                try {
                    if (frontendSession.isOpen()) {
                        // 复制 ByteBuffer 以避免被修改
                        byte[] audioData = new byte[bytes.remaining()];
                        bytes.get(audioData);
                        frontendSession.sendMessage(new BinaryMessage(audioData));
                    }
                } catch (Exception e) {
                    logger.error("TTS转发音频数据失败", e);
                }
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("TTS语音服务连接已关闭, taskId: {}, code: {}, reason: {}", taskId, code, reason);
                
                // 通知前端连接关闭
                try {
                    if (frontendSession.isOpen()) {
                        Map<String, Object> responseMap = new HashMap<>();
                        responseMap.put("type", "disconnected");
                        responseMap.put("taskId", taskId);
                        responseMap.put("message", "语音服务连接已关闭");
                        String response = objectMapper.writeValueAsString(responseMap);
                        frontendSession.sendMessage(new TextMessage(response));
                    }
                } catch (Exception e) {
                    logger.error("TTS发送连接关闭消息失败", e);
                }
            }
            
            @Override
            public void onError(Exception ex) {
                logger.error("TTS语音服务连接错误, taskId: {}", taskId, ex);
                
                // 通知前端错误
                try {
                    if (frontendSession.isOpen()) {
                        Map<String, Object> responseMap = new HashMap<>();
                        responseMap.put("type", "error");
                        responseMap.put("taskId", taskId);
                        responseMap.put("message", "语音服务错误: " + ex.getMessage());
                        String response = objectMapper.writeValueAsString(responseMap);
                        frontendSession.sendMessage(new TextMessage(response));
                    }
                } catch (Exception e) {
                    logger.error("TTS发送错误消息失败", e);
                }
            }
        };
    }
    
    /**
     * 向前端发送错误消息
     */
    private void sendErrorToClient(WebSocketSession session, String errorMessage) {
        try {
            if (session.isOpen()) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("type", "error");
                responseMap.put("message", errorMessage);
                String response = objectMapper.writeValueAsString(responseMap);
                session.sendMessage(new TextMessage(response));
            }
        } catch (Exception e) {
            logger.error("TTS发送错误消息失败", e);
        }
    }
}




