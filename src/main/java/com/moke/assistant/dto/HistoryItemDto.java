package com.moke.assistant.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 历史记录列表项 DTO
 */
@Data
public class HistoryItemDto {
    
    private String taskId;
    private String title;
    
    /**
     * 状态：PROCESSING / DONE / FAILED
     */
    private String status;
    
    private LocalDateTime createTime;
    
    /**
     * 简要概述，用于列表展示
     */
    private String summary;
}

