package com.moke.assistant.service.impl;

import com.moke.assistant.dto.ChatCompletionRequest;
import com.moke.assistant.dto.ChatCompletionResponse;
import com.moke.assistant.dto.VideoAnalysisResponse;
import com.moke.assistant.entity.VideoAnalysisRecord;
import com.moke.assistant.repository.VideoAnalysisRecordRepository;
import com.moke.assistant.service.VideoAnalysisService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频分析服务实现类
 */
@Service
public class VideoAnalysisServiceImpl implements VideoAnalysisService {

    private static final String AI_SERVICE_URL = "http://ai-service.tal.com/openai-compatible/v1/chat/completions";
    private static final String API_KEY = "300000775:568f52f31bf0782018145f7dc56c3afb";
    private static final String PROMPT_TEXT = "请作为专业的教育教学诊断专家，调用多模态能力分析以下课堂视频，结合可选的教案核心目标，生成一份专业的中小学/初高中[需替换为对应学段]磨课诊断报告（各评价维度按满分10分打分）。报告需包含四大核心模块：1. 量化数据（平均语速、口头禅频次、非必要停顿次数、眼神交流比例估算、提问次数、互动覆盖率、各环节时间占比，数据均从视频中提取估算）；2. 分维度诊断（严格按以下10个评价维度及标准执行，每个维度按满分10分打分，需完成三项核心内容：①给出维度得分及得分依据；②结合视频具体教学场景做评价，若发现问题需做实质性描述（避免笼统表述，明确‘问题是什么、在哪个环节出现、造成的影响’）；③每个实质性问题需精准对应视频时间段（精确到秒，格式为“00:XX:XX-00:XX:XX”），并关联可直接跳转的视频时间戳链接，同时匹配1条具体可落地的优化建议（建议需针对性解决该问题，具备操作指导性）；单个维度下有多类问题时，按“问题描述+对应时间点+优化建议”的组合逐条罗列）。10个评价维度及标准如下：① 教师仪表与教态：仪表端庄，教态亲切、自然；需细化评价表情、眼神、手势、站姿走姿、服饰等细节；② 课堂导入：温故到位，导入新课自然、有趣；需结合8种常用导入方法（复习导入、情境导入、悬念导入、故事导入、实验导入、问题导入、游戏导入、多媒体导入）分析适配性；③ 教学语言：语言流利，表达顺畅、有感染力、逻辑性强，普通话标准并富有情感；④ 重点难点把握：讲练突出、适当，符合知识形成的建构理论（关注“最近发展区”与“未知区”）；⑤ 多媒体运用：辅助教学手段使用得当，恰当使用PPT；严格遵循“辅助”原则，评价是否存在过度使用问题；⑥ 教学方法与理念：是否采用学为主体，教为主导，启发诱导，学思结合的当代教育新理念；⑦ 课堂气氛与互动：课堂气氛活跃，双边活动充分，“收、放”恰当，具有有效性和可控性；⑧ 学生课堂状态（“六种状态”）：关注并评价学生的注意、参与、交往、思维、情绪、生成六种状态；⑨ 板书设计：板书工整，书写速度快，主、副区布局合理；⑩ 时间分布：各教学环节时间分布合理，契合教学目标与课堂节奏。\n" +
            "3.  课堂提问清单（含所有从视频中识别的提问内容、对应时间戳及质量评分（满分10分），评分需结合上述“重点难点把握”“教学语言”等维度标准）；\n" +
            "4.  综合改进行动汇总（整合各维度核心建议，形成可落地、可量化的具体任务项，明确“优化动作、执行步骤、预期效果”，支持教师直接参照执行）。要求语言专业、客观，数据估算精准，问题定位清晰，时间戳链接可直接跳转至对应视频片段，所有建议均具备强可操作性，符合[对应学科]教学规范及该学段教学要求。输入素材：课堂视频文件{此处插入视频文件路径/标识符}，可选教案核心目标{此处插入教案核心目标，无则填“无”}";

    private final RestTemplate restTemplate;
    private final VideoAnalysisRecordRepository recordRepository;

    public VideoAnalysisServiceImpl(VideoAnalysisRecordRepository recordRepository) {
        this.restTemplate = new RestTemplate();
        this.recordRepository = recordRepository;
    }

