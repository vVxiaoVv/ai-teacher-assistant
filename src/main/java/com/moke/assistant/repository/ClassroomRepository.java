package com.moke.assistant.repository;

import com.moke.assistant.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 课堂Repository
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    /**
     * 根据课堂名称模糊查询
     */
    Page<Classroom> findByNameContaining(String name, Pageable pageable);

    /**
     * 综合条件查询：课堂名称模糊匹配
     */
    @Query("SELECT c FROM Classroom c WHERE " +
           "(:classroomName IS NULL OR c.name LIKE %:classroomName%) " +
           "ORDER BY c.createTime DESC")
    Page<Classroom> findByConditions(
            @Param("classroomName") String classroomName,
            Pageable pageable
    );
}





