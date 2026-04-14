package com.moke.assistant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 教案实体类
 */
@Entity
@Table(name = "lesson_plan")
@Data
public class LessonPlan {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 教案标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 教案内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 关联课堂ID
     */
    @Column(name = "classroom_id")
    private Long classroomId;

    /**
     * 上传时间
     */
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

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
        uploadTime = LocalDateTime.now();
    }
}