    @Override
    public VideoAnalysisResponse analyzeVideo(String videoUrl) {
        try {
            // 构建请求
            ChatCompletionRequest request = buildRequest(videoUrl);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", API_KEY);
            
            HttpEntity<ChatCompletionRequest> entity = new HttpEntity<>(request, headers);
            
            // 调用API
            ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(
                    AI_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    ChatCompletionResponse.class
            );
            
            // 提取message内容
            ChatCompletionResponse responseBody = response.getBody();
            if (responseBody == null || responseBody.getChoices() == null || responseBody.getChoices().isEmpty()) {
                throw new RuntimeException("API返回数据为空");
            }
            
            String messageContent = responseBody.getChoices().get(0).getMessage().getContent();
            
            // 格式化消息
            String formattedMessage = formatMessage(messageContent);
            
            // 提取标题（从分析结果中提取"课题："后面的内容）
            String title = extractTitle(formattedMessage);
            if (title == null || title.isEmpty()) {
                // 如果无法提取标题，尝试从原始消息中提取
                title = extractTitle(messageContent);
            }
            
            // 生成带时间戳的标题
            String titleWithTimestamp = generateTitleWithTimestamp(title);
            
            // 保存到数据库
            VideoAnalysisRecord record = new VideoAnalysisRecord();
            record.setVideoUrl(videoUrl);
            record.setTitle(titleWithTimestamp);
            record.setRawMessage(messageContent);
            record.setFormattedMessage(formattedMessage);
            
            // 从用户上下文获取当前登录用户ID
            Long userId = com.moke.assistant.common.utils.UserContext.getUserId();
            if (userId != null) {
                record.setCreateUserId(userId);
                System.out.println("设置create_user_id: " + userId);
            } else {
                System.out.println("警告: UserContext.getUserId()返回null，create_user_id将不会被设置");
            }
            
            recordRepository.save(record);
            System.out.println("视频分析记录已保存，ID: " + record.getId() + ", create_user_id: " + record.getCreateUserId());
            
            // 构建响应
            VideoAnalysisResponse result = new VideoAnalysisResponse();
            result.setRawMessage(messageContent);
            result.setFormattedMessage(formattedMessage);
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("调用视频分析API失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建API请求对象
     */
    private ChatCompletionRequest buildRequest(String videoUrl) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        
        // 创建消息内容
        List<ChatCompletionRequest.ContentItem> contentItems = new ArrayList<>();
        
        // 文本内容
        ChatCompletionRequest.ContentItem textItem = new ChatCompletionRequest.ContentItem();
        textItem.setType("text");
        textItem.setText(PROMPT_TEXT);
        contentItems.add(textItem);
        
        // 视频URL内容
        ChatCompletionRequest.ContentItem videoItem = new ChatCompletionRequest.ContentItem();
        videoItem.setType("video_url");
        ChatCompletionRequest.VideoUrl videoUrlObj = new ChatCompletionRequest.VideoUrl();
        videoUrlObj.setUrl(videoUrl);
        videoItem.setVideoUrl(videoUrlObj);
        contentItems.add(videoItem);
        
        // 创建消息
        ChatCompletionRequest.Message message = new ChatCompletionRequest.Message();
        message.setRole("user");
        message.setContent(contentItems);
        
        // 设置消息列表
        List<ChatCompletionRequest.Message> messages = new ArrayList<>();
        messages.add(message);
        request.setMessages(messages);
        
        // 设置流选项
        ChatCompletionRequest.StreamOptions streamOptions = new ChatCompletionRequest.StreamOptions();
        streamOptions.setIncludeUsage(false);
        request.setStreamOptions(streamOptions);
        
        return request;
    }

    /**
     * 格式化消息内容，去除冗余标识符并自动换行
     * 1. 去除所有Markdown标题标记（#、##、###等）
     * 2. 在段落之间增加换行
     * 3. 保持文本可读性
     */
    private String formatMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        // 第一步：去除所有Markdown标题标记（#、##、###、####等）
        String formatted = message
                // 去除Markdown标题标记（行首的#号，包括空格）
                .replaceAll("^#{1,6}\\s+", "")  // 行首的标题标记
                .replaceAll("\\n#{1,6}\\s+", "\n")  // 换行后的标题标记
                // 去除Markdown代码块标记
                .replaceAll("```[\\w]*\\n?", "")
                .replaceAll("```", "")
                // 去除多余的星号（Markdown粗体/斜体标记）
                .replaceAll("\\*{2,}", "")
                // 统一换行符
                .replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n");
        
        // 第二步：按行处理，去除每行的标题标记和多余空白
        String[] lines = formatted.split("\n");
        StringBuilder sb = new StringBuilder();
        boolean lastLineWasEmpty = false;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // 去除行首的标题标记（防止有遗漏）
            line = line.replaceAll("^#{1,6}\\s*", "");
            
            // 去除行首行尾空白
            line = line.trim();
            
            // 跳过空行，但保留段落之间的空行
            if (line.isEmpty()) {
                if (!lastLineWasEmpty && sb.length() > 0) {
                    // 如果上一行不是空行，且当前行是空行，添加一个换行（段落分隔）
                    sb.append("\n");
                    lastLineWasEmpty = true;
                }
                continue;
            }
            
            // 如果上一行不是空行，且当前行不是空行，检查是否需要添加换行
            if (!lastLineWasEmpty && sb.length() > 0) {
                // 检查是否是新的段落开始（如：数字编号、中文编号、特殊标记等）
                boolean isNewParagraph = line.matches("^[\\d一二三四五六七八九十]+[.、：:]\\s*.*")  // 数字或中文编号
                        || line.matches("^[【（(].*[】）)]\\s*.*")  // 带括号的标题
                        || line.matches("^[第].*[章节部分项]\\s*.*")  // 第X章/节/部分
                        || line.matches("^[一二三四五六七八九十]+[.、]\\s*.*");  // 纯中文编号
                
                if (isNewParagraph) {
                    // 新段落前添加空行
                    sb.append("\n");
                }
            }
            
            // 添加当前行
            if (sb.length() > 0 && !sb.toString().endsWith("\n")) {
                sb.append("\n");
            }
            sb.append(line);
            lastLineWasEmpty = false;
        }
        
        formatted = sb.toString();
        
        // 第三步：清理多余的空行，确保段落之间最多只有一个空行
        formatted = formatted.replaceAll("\n{3,}", "\n\n");
        
        // 第四步：最终清理，去除每行首尾空白，但保留段落结构
        lines = formatted.split("\n");
        sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                // 如果不是第一行，且上一行不是空行，添加换行
                if (sb.length() > 0 && !sb.toString().endsWith("\n\n")) {
                    sb.append("\n");
                }
                sb.append(line);
            } else if (i > 0 && i < lines.length - 1) {
                // 保留段落之间的空行（但确保不会连续多个空行）
                String current = sb.toString();
                if (!current.isEmpty() && !current.endsWith("\n\n")) {
                    sb.append("\n");
                }
            }
        }
        
        return sb.toString().trim();
    }

    /**
     * 从分析结果中提取标题（课题）
     * 
     * @param message 分析结果消息
     * @return 提取的标题，如果未找到则返回null
     */
    private String extractTitle(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }
        
        // 查找"课题："后面的内容
        String[] lines = message.split("\n");
        for (String line : lines) {
            line = line.trim();
            // 匹配格式：课题：[学科] · [知识点] 或 课题：[内容]
            if (line.contains("课题：") || line.contains("课题:")) {
                int index = line.indexOf("课题：");
                if (index == -1) {
                    index = line.indexOf("课题:");
                }
                if (index != -1) {
                    String title = line.substring(index + 3).trim();
                    // 如果标题包含换行符，只取第一行
                    if (title.contains("\n")) {
                        title = title.split("\n")[0].trim();
                    }
                    // 去除可能的后续内容（如"综合等级"等）
                    if (title.contains("综合等级")) {
                        title = title.substring(0, title.indexOf("综合等级")).trim();
                    }
                    return title.isEmpty() ? null : title;
                }
            }
        }
        
        return null;
    }

    /**
     * 生成带时间戳的标题
     * 格式：标题 + 年月日时分
     * 
     * @param title 原始标题
     * @return 带时间戳的标题
     */
    private String generateTitleWithTimestamp(String title) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format("%04d%02d%02d%02d%02d",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute());
        
        // 如果标题为空，使用默认标题
        if (title == null || title.isEmpty()) {
            return "视频分析_" + timestamp;
        }
        
        // 组合标题和时间戳
        return title + "_" + timestamp;
    }
}

