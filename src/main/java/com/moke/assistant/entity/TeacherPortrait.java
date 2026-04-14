package com.moke.assistant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 教师画像实体类
 */
@Entity
@Table(name = "teacher_portrait")
@Data
public class TeacherPortrait {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 画像描述（格式化后的AI响应）
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 打分规则说明
     */
    @Column(name = "scoring_rule", columnDefinition = "TEXT")
    private String scoringRule;

    /**
     * AI原始响应
     */
    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;

    /**
     * 历史记录数量
     */
    @Column(name = "history_count")
    private Integer historyCount;

    /**
     * 教学基本功分数
     */
    @Column(name = "teaching_foundation")
    private Double teachingFoundation;

    /**
     * 教学过程设计分数
     */
    @Column(name = "teaching_process_design")
    private Double teachingProcessDesign;

    /**
     * 教态分数
     */
    @Column(name = "teaching_manner")
    private Double teachingManner;

    /**
     * 多媒体与板书运用分数
     */
    @Column(name = "multimedia_and_blackboard")
    private Double multimediaAndBlackboard;

    /**
     * 课堂气氛分数
     */
    @Column(name = "classroom_atmosphere")
    private Double classroomAtmosphere;

    /**
     * 时间节奏掌控分数
     */
    @Column(name = "time_rhythm_control")
    private Double timeRhythmControl;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}


