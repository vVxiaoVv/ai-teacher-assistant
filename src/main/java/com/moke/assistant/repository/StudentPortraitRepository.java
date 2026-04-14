package com.moke.assistant.repository;

import com.moke.assistant.entity.StudentPortrait;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * AI学生画像Repository
 */
@Repository
public interface StudentPortraitRepository extends JpaRepository<StudentPortrait, Long> {

    /**
     * 根据学生姓名模糊查询
     */
    Page<StudentPortrait> findByStudentNameContaining(String studentName, Pageable pageable);

    /**
     * 综合条件查询：学生姓名模糊匹配 + 时间范围
     */
    @Query("SELECT sp FROM StudentPortrait sp WHERE " +
           "(:studentName IS NULL OR sp.studentName LIKE %:studentName%) AND " +
           "(:startTime IS NULL OR sp.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR sp.createTime <= :endTime) " +
           "ORDER BY sp.createTime DESC")
    Page<StudentPortrait> findByConditions(
            @Param("studentName") String studentName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );
}