package com.moke.assistant.service;

import com.moke.assistant.dto.LessonPlanDto;
import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.entity.LessonPlan;
import org.springframework.data.domain.Pageable;

public interface LessonPlanService {
    
    LessonPlan uploadLessonPlan(String title, String content, Long classroomId);
    
    LessonPlan getLessonPlanById(Long id);
    
    PageResultDto<LessonPlanDto> getLessonPlanList(String title, String startTime, String endTime, Pageable pageable);
}
