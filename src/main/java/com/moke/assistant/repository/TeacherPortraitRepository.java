package com.moke.assistant.repository;

import com.moke.assistant.entity.TeacherPortrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 教师画像Repository
 */
@Repository
public interface TeacherPortraitRepository extends JpaRepository<TeacherPortrait, Long> {

    /**
     * 根据用户ID查询最新的教师画像（按创建时间倒序）
     */
    Optional<TeacherPortrait> findFirstByUserIdOrderByCreateTimeDesc(Long userId);
}


