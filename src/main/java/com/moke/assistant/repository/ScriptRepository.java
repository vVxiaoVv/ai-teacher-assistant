package com.moke.assistant.repository;

import com.moke.assistant.entity.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 逐字稿Repository
 */
@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {

    /**
     * 根据教案ID查询逐字稿
     *
     * @param lessonPlanId 教案ID
     * @return 逐字稿
     */
    Optional<Script> findByLessonPlanId(Long lessonPlanId);
}

