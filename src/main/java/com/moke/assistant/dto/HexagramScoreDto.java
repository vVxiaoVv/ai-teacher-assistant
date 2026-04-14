package com.moke.assistant.dto;

import lombok.Data;

/**
 * 六芒星矩阵分数DTO
 */
@Data
public class HexagramScoreDto {
    
    /**
     * 教学基本功（0-100分）
     */
    private Double teachingFoundation;
    
    /**
     * 教学过程设计（0-100分）
     */
    private Double teachingProcessDesign;
    
    /**
     * 教态（0-100分）
     */
    private Double teachingManner;
    
    /**
     * 多媒体与板书运用（0-100分）
     */
    private Double multimediaAndBlackboard;
    
    /**
     * 课堂气氛（0-100分）
     */
    private Double classroomAtmosphere;
    
    /**
     * 时间节奏掌控（0-100分）
     */
    private Double timeRhythmControl;
}






