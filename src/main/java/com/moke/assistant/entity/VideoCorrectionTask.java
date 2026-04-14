package com.moke.assistant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 视频纠偏任务实体类
 */
@Entity
@Table(name = "video_correction_task")
@Data
public class VideoCorrectionTask {

    /**
     * 任务ID（主键）
     */
    @Id
    @Column(name = "id", length = 50)
    private String id;

    /**
     * 任务标题
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * 视频URL
     */
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /**
     * 文件名
     */
    @Column(name = "file_name", length = 255)
    private String fileName;

    /**
     * 任务状态（如：PENDING、PROCESSING、DONE、FAILED）
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /**
     * 纠偏总结
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * 结构建议
     */
    @Column(name = "structure_advice", columnDefinition = "TEXT")
    private String structureAdvice;

    /**
     * 互动建议
     */
    @Column(name = "interaction_advice", columnDefinition = "TEXT")
    private String interactionAdvice;

    /**
     * 语言建议
     */
    @Column(name = "language_advice", columnDefinition = "TEXT")
    private String languageAdvice;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 创建者ID（操作人ID）
     */
    @Column(name = "create_user_id")
    private Long createUserId;

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

