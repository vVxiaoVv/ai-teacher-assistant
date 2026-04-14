package com.moke.assistant.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

/**
 * 课堂学生关联实体类（多对多关系）
 */
@Entity
@Table(name = "classroom_student", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"classroom_id", "student_id"})
})
@Data
public class ClassroomStudent {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 课堂ID
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    /**
     * 学生ID
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentPortrait student;
}





