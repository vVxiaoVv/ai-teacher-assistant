package com.moke.assistant.repository;

import com.moke.assistant.entity.VideoCorrectionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 视频纠偏任务Repository
 */
@Repository
public interface VideoCorrectionTaskRepository extends JpaRepository<VideoCorrectionTask, String> {

    /**
     * 根据状态查询任务列表
     */
    List<VideoCorrectionTask> findByStatusOrderByCreateTimeDesc(String status);

    /**
     * 查询所有任务，按创建时间倒序
     */
    List<VideoCorrectionTask> findAllByOrderByCreateTimeDesc();

    /**
     * 根据用户ID查询任务列表
     */
    @Query("SELECT t FROM VideoCorrectionTask t WHERE t.createUserId = :userId ORDER BY t.createTime DESC")
    List<VideoCorrectionTask> findByCreateUserIdOrderByCreateTimeDesc(Long userId);
}

