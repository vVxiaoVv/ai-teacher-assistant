package com.moke.assistant.service.impl;

import com.moke.assistant.common.exception.ServiceException;
import com.moke.assistant.common.utils.UserContext;
import com.moke.assistant.dto.*;
import com.moke.assistant.entity.TeacherPortrait;
import com.moke.assistant.entity.User;
import com.moke.assistant.entity.VideoAnalysisRecord;
import com.moke.assistant.repository.TeacherPortraitRepository;
import com.moke.assistant.repository.UserRepository;
import com.moke.assistant.repository.VideoAnalysisRecordRepository;
import com.moke.assistant.service.TeacherPortraitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 教师画像服务实现类
 */
@Service
public class TeacherPortraitServiceImpl implements TeacherPortraitService {
    
    private static final String AI_SERVICE_URL = "http://ai-service.tal.com/openai-compatible/v1/chat/completions";
    private static final String API_KEY = "300000775:568f52f31bf0782018145f7dc56c3afb";
    private static final String MODEL_NAME = "gpt-5.1";
    
    private static final Logger logger = LoggerFactory.getLogger(TeacherPortraitServiceImpl.class);
    
    private final RestTemplate restTemplate;
    private final VideoAnalysisRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final TeacherPortraitRepository portraitRepository;
    
    public TeacherPortraitServiceImpl(VideoAnalysisRecordRepository recordRepository,
                                     UserRepository userRepository,
                                     TeacherPortraitRepository portraitRepository) {
        this.restTemplate = new RestTemplate();
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
        this.portraitRepository = portraitRepository;
    }
    
