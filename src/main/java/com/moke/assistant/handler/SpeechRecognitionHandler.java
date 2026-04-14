package com.moke.assistant.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moke.assistant.dto.speech.RecognitionEvent;
import com.moke.assistant.dto.speech.StartRecognitionRequest;
import com.moke.assistant.dto.speech.StopRecognitionRequest;
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
 * 语音识别WebSocket处理器
 * 作为前端和语音服务之间的代理
 */
@Component
public class SpeechRecognitionHandler extends AbstractWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SpeechRecognitionHandler.class);
    
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
        logger.info("前端WebSocket连接已建立: {}", session.getId());
        
        // 生成taskId
        String taskId = StartRecognitionRequest.generateId();
        sessionTaskIdMap.put(session.getId(), taskId);
        
        // 先发送连接成功消息给前端，让前端知道WebSocket已建立
        try {
            Map<String, Object> connectedMsg = new HashMap<>();
            connectedMsg.put("type", "connected");
            connectedMsg.put("taskId", taskId);
            connectedMsg.put("message", "WebSocket连接已建立，正在连接语音服务...");
            String response = objectMapper.writeValueAsString(connectedMsg);
            session.sendMessage(new TextMessage(response));
            logger.info("已发送connected消息给前端, taskId: {}", taskId);
        } catch (Exception e) {
            logger.error("发送connected消息失败", e);
        }
        
        // 创建到语音服务的WebSocket连接
        try {
            logger.info("正在连接语音服务: {}", speechServiceUrl);
            WebSocketClient speechClient = createSpeechClient(session, taskId);
            speechClient.connectBlocking(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (speechClient.isOpen()) {
                sessionClientMap.put(session.getId(), speechClient);
                logger.info("语音服务连接成功, taskId: {}", taskId);
            } else {
                logger.warn("语音服务连接失败（超时）, taskId: {}", taskId);
                sendErrorToClient(session, "语音服务连接失败，请稍后重试");
            }
        } catch (Exception e) {
            logger.error("连接语音服务异常: {}", e.getMessage(), e);
            sendErrorToClient(session, "语音服务连接异常: " + e.getMessage());
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("收到前端文本消息: {}", payload);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String action = jsonNode.has("action") ? jsonNode.get("action").asText() : null;
            
            WebSocketClient speechClient = sessionClientMap.get(session.getId());
            String taskId = sessionTaskIdMap.get(session.getId());
            
            if (speechClient == null || !speechClient.isOpen()) {
                logger.warn("语音服务连接未就绪");
                sendErrorToClient(session, "语音服务连接未就绪");
                return;
            }
            
            // 提取 appkey 部分用于请求
            String appkeyPart = apiKey.contains(":") ? apiKey.split(":")[0] : apiKey;
            
            if ("start".equals(action)) {
                // 发送开始识别指令
                StartRecognitionRequest startRequest = StartRecognitionRequest.create(taskId, appkeyPart);
                String startMessage = objectMapper.writeValueAsString(startRequest);
                speechClient.send(startMessage);
                logger.info("发送开始识别指令, taskId: {}", taskId);
                
            } else if ("stop".equals(action)) {
                // 发送停止识别指令
                StopRecognitionRequest stopRequest = StopRecognitionRequest.create(taskId, appkeyPart);
                String stopMessage = objectMapper.writeValueAsString(stopRequest);
                speechClient.send(stopMessage);
                logger.info("发送停止识别指令, taskId: {}", taskId);
            }
            
        } catch (Exception e) {
            logger.error("处理前端消息失败", e);
            sendErrorToClient(session, "消息处理失败: " + e.getMessage());
        }
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // 转发音频数据到语音服务
        WebSocketClient speechClient = sessionClientMap.get(session.getId());
        
        if (speechClient != null && speechClient.isOpen()) {
            ByteBuffer audioData = message.getPayload();
            speechClient.send(audioData);
            logger.debug("转发音频数据, 大小: {} bytes", audioData.remaining());
        } else {
            logger.warn("无法转发音频数据，语音服务连接未就绪");
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("前端WebSocket连接已关闭: {}, 状态: {}", session.getId(), status);
        
        // 关闭到语音服务的连接
        WebSocketClient speechClient = sessionClientMap.remove(session.getId());
        if (speechClient != null) {
            speechClient.close();
        }
        sessionTaskIdMap.remove(session.getId());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket传输错误: {}", session.getId(), exception);
        sendErrorToClient(session, "传输错误: " + exception.getMessage());
    }
    
    /**
     * 创建到语音服务的WebSocket客户端
     */
    private WebSocketClient createSpeechClient(WebSocketSession frontendSession, String taskId) throws Exception {
        URI uri = new URI(speechServiceUrl);
        logger.info("连接语音服务 URL: {}", speechServiceUrl);
        
        // 添加认证头 - 只使用 api-key
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", apiKey);
        logger.info("认证头: api-key={}", apiKey);
        
        return new WebSocketClient(uri, headers) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                logger.info("语音服务连接已建立, taskId: {}", taskId);
                
                // 通知前端连接成功
                try {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("type", "connected");
                    responseMap.put("taskId", taskId);
                    responseMap.put("message", "语音服务连接成功");
                    String response = objectMapper.writeValueAsString(responseMap);
                    frontendSession.sendMessage(new TextMessage(response));
                } catch (Exception e) {
                    logger.error("发送连接成功消息失败", e);
                }
            }
            
            @Override
            public void onMessage(String message) {
                logger.debug("收到语音服务消息: {}", message);
                
                try {
                    // 解析事件
                    RecognitionEvent event = objectMapper.readValue(message, RecognitionEvent.class);
                    
                    // 转发给前端
                    // 优先使用 rawResult，如果不存在则使用 result
                    String rawResult = event.getRawResult();
                    String result = event.getResult();
                    String finalResult = (rawResult != null && !rawResult.isEmpty()) ? rawResult : result;
                    
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("type", event.getEventName());
                    responseMap.put("success", event.isSuccess());
                    responseMap.put("result", finalResult != null ? finalResult : "");
                    responseMap.put("rawResult", rawResult != null ? rawResult : "");
                    responseMap.put("statusText", event.getStatusText() != null ? event.getStatusText() : "");
                    responseMap.put("rawMessage", message);
                    String response = objectMapper.writeValueAsString(responseMap);
                    
                    if (frontendSession.isOpen()) {
                        frontendSession.sendMessage(new TextMessage(response));
                    }
                    
                    // 记录特殊事件
                    if (event.isRecognitionStarted()) {
                        logger.info("识别已开始, taskId: {}", taskId);
                    } else if (event.isRecognitionCompleted()) {
                        logger.info("识别已完成, taskId: {}, result: {}", taskId, event.getResult());
                    } else if (event.isTaskFailed()) {
                        logger.error("识别任务失败, taskId: {}, statusText: {}", taskId, event.getStatusText());
                    }
                    
                } catch (Exception e) {
                    logger.error("处理语音服务消息失败", e);
                }
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("语音服务连接已关闭, taskId: {}, code: {}, reason: {}", taskId, code, reason);
                
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
                    logger.error("发送连接关闭消息失败", e);
                }
            }
            
            @Override
            public void onError(Exception ex) {
                logger.error("语音服务连接错误, taskId: {}", taskId, ex);
                
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
                    logger.error("发送错误消息失败", e);
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
            logger.error("发送错误消息失败", e);
        }
    }
}

