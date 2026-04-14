package com.moke.assistant.service;

import com.moke.assistant.dto.HistoryItemDto;
import com.moke.assistant.dto.VideoCorrectionResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoCorrectionService {

    /**
     * 创建视频纠偏任务，返回任务 ID。
     *
     * @param file  试讲视频文件
     * @param title 可选标题
     * @param userId 用户ID（从Cookie中获取）
     * @return 任务 ID
     */
    String createTask(MultipartFile file, String title, Long userId);

    /**
     * 根据任务 ID 获取分析结果。
     *
     * @param taskId 任务 ID
     * @return 结果 DTO（可能为处理中状态）
     */
    VideoCorrectionResultDto getResult(String taskId);

    /**
     * 获取历史记录列表。
     *
     * @return 历史记录列表，按创建时间倒序
     */
    List<HistoryItemDto> getHistoryList();
}


