package com.moke.assistant.service.impl;

import com.moke.assistant.common.exception.ServiceException;
import com.moke.assistant.common.utils.UserContext;
import com.moke.assistant.dto.ScriptGenerationRequest;
import com.moke.assistant.dto.ScriptGenerationResponse;
import com.moke.assistant.entity.ClassroomStudent;
import com.moke.assistant.entity.LessonPlan;
import com.moke.assistant.entity.Script;
import com.moke.assistant.entity.StudentPortrait;
import com.moke.assistant.repository.ClassroomStudentRepository;
import com.moke.assistant.repository.LessonPlanRepository;
import com.moke.assistant.repository.ScriptRepository;
import com.moke.assistant.repository.StudentPortraitRepository;
import com.moke.assistant.service.ScriptGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 逐字稿生成服务实现类
 */
@Service
public class ScriptGenerationServiceImpl implements ScriptGenerationService {

    private static final String AI_SERVICE_URL = "http://ai-service.tal.com/openai-compatible/v1/chat/completions";
    private static final String API_KEY = "300000775:568f52f31bf0782018145f7dc56c3afb";
    private static final String MODEL_NAME = "gpt-5.1";

    private static final Logger logger = LoggerFactory.getLogger(ScriptGenerationServiceImpl.class);

    private final RestTemplate restTemplate;
    private final LessonPlanRepository lessonPlanRepository;
    private final StudentPortraitRepository studentPortraitRepository;
    private final ScriptRepository scriptRepository;
    private final ClassroomStudentRepository classroomStudentRepository;

    public ScriptGenerationServiceImpl(LessonPlanRepository lessonPlanRepository,
                                      StudentPortraitRepository studentPortraitRepository,
                                      ScriptRepository scriptRepository,
                                      ClassroomStudentRepository classroomStudentRepository) {
        // 配置 RestTemplate 超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 连接超时：10秒
        factory.setReadTimeout(300000); // 读取超时：5分钟（AI生成可能需要较长时间）
        this.restTemplate = new RestTemplate(factory);
        this.lessonPlanRepository = lessonPlanRepository;
        this.studentPortraitRepository = studentPortraitRepository;
        this.scriptRepository = scriptRepository;
        this.classroomStudentRepository = classroomStudentRepository;
    }

