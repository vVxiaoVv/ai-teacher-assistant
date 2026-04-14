package com.moke.assistant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 逐字稿实体类
 */
@Entity
@Table(name = "script")
@Data
public class Script {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的教案ID
     */
    @Column(name = "lesson_plan_id", nullable = false)
    private Long lessonPlanId;

    /**
     * 逐字稿标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 逐字稿内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

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

    /**
     * 创建者ID
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

