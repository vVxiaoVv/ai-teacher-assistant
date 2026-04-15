package com.moke.assistant.dto;

import lombok.Data;

/**
 * 首页统计数据DTO
 */
@Data
public class DashboardStatsDto {
    
    /**
     * 学生画像数量
     */
    private Long studentCount;
    
    /**
     * 教案文件数量
     */
    private Long lessonCount;
    
    /**
     * 视频范例数量
     */
    private Long videoCount;
}