    @Override
    public Script generateScript(Long lessonPlanId) {
        try {
            logger.info("开始生成逐字稿，教案ID: {}", lessonPlanId);

            // 1. 读取教案内容
            Optional<LessonPlan> lessonPlanOpt = lessonPlanRepository.findById(lessonPlanId);
            if (!lessonPlanOpt.isPresent()) {
                throw new ServiceException("教案不存在，ID: " + lessonPlanId);
            }
            LessonPlan lessonPlan = lessonPlanOpt.get();
            String lessonContent = lessonPlan.getContent();
            if (lessonContent == null || lessonContent.trim().isEmpty()) {
                throw new ServiceException("教案内容为空，无法生成逐字稿");
            }
            String lessonTitle = lessonPlan.getTitle();

            // 2. 读取关联课堂的学生画像信息
            List<StudentPortrait> students = new ArrayList<>();
            if (lessonPlan.getClassroomId() != null) {
                List<ClassroomStudent> classroomStudents = classroomStudentRepository.findByClassroomId(lessonPlan.getClassroomId());
                List<Long> studentIds = new ArrayList<>();
                for (ClassroomStudent cs : classroomStudents) {
                    // 使用 student 关联对象获取ID（LAZY加载，需要确保在事务中）
                    if (cs.getStudent() != null) {
                        studentIds.add(cs.getStudent().getId());
                    }
                }
                if (!studentIds.isEmpty()) {
                    students = studentPortraitRepository.findAllById(studentIds);
                    logger.info("读取到关联课堂的学生信息数量: {}", students.size());
                } else {
                    logger.warn("关联课堂中没有学生信息");
                }
            } else {
                logger.warn("教案未关联课堂，无法获取学生信息");
            }

            // 3. 构建AI Prompt
            String prompt = buildPrompt(lessonContent, students);

            // 4. 调用AI服务生成逐字稿
            String scriptContent = callAIService(prompt);
            logger.info("AI服务调用成功，逐字稿长度: {}", scriptContent.length());

            // 5. 保存到数据库
            Script script = new Script();
            script.setLessonPlanId(lessonPlanId);
            script.setTitle(lessonTitle + " - 逐字稿");
            script.setContent(scriptContent);
            // UserContext.getUserId()可能为null，允许为空
            Long userId = UserContext.getUserId();
            if (userId != null) {
                script.setCreateUserId(userId);
            }

            // 如果已存在逐字稿，则更新；否则创建新的
            Optional<Script> existingScript = scriptRepository.findByLessonPlanId(lessonPlanId);
            if (existingScript.isPresent()) {
                Script existing = existingScript.get();
                existing.setTitle(script.getTitle());
                existing.setContent(script.getContent());
                existing.setCreateUserId(script.getCreateUserId());
                script = scriptRepository.save(existing);
                logger.info("更新逐字稿，ID: {}", script.getId());
            } else {
                script = scriptRepository.save(script);
                logger.info("创建逐字稿，ID: {}", script.getId());
            }

            return script;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("生成逐字稿失败", e);
            throw new ServiceException("生成逐字稿失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Script getScriptByLessonPlanId(Long lessonPlanId) {
        Optional<Script> scriptOpt = scriptRepository.findByLessonPlanId(lessonPlanId);
        return scriptOpt.orElse(null);
    }

    @Override
    public Script getScriptById(Long id) {
        Optional<Script> scriptOpt = scriptRepository.findById(id);
        return scriptOpt.orElse(null);
    }

    /**
     * 构建AI Prompt
     */
    private String buildPrompt(String lessonContent, List<StudentPortrait> students) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("你是一位经验丰富的教师，需要根据以下教案内容生成一堂课的逐字稿。\n\n");
        promptBuilder.append("教案内容：\n");
        promptBuilder.append(lessonContent);
        promptBuilder.append("\n\n");

        if (students != null && !students.isEmpty()) {
            promptBuilder.append("学生信息：\n");
            for (StudentPortrait student : students) {
                promptBuilder.append(student.getStudentName());
                if (student.getCharacteristics() != null && !student.getCharacteristics().trim().isEmpty()) {
                    promptBuilder.append("：").append(student.getCharacteristics());
                }
                promptBuilder.append("\n");
            }
            promptBuilder.append("\n");
        } else {
            promptBuilder.append("学生信息：暂无学生信息，请使用通用的学生角色。\n\n");
        }

        promptBuilder.append("要求：\n");
        promptBuilder.append("1. 生成完整的课堂逐字稿，包含教师的讲解、提问和学生的回答\n");
        promptBuilder.append("2. 根据每个学生的特点设计符合其性格和能力的回答\n");
        promptBuilder.append("3. 逐字稿格式：明确标注\"老师：\"和\"学生姓名：\"，对话自然流畅\n");
        promptBuilder.append("4. 包含课堂的各个环节：导入、新授、练习、总结等\n");
        if (students != null && !students.isEmpty()) {
            promptBuilder.append("5. 学生的回答要符合其特点（如：懒羊羊可能回答简单或沉默，喜羊羊回答准确深入等）\n");
        }
        promptBuilder.append("6. 逐字稿要详细、完整，能够真实反映课堂教学过程\n");

        return promptBuilder.toString();
    }

    /**
     * 调用AI服务
     */
    private String callAIService(String prompt) {
        try {
            logger.info("开始调用AI服务，Prompt长度: {}", prompt.length());
            
            ScriptGenerationRequest request = new ScriptGenerationRequest();
            request.setModel(MODEL_NAME);
            request.setReasoningEffort("high");

            List<ScriptGenerationRequest.Message> messages = new ArrayList<>();
            ScriptGenerationRequest.Message systemMessage = new ScriptGenerationRequest.Message();
            systemMessage.setRole("system");
            systemMessage.setContent("你是一位经验丰富的教师，擅长根据教案生成详细的课堂逐字稿。");
            messages.add(systemMessage);

            ScriptGenerationRequest.Message userMessage = new ScriptGenerationRequest.Message();
            userMessage.setRole("user");
            userMessage.setContent(prompt);
            messages.add(userMessage);

            request.setMessages(messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            HttpEntity<ScriptGenerationRequest> entity = new HttpEntity<>(request, headers);
            
            logger.info("发送AI服务请求，URL: {}, Model: {}", AI_SERVICE_URL, MODEL_NAME);
            long startTime = System.currentTimeMillis();
            
            ResponseEntity<ScriptGenerationResponse> response = restTemplate.exchange(
                    AI_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    ScriptGenerationResponse.class
            );

            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.info("AI服务响应时间: {}ms, 状态码: {}", elapsedTime, response.getStatusCodeValue());

            ScriptGenerationResponse responseBody = response.getBody();
            
            // 详细记录响应内容用于调试
            if (responseBody == null) {
                logger.error("AI服务返回的响应体为null");
                throw new ServiceException("AI服务返回空响应体");
            }
            
            logger.info("AI服务响应 - ID: {}, Model: {}, Choices数量: {}", 
                    responseBody.getId(), 
                    responseBody.getModel(),
                    responseBody.getChoices() != null ? responseBody.getChoices().size() : 0);
            
            if (responseBody.getChoices() != null && !responseBody.getChoices().isEmpty()) {
                ScriptGenerationResponse.Choice firstChoice = responseBody.getChoices().get(0);
                ScriptGenerationResponse.Message message = firstChoice.getMessage();
                
                if (message != null) {
                    String content = message.getContent();
                    logger.info("AI服务返回内容长度: {}, FinishReason: {}", 
                            content != null ? content.length() : 0,
                            firstChoice.getFinishReason());
                    
                    if (content != null && !content.trim().isEmpty()) {
                        return formatContent(content);
                    } else {
                        logger.error("AI服务返回的内容为空，FinishReason: {}", firstChoice.getFinishReason());
                        throw new ServiceException("AI服务返回空内容，FinishReason: " + firstChoice.getFinishReason());
                    }
                } else {
                    logger.error("AI服务返回的Message为null");
                    throw new ServiceException("AI服务返回的Message为null");
                }
            } else {
                logger.error("AI服务返回的Choices为空或为空列表");
                if (responseBody.getChoices() == null) {
                    throw new ServiceException("AI服务返回空结果：Choices为null");
                } else {
                    throw new ServiceException("AI服务返回空结果：Choices列表为空");
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("调用AI服务超时或连接失败", e);
            throw new ServiceException("调用AI服务超时或连接失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("调用AI服务失败", e);
            throw new ServiceException("调用AI服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 格式化内容
     */
    private String formatContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        // 处理换行符，确保格式正确
        return content.trim();
    }
}

