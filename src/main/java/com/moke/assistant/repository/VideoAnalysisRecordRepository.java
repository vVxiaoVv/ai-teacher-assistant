package com.moke.assistant.repository;

import com.moke.assistant.entity.VideoAnalysisRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频分析记录Repository
 */
@Repository
public interface VideoAnalysisRecordRepository extends JpaRepository<VideoAnalysisRecord, Long> {

    /**
     * 根据视频URL模糊查询
     */
    Page<VideoAnalysisRecord> findByVideoUrlContaining(String videoUrl, Pageable pageable);

    /**
     * 根据创建时间范围查询
     */
    Page<VideoAnalysisRecord> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 综合条件查询：视频URL模糊匹配 + 时间范围 + 提交人
     */
    @Query("SELECT v FROM VideoAnalysisRecord v WHERE " +
           "(:videoUrl IS NULL OR v.videoUrl LIKE %:videoUrl%) AND " +
           "(:startTime IS NULL OR v.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR v.createTime <= :endTime) AND " +
           "(:createUserId IS NULL OR v.createUserId = :createUserId) " +
           "ORDER BY v.createTime DESC")
    Page<VideoAnalysisRecord> findByConditions(
            @Param("videoUrl") String videoUrl,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("createUserId") Long createUserId,
            Pageable pageable
    );
    
    /**
     * 根据用户ID查询所有历史记录（按时间升序，用于计算权重）
     */
    @Query("SELECT v FROM VideoAnalysisRecord v WHERE v.createUserId = :userId ORDER BY v.createTime ASC")
    List<VideoAnalysisRecord> findByCreateUserIdOrderByCreateTimeAsc(@Param("userId") Long userId);
}

