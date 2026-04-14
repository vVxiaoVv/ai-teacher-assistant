package com.moke.assistant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * AI学生画像实体类
 */
@Entity
@Table(name = "student_portrait")
@Data
public class StudentPortrait {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 学生姓名
     */
    @Column(name = "student_name", nullable = false, length = 100)
    private String studentName;

    /**
     * 学生照片URL
     */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /**
     * 学生特点描述
     */
    @Column(name = "characteristics", columnDefinition = "TEXT")
    private String characteristics;

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

    /**
     * 更新者ID
     */
    @Column(name = "update_user_id")
    private Long updateUserId;

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