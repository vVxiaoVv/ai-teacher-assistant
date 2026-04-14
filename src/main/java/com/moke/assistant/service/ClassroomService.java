package com.moke.assistant.service;

import com.moke.assistant.dto.ClassroomDto;
import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.entity.Classroom;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 课堂服务接口
 */
public interface ClassroomService {

    /**
     * 创建课堂
     *
     * @param classroomName 课堂名称
     * @param description 课堂描述
     * @param studentIds 学生ID列表
     * @return 保存的课堂实体
     */
    Classroom createClassroom(String classroomName, String description, List<Long> studentIds);

    /**
     * 更新课堂
     *
     * @param id 课堂ID
     * @param classroomName 课堂名称
     * @param description 课堂描述
     * @param studentIds 学生ID列表
     * @return 更新后的课堂实体
     */
    Classroom updateClassroom(Long id, String classroomName, String description, List<Long> studentIds);

    /**
     * 根据ID查询课堂
     *
     * @param id 课堂ID
     * @return 课堂DTO（包含学生信息）
     */
    ClassroomDto getClassroomById(Long id);

    /**
     * 根据ID删除课堂
     *
     * @param id 课堂ID
     */
    void deleteClassroomById(Long id);

    /**
     * 分页查询课堂列表（支持条件查询）
     *
     * @param classroomName 课堂名称模糊匹配（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    PageResultDto<ClassroomDto> getClassroomList(String classroomName, Pageable pageable);
}





