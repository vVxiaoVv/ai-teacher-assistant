package com.moke.assistant.repository;

import com.moke.assistant.entity.VideoExample;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 视频范例Repository
 */
@Repository
public interface VideoExampleRepository extends JpaRepository<VideoExample, Long> {

    /**
     * 根据主题模糊查询
     */
    Page<VideoExample> findByTopicContaining(String topic, Pageable pageable);

    /**
     * 根据创建时间范围查询
     */
    Page<VideoExample> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据教案ID查询
     */
    Page<VideoExample> findByLessonPlanId(Long lessonPlanId, Pageable pageable);

    /**
     * 综合条件查询：主题模糊匹配 + 时间范围 + 教案ID
     */
    @Query("SELECT ve FROM VideoExample ve WHERE " +
           "(:topic IS NULL OR ve.topic LIKE %:topic%) AND " +
           "(:startTime IS NULL OR ve.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR ve.createTime <= :endTime) AND " +
           "(:lessonPlanId IS NULL OR ve.lessonPlan.id = :lessonPlanId) " +
           "ORDER BY ve.createTime DESC")
    Page<VideoExample> findByConditions(
            @Param("topic") String topic,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("lessonPlanId") Long lessonPlanId,
            Pageable pageable
    );
}