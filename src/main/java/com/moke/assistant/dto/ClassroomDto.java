package com.moke.assistant.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课堂DTO
 */
@Data
public class ClassroomDto {
    
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 课堂名称
     */
    private String classroomName;

    /**
     * 课堂描述
     */
    private String description;

    /**
     * 学生数量
     */
    private Integer studentCount;

    /**
     * 学生列表
     */
    private List<StudentPortraitDto> students;

    /**
     * 学生ID列表
     */
    private List<Long> studentIds;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}





