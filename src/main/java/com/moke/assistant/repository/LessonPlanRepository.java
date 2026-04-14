package com.moke.assistant.repository;

import com.moke.assistant.entity.LessonPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 教案Repository
 */
@Repository
public interface LessonPlanRepository extends JpaRepository<LessonPlan, Long> {

    /**
     * 根据标题模糊查询
     */
    Page<LessonPlan> findByTitleContaining(String title, Pageable pageable);

    /**
     * 根据上传时间范围查询
     */
    Page<LessonPlan> findByUploadTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 综合条件查询：标题模糊匹配 + 时间范围
     */
    @Query("SELECT lp FROM LessonPlan lp WHERE " +
           "(:title IS NULL OR lp.title LIKE %:title%) AND " +
           "(:startTime IS NULL OR lp.uploadTime >= :startTime) AND " +
           "(:endTime IS NULL OR lp.uploadTime <= :endTime) " +
           "ORDER BY lp.uploadTime DESC")
    Page<LessonPlan> findByConditions(
            @Param("title") String title,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );
}