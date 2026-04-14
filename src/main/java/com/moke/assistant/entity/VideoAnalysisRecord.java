package com.moke.assistant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 视频分析记录实体类
 */
@Entity
@Table(name = "video_analysis_record")
@Data
public class VideoAnalysisRecord {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 视频URL
     */
    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    /**
     * 标题（课题+时间）
     */
    @Column(name = "title", length = 500)
    private String title;

    /**
     * 格式化后的分析结果
     */
    @Column(name = "formatted_message", columnDefinition = "TEXT")
    private String formattedMessage;

    /**
     * 原始分析结果
     */
    @Column(name = "raw_message", columnDefinition = "TEXT")
    private String rawMessage;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 创建者ID（操作人ID）
     */
    @Column(name = "create_user_id")
    private Long createUserId;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

