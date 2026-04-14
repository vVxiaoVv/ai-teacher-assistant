package com.moke.assistant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 视频范例实体类
 */
@Entity
@Table(name = "video_example")
@Data
public class VideoExample {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的教案ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_plan_id", nullable = false)
    private LessonPlan lessonPlan;

    /**
     * 视频URL
     */
    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    /**
     * 视频主题
     */
    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    /**
     * 视频描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 创建者ID
     */
    @Column(name = "create_user_id")
    private Long createUserId;

    /**
     * 更新者ID
     */
    @Column(name = "update_user_id")
    private Long updateUserId;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}