    @Override
    public TeacherPortraitResponse generateTeacherPortrait(TeacherPortraitRequest request) {
        try {
            // 确定用户ID
            Long userId = request.getUserId();
            if (userId == null) {
                userId = UserContext.getUserId();
                if (userId == null) {
                    logger.error("无法确定用户ID，UserContext.getUserId()返回null");
                    throw new ServiceException("无法确定用户ID，请先登录");
                }
            }
            
            logger.info("开始生成教师画像，用户ID: {}", userId);
            
            // 查询用户信息
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                logger.error("用户不存在，用户ID: {}", userId);
                throw new ServiceException("用户不存在");
            }
            User user = userOpt.get();
            
            // 查询该用户的所有历史记录（按时间升序）
            List<VideoAnalysisRecord> records = recordRepository.findByCreateUserIdOrderByCreateTimeAsc(userId);
            logger.info("查询到用户历史记录数量: {}", records.size());
            
            if (records.isEmpty()) {
                logger.warn("用户暂无纠偏历史记录，用户ID: {}", userId);
                throw new ServiceException("该用户暂无纠偏历史记录，无法生成画像");
            }
            
            // 构建AI请求消息
            List<TeacherPortraitChatRequest.Message> messages = buildMessages(records);
            logger.info("构建AI请求消息完成，消息数量: {}", messages.size());
            
            // 调用AI接口
            logger.info("开始调用AI服务...");
            String formattedContent = callAIService(messages);
            logger.info("AI服务调用成功，响应长度: {}", formattedContent != null ? formattedContent.length() : 0);
            
            // 解析AI响应，提取文字描述和六芒星分数
            TeacherPortraitResponse response = parseAIResponse(formattedContent);
            response.setUserId(userId);
            response.setUsername(user.getUsername());
            response.setAvatarUrl(user.getAvatarUrl());
            response.setAge(user.getAge());
            response.setSubject(user.getSubject());
            response.setHistoryCount(records.size());
            
            // 生成打分规则说明
            response.setScoringRule(generateScoringRule(records));
            
            // 保存到数据库（保存格式化后的内容）
            saveTeacherPortrait(userId, response, formattedContent, records.size());
            
            logger.info("教师画像生成成功，用户ID: {}, 用户名: {}", userId, user.getUsername());
            return response;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("生成教师画像失败", e);
            throw new ServiceException("生成教师画像失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建AI请求消息
     */
    private List<TeacherPortraitChatRequest.Message> buildMessages(List<VideoAnalysisRecord> records) {
        List<TeacherPortraitChatRequest.Message> messages = new ArrayList<>();
        
        // 系统提示词（按照用户提供的格式）
        TeacherPortraitChatRequest.Message systemMessage = new TeacherPortraitChatRequest.Message();
        systemMessage.setRole("system");
        systemMessage.setContent("你的角色：资深教师培训专家、优质课评委。你的核心任务是：依据教师上传讲课视频生成的分析文档，综合评价这位老师。需要制定一套打分规则，要求纠偏历史文档时间越靠前的权重越低，时间越近的权重越高，计算出来对应的六芒星矩阵图的分数（教学基本功、教学过程设计、教态、多媒体与板书运用、课堂气氛、时间节奏掌控），六芒星每个维度满分是100分。\n\n重要要求：\n1. 在输出中必须明确包含每个维度的分数，格式如下：\n   教学基本功：XX分\n   教学过程设计：XX分\n   教态：XX分\n   多媒体与板书运用：XX分\n   课堂气氛：XX分\n   时间节奏掌控：XX分\n2. 分数范围是0-100分\n3. 除了输出分数，还要输出对于教师的整体评价。");
        messages.add(systemMessage);
        
        // 添加历史记录作为用户消息（按时间顺序，越早的权重越低）
        for (int i = 0; i < records.size(); i++) {
            VideoAnalysisRecord record = records.get(i);
            TeacherPortraitChatRequest.Message userMessage = new TeacherPortraitChatRequest.Message();
            userMessage.setRole("user");
            
            // 构建消息内容，包含时间信息和分析结果
            String content = String.format("试讲诊断报告%d（时间：%s）\n%s", 
                    i + 1, 
                    record.getCreateTime().toString(),
                    record.getFormattedMessage() != null ? record.getFormattedMessage() : record.getRawMessage());
            userMessage.setContent(content);
            messages.add(userMessage);
        }
        
        return messages;
    }
    
    /**
     * 调用AI服务
     */
    private String callAIService(List<TeacherPortraitChatRequest.Message> messages) {
        try {
            TeacherPortraitChatRequest request = new TeacherPortraitChatRequest();
            request.setModel(MODEL_NAME);
            request.setMessages(messages);
            request.setReasoningEffort("high");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);
            
            HttpEntity<TeacherPortraitChatRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<TeacherPortraitChatResponse> response = restTemplate.exchange(
                    AI_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    TeacherPortraitChatResponse.class
            );
            
            TeacherPortraitChatResponse responseBody = response.getBody();
            if (responseBody != null && 
                responseBody.getChoices() != null && 
                !responseBody.getChoices().isEmpty()) {
                String content = responseBody.getChoices().get(0).getMessage().getContent();
                if (content != null && !content.trim().isEmpty()) {
                    // 美化格式：处理换行符和冗余字符
                    return formatContent(content);
                }
            }
            
            throw new ServiceException("AI服务返回空结果");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("调用AI服务失败", e);
            throw new ServiceException("调用AI服务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 美化内容格式：处理换行符、去掉冗余字符
     */
    private String formatContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        // 1. 统一换行符为 \n
        content = content.replace("\r\n", "\n").replace("\r", "\n");
        
        // 2. 去掉特殊控制字符（保留换行符、制表符等常用字符）
        content = content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        // 3. 去掉行首行尾的空白字符，但保留段落结构
        String[] lines = content.split("\n");
        StringBuilder formatted = new StringBuilder();
        boolean lastLineEmpty = false;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty()) {
                // 空行：如果上一行不是空行，则添加一个换行符
                if (!lastLineEmpty && i > 0) {
                    formatted.append("\n");
                    lastLineEmpty = true;
                }
            } else {
                // 非空行：去掉行内多余空格，但保留必要的空格
                line = line.replaceAll("[ \\t]+", " ");
                formatted.append(line);
                if (i < lines.length - 1) {
                    formatted.append("\n");
                }
                lastLineEmpty = false;
            }
        }
        
        // 4. 去掉多余的空行（连续3个以上换行符替换为2个）
        String result = formatted.toString();
        result = result.replaceAll("\n{3,}", "\n\n");
        
        // 5. 去掉行首行尾的换行符
        result = result.replaceAll("^\\n+", "").replaceAll("\\n+$", "");
        
        return result;
    }
    
    /**
     * 解析AI响应（简化版：提取分数用于显示六芒星图）
     */
    private TeacherPortraitResponse parseAIResponse(String formattedContent) {
        TeacherPortraitResponse response = new TeacherPortraitResponse();
        
        // 提取"五、基于六维结果的综合评价（简要）"部分作为描述
        String evaluationSection = extractEvaluationSection(formattedContent);
        
        // 如果提取的章节为空，使用整个格式化后的内容作为描述
        if (evaluationSection == null || evaluationSection.trim().isEmpty()) {
            logger.warn("未找到综合评价章节，使用整个AI响应内容作为描述");
            evaluationSection = formattedContent != null ? formattedContent.trim() : "";
        }
        
        response.setDescription(evaluationSection);
        logger.info("设置description字段，长度: {}", evaluationSection != null ? evaluationSection.length() : 0);
        
        // 简化提取分数：只做基本的文本匹配，提取分数用于显示六芒星图
        HexagramScoreDto scores = extractScoresSimple(formattedContent);
        logger.info("提取到的六芒星分数: teachingFoundation={}, teachingProcessDesign={}, teachingManner={}, multimediaAndBlackboard={}, classroomAtmosphere={}, timeRhythmControl={}", 
                scores.getTeachingFoundation(), scores.getTeachingProcessDesign(), scores.getTeachingManner(),
                scores.getMultimediaAndBlackboard(), scores.getClassroomAtmosphere(), scores.getTimeRhythmControl());
        response.setHexagramScore(scores);
        
        return response;
    }
    
    /**
     * 提取"五、基于六维结果的综合评价（简要）"部分内容
     * 如果找不到该部分，返回空字符串
     */
    private String extractEvaluationSection(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        // 匹配"五、基于六维结果的综合评价（简要）"或类似标题
        // 支持多种可能的标题格式，按优先级排序
        String[] possibleTitles = {
            "五、基于六维结果的综合评价（简要）",
            "五、基于六维结果的综合评价",
            "五、综合评价（简要）",
            "五、综合评价",
            "五、综合评估（简要）",
            "五、综合评估",
            "五、"
        };
        
        int startIndex = -1;
        String matchedTitle = null;
        
        // 查找匹配的标题
        for (String title : possibleTitles) {
            int index = content.indexOf(title);
            if (index != -1) {
                startIndex = index;
                matchedTitle = title;
                break;
            }
        }
        
        // 如果没找到"五、"，尝试查找"综合评价"或"综合评估"（作为备选）
        if (startIndex == -1) {
            String[] fallbackTitles = {
                "综合评价（简要）",
                "综合评价",
                "综合评估（简要）",
                "综合评估"
            };
            for (String title : fallbackTitles) {
                int index = content.indexOf(title);
                if (index != -1) {
                    // 检查前面是否有"五、"或其他章节标记
                    String before = content.substring(Math.max(0, index - 10), index);
                    if (before.contains("五") || before.trim().isEmpty() || before.endsWith("\n")) {
                        startIndex = index;
                        matchedTitle = title;
                        break;
                    }
                }
            }
        }
        
        if (startIndex == -1) {
            logger.warn("未找到综合评价部分，返回空字符串");
            return "";
        }
        
        // 从匹配位置开始提取内容（包含标题）
        String sectionStart = content.substring(startIndex);
        
        // 查找下一个主要章节的开始（如"六、"、"---"、或其他明显的分隔符）
        // 如果没有找到，则提取到文本末尾
        int endIndex = sectionStart.length();
        
        // 查找下一个主要章节标记
        String[] nextSectionMarkers = {
            "\n六、",
            "\n六.",
            "\n七、",
            "\n七.",
            "\n---",
            "\n##",
            "\n# ",
            "\n## ",
            "\n\n---",
            "\n\n##",
            "\n\n# "
        };
        
        for (String marker : nextSectionMarkers) {
            int markerIndex = sectionStart.indexOf(marker);
            if (markerIndex != -1 && markerIndex < endIndex) {
                // 如果找到了标题，确保标记在标题之后
                if (matchedTitle != null && markerIndex > matchedTitle.length()) {
                    endIndex = markerIndex;
                } else if (matchedTitle == null) {
                    endIndex = markerIndex;
                }
            }
        }
        
        // 提取内容（包含标题）
        String extracted = sectionStart.substring(0, endIndex).trim();
        
        // 如果提取的内容为空或太短，尝试提取更多内容（直到文本末尾）
        if (extracted.isEmpty() || extracted.length() < 50) {
            extracted = sectionStart.trim();
        }
        
        // 确保包含标题
        if (!extracted.startsWith("五、") && !extracted.startsWith("综合评价") && !extracted.startsWith("综合评估")) {
            // 如果提取的内容不包含标题，尝试添加标题
            if (matchedTitle != null && !matchedTitle.equals("五、")) {
                extracted = matchedTitle + "\n" + extracted;
            }
        }
        
        // 清理提取的内容：去掉多余的空行
        extracted = extracted.replaceAll("\n{3,}", "\n\n");
        extracted = extracted.replaceAll("^\\n+", "").replaceAll("\\n+$", "");
        
        logger.info("成功提取综合评价部分，长度: {}, 标题: {}", extracted.length(), matchedTitle);
        return extracted;
    }
    
    /**
     * 简化版分数提取：从文本中提取分数（仅用于显示六芒星图）
     */
    private HexagramScoreDto extractScoresSimple(String text) {
        HexagramScoreDto scores = new HexagramScoreDto();
        
        if (text == null || text.isEmpty()) {
            logger.warn("提取分数时文本为空");
            return scores;
        }
        
        logger.info("开始提取分数，文本长度: {}", text.length());
        
        // 简单的正则匹配：匹配 "维度名：分数" 或 "维度名: 分数" 格式
        String[] dimensionNames = {
            "教学基本功", "教学过程设计", "教态", 
            "多媒体与板书运用", "课堂气氛", "时间节奏掌控"
        };
        
        String[] englishKeys = {
            "teachingFoundation", "teachingProcessDesign", "teachingManner",
            "multimediaAndBlackboard", "classroomAtmosphere", "timeRhythmControl"
        };
        
        int extractedCount = 0;
        
        for (int i = 0; i < dimensionNames.length; i++) {
            boolean found = false;
            String dimensionName = dimensionNames[i];
            
            // 尝试多种格式匹配，按优先级排序
            
            // 1. 匹配格式：教学基本功：72分 或 教学基本功: 72分 或 教学基本功：72 或 教学基本功: 72
            String[] patterns1 = {
                dimensionName + "[：:]\\s*(\\d+(?:\\.\\d+)?)\\s*分",
                dimensionName + "[：:]\\s*(\\d+(?:\\.\\d+)?)(?!\\d)",
                dimensionName + "\\s*[：:]\\s*(\\d+(?:\\.\\d+)?)\\s*分",
                dimensionName + "\\s*[：:]\\s*(\\d+(?:\\.\\d+)?)(?!\\d)"
            };
            
            for (String patternStr : patterns1) {
                if (found) break;
                try {
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile(patternStr);
                    java.util.regex.Matcher m = p.matcher(text);
                    if (m.find()) {
                        Double score = Double.parseDouble(m.group(1));
                        // 确保分数在合理范围内（0-100）
                        if (score >= 0 && score <= 100) {
                            setScoreByIndex(scores, i, score);
                            logger.info("从格式 '{}' 中提取到 {} 分数: {}", patternStr, dimensionName, score);
                            extractedCount++;
                            found = true;
                            break;
                        } else {
                            logger.warn("提取到的 {} 分数 {} 超出范围(0-100)，忽略", dimensionName, score);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("解析 {} 分数失败，模式: {}, 错误: {}", dimensionName, patternStr, e.getMessage());
                }
            }
            
            // 2. 匹配格式：教学基本功 72分 或 教学基本功 72（中间有空格）
            if (!found) {
                String[] patterns2 = {
                    dimensionName + "\\s+(\\d+(?:\\.\\d+)?)\\s*分",
                    dimensionName + "\\s+(\\d+(?:\\.\\d+)?)(?!\\d)"
                };
                for (String patternStr : patterns2) {
                    if (found) break;
                    try {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile(patternStr);
                        java.util.regex.Matcher m = p.matcher(text);
                        if (m.find()) {
                            Double score = Double.parseDouble(m.group(1));
                            if (score >= 0 && score <= 100) {
                                setScoreByIndex(scores, i, score);
                                logger.info("从空格格式中提取到 {} 分数: {}", dimensionName, score);
                                extractedCount++;
                                found = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // 忽略
                    }
                }
            }
            
            // 3. 匹配JSON格式："教学基本功": 72 或 "teachingFoundation": 72
            if (!found) {
                String[] patterns3 = {
                    "\"" + dimensionName + "\"\\s*[:：]\\s*(\\d+(?:\\.\\d+)?)",
                    "'" + dimensionName + "'\\s*[:：]\\s*(\\d+(?:\\.\\d+)?)"
                };
                for (String patternStr : patterns3) {
                    if (found) break;
                    try {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile(patternStr);
                        java.util.regex.Matcher m = p.matcher(text);
                        if (m.find()) {
                            Double score = Double.parseDouble(m.group(1));
                            if (score >= 0 && score <= 100) {
                                setScoreByIndex(scores, i, score);
                                logger.info("从JSON格式中提取到 {} 分数: {}", dimensionName, score);
                                extractedCount++;
                                found = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // 忽略
                    }
                }
            }
            
            // 4. 匹配英文key格式
            if (!found) {
                String pattern4 = "\"" + englishKeys[i] + "\"\\s*[:：]\\s*(\\d+(?:\\.\\d+)?)";
                try {
                    java.util.regex.Pattern p4 = java.util.regex.Pattern.compile(pattern4);
                    java.util.regex.Matcher m4 = p4.matcher(text);
                    if (m4.find()) {
                        Double score = Double.parseDouble(m4.group(1));
                        if (score >= 0 && score <= 100) {
                            setScoreByIndex(scores, i, score);
                            logger.info("从英文key格式中提取到 {} 分数: {}", dimensionName, score);
                            extractedCount++;
                            found = true;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("解析 {} 英文key分数失败: {}", dimensionName, e.getMessage());
                }
            }
            
            // 5. 如果还没找到，尝试更宽松的匹配：在维度名附近查找数字
            if (!found) {
                // 查找维度名后100个字符内的数字
                int nameIndex = text.indexOf(dimensionName);
                if (nameIndex != -1) {
                    int searchEnd = Math.min(nameIndex + 100, text.length());
                    String searchText = text.substring(nameIndex, searchEnd);
                    // 查找第一个数字
                    java.util.regex.Pattern p5 = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)");
                    java.util.regex.Matcher m5 = p5.matcher(searchText);
                    if (m5.find()) {
                        try {
                            Double score = Double.parseDouble(m5.group(1));
                            if (score >= 0 && score <= 100) {
                                setScoreByIndex(scores, i, score);
                                logger.info("从宽松匹配中提取到 {} 分数: {}", dimensionName, score);
                                extractedCount++;
                                found = true;
                            }
                        } catch (Exception e) {
                            // 忽略
                        }
                    }
                }
            }
            
            if (!found) {
                logger.warn("未能提取到 {} 的分数", dimensionName);
            }
        }
        
        logger.info("分数提取完成，成功提取 {} 个维度的分数", extractedCount);
        if (extractedCount == 0) {
            logger.warn("未能从文本中提取到任何分数，文本预览: {}", text.length() > 200 ? text.substring(0, 200) + "..." : text);
        }
        
        return scores;
    }
    
    /**
     * 根据索引设置分数
     */
    private void setScoreByIndex(HexagramScoreDto scores, int index, Double score) {
        switch (index) {
            case 0:
                scores.setTeachingFoundation(score);
                break;
            case 1:
                scores.setTeachingProcessDesign(score);
                break;
            case 2:
                scores.setTeachingManner(score);
                break;
            case 3:
                scores.setMultimediaAndBlackboard(score);
                break;
            case 4:
                scores.setClassroomAtmosphere(score);
                break;
            case 5:
                scores.setTimeRhythmControl(score);
                break;
        }
    }
    
    /**
     * 保存教师画像到数据库（保存描述和分数）
     * 如果该用户已存在画像记录，则覆盖更新；否则创建新记录
     */
    private void saveTeacherPortrait(Long userId, TeacherPortraitResponse response, String aiResponse, Integer historyCount) {
        try {
            // 先查询该用户是否已存在画像记录
            Optional<TeacherPortrait> existingPortraitOpt = portraitRepository.findFirstByUserIdOrderByCreateTimeDesc(userId);
            
            TeacherPortrait portrait;
            if (existingPortraitOpt.isPresent()) {
                // 如果存在，则更新现有记录
                portrait = existingPortraitOpt.get();
                logger.info("用户ID: {} 已存在画像记录，将覆盖更新，记录ID: {}", userId, portrait.getId());
            } else {
                // 如果不存在，则创建新记录
                portrait = new TeacherPortrait();
                portrait.setUserId(userId);
                logger.info("用户ID: {} 不存在画像记录，将创建新记录", userId);
            }
            
            // 更新/设置画像数据
            String description = response.getDescription();
            logger.info("准备保存description字段，长度: {}, 内容预览: {}", 
                    description != null ? description.length() : 0,
                    description != null && description.length() > 0 ? description.substring(0, Math.min(100, description.length())) : "空");
            
            portrait.setDescription(description);
            portrait.setScoringRule(response.getScoringRule());
            portrait.setAiResponse(aiResponse);
            portrait.setHistoryCount(historyCount);
            
            logger.info("已设置description字段到portrait对象，值: {}", 
                    portrait.getDescription() != null ? "非空，长度: " + portrait.getDescription().length() : "null");
            
            // 保存六芒星分数（如果存在）
            if (response.getHexagramScore() != null) {
                HexagramScoreDto scores = response.getHexagramScore();
                logger.info("准备保存六芒星分数: teachingFoundation={}, teachingProcessDesign={}, teachingManner={}, multimediaAndBlackboard={}, classroomAtmosphere={}, timeRhythmControl={}", 
                        scores.getTeachingFoundation(), scores.getTeachingProcessDesign(), scores.getTeachingManner(),
                        scores.getMultimediaAndBlackboard(), scores.getClassroomAtmosphere(), scores.getTimeRhythmControl());
                
                // 设置分数，即使为null也设置（覆盖旧值）
                portrait.setTeachingFoundation(scores.getTeachingFoundation());
                portrait.setTeachingProcessDesign(scores.getTeachingProcessDesign());
                portrait.setTeachingManner(scores.getTeachingManner());
                portrait.setMultimediaAndBlackboard(scores.getMultimediaAndBlackboard());
                portrait.setClassroomAtmosphere(scores.getClassroomAtmosphere());
                portrait.setTimeRhythmControl(scores.getTimeRhythmControl());
                
                // 验证保存的分数
                logger.info("验证保存的分数 - teachingFoundation={}, teachingProcessDesign={}, teachingManner={}, multimediaAndBlackboard={}, classroomAtmosphere={}, timeRhythmControl={}", 
                        portrait.getTeachingFoundation(), portrait.getTeachingProcessDesign(), portrait.getTeachingManner(),
                        portrait.getMultimediaAndBlackboard(), portrait.getClassroomAtmosphere(), portrait.getTimeRhythmControl());
                
                logger.info("六芒星分数已设置到portrait对象");
            } else {
                logger.warn("response.getHexagramScore() 为 null，无法保存分数");
            }
            
            // 保存到数据库
            TeacherPortrait savedPortrait = portraitRepository.save(portrait);
            logger.info("教师画像已保存到数据库，用户ID: {}, 操作类型: {}, 记录ID: {}", 
                    userId, existingPortraitOpt.isPresent() ? "更新" : "创建", savedPortrait.getId());
            
            // 验证保存后的数据
            logger.info("保存后验证数据库中的分数 - teachingFoundation={}, teachingProcessDesign={}, teachingManner={}, multimediaAndBlackboard={}, classroomAtmosphere={}, timeRhythmControl={}", 
                    savedPortrait.getTeachingFoundation(), savedPortrait.getTeachingProcessDesign(), savedPortrait.getTeachingManner(),
                    savedPortrait.getMultimediaAndBlackboard(), savedPortrait.getClassroomAtmosphere(), savedPortrait.getTimeRhythmControl());
        } catch (Exception e) {
            logger.error("保存教师画像到数据库失败", e);
            // 不抛出异常，因为画像生成已经成功，保存失败不影响返回结果
        }
    }
    
    /**
     * 生成打分规则说明
     */
    private String generateScoringRule(List<VideoAnalysisRecord> records) {
        if (records.isEmpty()) {
            return "无历史记录";
        }
        
        LocalDateTime earliestTime = records.get(0).getCreateTime();
        LocalDateTime latestTime = records.get(records.size() - 1).getCreateTime();
        long daysBetween = ChronoUnit.DAYS.between(earliestTime, latestTime);
        
        StringBuilder rule = new StringBuilder();
        rule.append("打分规则说明：\n");
        rule.append("1. 时间权重计算：历史记录按时间排序，时间越近的权重越高。\n");
        rule.append("2. 权重分配：采用指数衰减权重，最近一次记录的权重最高，最早一次记录的权重最低。\n");
        rule.append("3. 时间范围：从").append(earliestTime.toLocalDate()).append("到").append(latestTime.toLocalDate());
        if (daysBetween > 0) {
            rule.append("，共").append(daysBetween).append("天");
        }
        rule.append("。\n");
        rule.append("4. 记录数量：共使用").append(records.size()).append("条历史记录进行综合分析。\n");
        rule.append("5. 评分维度：教学基本功、教学过程设计、教态、多媒体与板书运用、课堂气氛、时间节奏掌控（每项0-100分）。\n");
        rule.append("6. 综合计算：根据各历史记录的权重，加权平均计算各项得分。");
        
        return rule.toString();
    }
    
    @Override
    public TeacherPortraitResponse getTeacherPortrait(Long userId) {
        try {
            Optional<TeacherPortrait> portraitOpt = portraitRepository.findFirstByUserIdOrderByCreateTimeDesc(userId);
            if (!portraitOpt.isPresent()) {
                return null;
            }
            
            TeacherPortrait portrait = portraitOpt.get();
            
            // 查询用户信息
            if (userId == null) {
                logger.error("查询教师画像时userId为null");
                return null;
            }
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return null;
            }
            User user = userOpt.get();
            
            // 转换为响应DTO
            TeacherPortraitResponse response = new TeacherPortraitResponse();
            response.setUserId(userId);
            response.setUsername(user.getUsername());
            response.setAvatarUrl(user.getAvatarUrl());
            response.setAge(user.getAge());
            response.setSubject(user.getSubject());
            response.setDescription(portrait.getDescription());
            response.setScoringRule(portrait.getScoringRule());
            response.setHistoryCount(portrait.getHistoryCount());
            
            // 设置六芒星分数（从数据库读取）
            HexagramScoreDto scores = new HexagramScoreDto();
            scores.setTeachingFoundation(portrait.getTeachingFoundation());
            scores.setTeachingProcessDesign(portrait.getTeachingProcessDesign());
            scores.setTeachingManner(portrait.getTeachingManner());
            scores.setMultimediaAndBlackboard(portrait.getMultimediaAndBlackboard());
            scores.setClassroomAtmosphere(portrait.getClassroomAtmosphere());
            scores.setTimeRhythmControl(portrait.getTimeRhythmControl());
            
            logger.info("从数据库读取的六芒星分数: teachingFoundation={}, teachingProcessDesign={}, teachingManner={}, multimediaAndBlackboard={}, classroomAtmosphere={}, timeRhythmControl={}", 
                    scores.getTeachingFoundation(), scores.getTeachingProcessDesign(), scores.getTeachingManner(),
                    scores.getMultimediaAndBlackboard(), scores.getClassroomAtmosphere(), scores.getTimeRhythmControl());
            
            response.setHexagramScore(scores);
            
            return response;
        } catch (Exception e) {
            logger.error("查询教师画像失败，用户ID: {}", userId, e);
            return null;
        }
    }
    
    @Override
    public List<TeacherPortraitResponse> getAllTeacherPortraits() {
        try {
            // 查询所有用户
            List<User> allUsers = userRepository.findAll();
            logger.info("查询到所有用户数量: {}", allUsers.size());
            
            // 过滤掉 role 为 'admin' 的用户
            List<User> filteredUsers = new ArrayList<>();
            for (User user : allUsers) {
                if (user.getRole() == null || !"admin".equalsIgnoreCase(user.getRole())) {
                    filteredUsers.add(user);
                }
            }
            logger.info("过滤掉admin用户后，剩余用户数量: {}", filteredUsers.size());
            
            // 查询所有教师画像记录（每个用户只取最新的）
            List<TeacherPortrait> allPortraits = portraitRepository.findAll();
            logger.info("查询到所有教师画像记录数量: {}", allPortraits.size());
            
            // 按用户ID分组，每个用户只保留最新的画像记录
            Map<Long, TeacherPortrait> latestPortraits = new HashMap<>();
            for (TeacherPortrait portrait : allPortraits) {
                Long userId = portrait.getUserId();
                if (!latestPortraits.containsKey(userId) || 
                    portrait.getCreateTime().isAfter(latestPortraits.get(userId).getCreateTime())) {
                    latestPortraits.put(userId, portrait);
                }
            }
            
            logger.info("去重后教师画像数量: {}", latestPortraits.size());
            
            // 为每个用户创建响应DTO（包括没有画像的用户）
            List<TeacherPortraitResponse> responseList = new ArrayList<>();
            for (User user : filteredUsers) {
                Long userId = user.getId();
                TeacherPortrait portrait = latestPortraits.get(userId);
                
                // 转换为响应DTO
                TeacherPortraitResponse response = new TeacherPortraitResponse();
                response.setUserId(userId);
                response.setUsername(user.getUsername());
                response.setAvatarUrl(user.getAvatarUrl());
                response.setAge(user.getAge());
                response.setSubject(user.getSubject());
                
                // 如果有画像数据，则填充画像信息
                if (portrait != null) {
                    response.setDescription(portrait.getDescription());
                    response.setScoringRule(portrait.getScoringRule());
                    response.setHistoryCount(portrait.getHistoryCount());
                    
                    // 设置六芒星分数
                    HexagramScoreDto scores = new HexagramScoreDto();
                    scores.setTeachingFoundation(portrait.getTeachingFoundation());
                    scores.setTeachingProcessDesign(portrait.getTeachingProcessDesign());
                    scores.setTeachingManner(portrait.getTeachingManner());
                    scores.setMultimediaAndBlackboard(portrait.getMultimediaAndBlackboard());
                    scores.setClassroomAtmosphere(portrait.getClassroomAtmosphere());
                    scores.setTimeRhythmControl(portrait.getTimeRhythmControl());
                    response.setHexagramScore(scores);
                } else {
                    // 没有画像数据，设置为空或默认值
                    response.setDescription(null);
                    response.setScoringRule(null);
                    response.setHistoryCount(0);
                    response.setHexagramScore(null);
                }
                
                responseList.add(response);
            }
            
            // 按用户名排序
            responseList.sort((a, b) -> {
                return a.getUsername().compareTo(b.getUsername());
            });
            
            logger.info("返回教师画像列表，数量: {} (包含所有用户)", responseList.size());
            return responseList;
        } catch (Exception e) {
            logger.error("查询所有教师画像失败", e);
            return new ArrayList<>();
        }
    }
}

