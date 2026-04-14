package com.moke.assistant.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI学生画像数据传输对象
 */
@Data
public class StudentPortraitDto {

    /**
     * 学生画像ID
     */
    private Long id;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学生照片URL
     */
    private String photoUrl;

    /**
     * 学生特点描述摘要
     */
    private String characteristicsSummary;

    /**
     * 学生特点描述
     */
    private String characteristics;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}