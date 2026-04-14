package com.moke.assistant.repository;

import com.moke.assistant.entity.ClassroomStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课堂学生关联Repository
 */
@Repository
public interface ClassroomStudentRepository extends JpaRepository<ClassroomStudent, Long> {

    /**
     * 根据课堂ID查询所有关联的学生
     */
    @Query("SELECT cs FROM ClassroomStudent cs WHERE cs.classroom.id = :classroomId")
    List<ClassroomStudent> findByClassroomId(@Param("classroomId") Long classroomId);

    /**
     * 根据课堂ID删除所有关联关系
     */
    @Modifying
    @Query("DELETE FROM ClassroomStudent cs WHERE cs.classroom.id = :classroomId")
    void deleteByClassroomId(@Param("classroomId") Long classroomId);

    /**
     * 根据学生ID查询所有关联的课堂
     */
    @Query("SELECT cs FROM ClassroomStudent cs WHERE cs.student.id = :studentId")
    List<ClassroomStudent> findByStudentId(@Param("studentId") Long studentId);
}





