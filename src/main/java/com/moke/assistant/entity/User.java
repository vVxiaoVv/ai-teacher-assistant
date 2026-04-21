package com.moke.assistant.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Entity
@Table(name = "user")
public class User {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名（唯一）
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（加密存储）
     */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /**
     * 用户角色（如：admin、teacher等）
     */
    @Column(name = "role", nullable = false, length = 20)
    private String role = "admin";

    /**
     * 用户状态（如：active、inactive等）
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "active";

    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 年龄
     */
    @Column(name = "age")
    private Integer age;

    /**
     * 学科（如：语文、数学、英语等）
     */
    @Column(name = "subject", length = 50)
    private String subject;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
