package com.moke.assistant.service;

import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.dto.StudentPortraitDto;
import com.moke.assistant.entity.StudentPortrait;
import org.springframework.data.domain.Pageable;

/**
 * AI学生画像服务接口
 */
public interface StudentPortraitService {

    /**
     * 创建学生画像
     *
     * @param studentName 学生姓名
     * @param photoUrl 学生照片URL
     * @param characteristics 学生特点描述
     * @return 保存的学生画像实体
     */
    StudentPortrait createStudentPortrait(String studentName, String photoUrl, String characteristics);

    /**
     * 更新学生画像
     *
     * @param id 学生画像ID
     * @param studentName 学生姓名
     * @param photoUrl 学生照片URL
     * @param characteristics 学生特点描述
     * @return 更新后的学生画像实体
     */
    StudentPortrait updateStudentPortrait(Long id, String studentName, String photoUrl, String characteristics);

    /**
     * 根据ID查询学生画像
     *
     * @param id 学生画像ID
     * @return 学生画像实体
     */
    StudentPortrait getStudentPortraitById(Long id);

    /**
     * 根据ID删除学生画像
     *
     * @param id 学生画像ID
     */
    void deleteStudentPortraitById(Long id);

    /**
     * 分页查询学生画像列表（支持条件查询）
     *
     * @param studentName 学生姓名模糊匹配（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    PageResultDto<StudentPortraitDto> getStudentPortraitList(String studentName, String startTime, String endTime, Pageable pageable);
}