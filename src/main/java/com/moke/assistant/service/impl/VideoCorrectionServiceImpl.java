package com.moke.assistant.service.impl;

import com.moke.assistant.dto.HistoryItemDto;
import com.moke.assistant.dto.VideoCorrectionResultDto;
import com.moke.assistant.entity.VideoAnalysisRecord;
import com.moke.assistant.repository.VideoAnalysisRecordRepository;
import com.moke.assistant.service.VideoCorrectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VideoCorrectionServiceImpl implements VideoCorrectionService {

    @Autowired
    private VideoAnalysisRecordRepository videoAnalysisRecordRepository;

    /**
     * Demo 版本：使用内存 Map 模拟任务存储。
     * 后续可以替换为数据库或缓存。
     */
    private final Map<String, VideoCorrectionResultDto> taskStore = new ConcurrentHashMap<>();
    
    /**
     * 存储任务元数据（标题、创建时间等）
     */
    private final Map<String, TaskMetadata> taskMetadataStore = new ConcurrentHashMap<>();

    private static class TaskMetadata {
        String title;
        LocalDateTime createTime;

        TaskMetadata(String title, LocalDateTime createTime) {
            this.title = title;
            this.createTime = createTime;
        }
    }

    @Override
    public String createTask(MultipartFile file, String title, Long userId) {
        // TODO: 在此处保存文件到磁盘或云存储，并异步调用 AI 服务分析视频
        String taskId = UUID.randomUUID().toString();

        // 保存任务元数据
        String finalTitle = (title != null && !title.trim().isEmpty()) 
            ? title.trim() 
            : "试讲视频_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        taskMetadataStore.put(taskId, new TaskMetadata(finalTitle, LocalDateTime.now()));

        // 示例：直接构造一个"已完成"的假数据
        VideoCorrectionResultDto result = new VideoCorrectionResultDto();
        result.setTaskId(taskId);
        result.setStatus("DONE");
        result.setSummary("这是对老师试讲内容的总体概述示例。后续可接入语音转写和大模型分析生成真实内容。");
        result.setStructureAdvice("1. 导入环节略显简短，可增加与生活场景的联系；\n2. 知识点之间的过渡可以更加自然。");
        result.setInteractionAdvice("建议每 5 分钟至少一次提问或互动，鼓励学生表达想法。");
        result.setLanguageAdvice("语速略快，建议控制在适中水平，并适当增加停顿以便学生思考。");

        taskStore.put(taskId, result);
        
        // 保存到video_analysis_record表，持久化userId
        try {
            // 构建视频URL（暂时使用占位符，后续文件保存后可更新）
            String videoUrl = "video-correction://" + taskId;
            if (file != null && !file.isEmpty()) {
                // 如果有文件名，可以包含在URL中
                String fileName = file.getOriginalFilename();
                if (fileName != null) {
                    videoUrl = "video-correction://" + taskId + "/" + fileName;
                }
            }
            
            // 构建分析结果（将纠偏结果合并为格式化消息）
            StringBuilder formattedMessage = new StringBuilder();
            formattedMessage.append("【总体概述】\n").append(result.getSummary()).append("\n\n");
            formattedMessage.append("【结构建议】\n").append(result.getStructureAdvice()).append("\n\n");
            formattedMessage.append("【互动建议】\n").append(result.getInteractionAdvice()).append("\n\n");
            formattedMessage.append("【语言建议】\n").append(result.getLanguageAdvice());
            
            VideoAnalysisRecord record = new VideoAnalysisRecord();
            record.setVideoUrl(videoUrl);
            record.setTitle(finalTitle);
            record.setFormattedMessage(formattedMessage.toString());
            record.setRawMessage(result.getSummary()); // 原始消息使用summary
            if (userId != null) {
                record.setCreateUserId(userId);
            }
            
            videoAnalysisRecordRepository.save(record);
        } catch (Exception e) {
            // 记录错误但不影响主流程
            System.err.println("保存视频分析记录失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return taskId;
    }

    @Override
    public VideoCorrectionResultDto getResult(String taskId) {
        VideoCorrectionResultDto dto = taskStore.get(taskId);
        if (dto == null) {
            VideoCorrectionResultDto pending = new VideoCorrectionResultDto();
            pending.setTaskId(taskId);
            pending.setStatus("PROCESSING");
            pending.setSummary("分析中，请稍后刷新查看。");
            return pending;
        }
        return dto;
    }

    @Override
    public List<HistoryItemDto> getHistoryList() {
        List<HistoryItemDto> historyList = new ArrayList<>();
        
        for (Map.Entry<String, VideoCorrectionResultDto> entry : taskStore.entrySet()) {
            String taskId = entry.getKey();
            VideoCorrectionResultDto result = entry.getValue();
            TaskMetadata metadata = taskMetadataStore.get(taskId);
            
            HistoryItemDto item = new HistoryItemDto();
            item.setTaskId(taskId);
            item.setStatus(result.getStatus());
            item.setSummary(result.getSummary());
            
            if (metadata != null) {
                item.setTitle(metadata.title);
                item.setCreateTime(metadata.createTime);
            } else {
                item.setTitle("试讲视频");
                item.setCreateTime(LocalDateTime.now());
            }
            
            historyList.add(item);
        }
        
        // 按创建时间倒序排序
        historyList.sort(Comparator.comparing(HistoryItemDto::getCreateTime).reversed());
        
        return historyList;
    }
}


