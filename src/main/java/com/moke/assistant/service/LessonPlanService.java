package com.moke.assistant.service;

import com.moke.assistant.dto.LessonPlanDto;
import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.entity.LessonPlan;
import org.springframework.data.domain.Pageable;

/**
 * 教案服务接口
 */
public interface LessonPlanService {

    /**
     * 上传教案
     *
     * @param title       教案标题
     * @param content     教案内容
     * @param classroomId 关联课堂ID（可选）
     * @return 保存的教案实体
     */
    LessonPlan uploadLessonPlan(String title, String content, Long classroomId);

    /**
     * 根据ID查询教案
     *
     * @param id 教案ID
     * @return 教案实体
     */
    LessonPlan getLessonPlanById(Long id);

    /**
     * 分页查询教案列表（支持条件查询）
     *
     * @param title     标题模糊匹配（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param pageable  分页参数
     * @return 分页结果
     */
    PageResultDto<LessonPlanDto> getLessonPlanList(String title, String startTime, String endTime, Pageable pageable);
}
