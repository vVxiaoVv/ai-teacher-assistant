package com.moke.assistant.service.impl;

import com.moke.assistant.dto.LessonPlanDto;
import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.entity.LessonPlan;
import com.moke.assistant.repository.LessonPlanRepository;
import com.moke.assistant.service.LessonPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教案服务实现类
 */
@Service
public class LessonPlanServiceImpl implements LessonPlanService {

    private final LessonPlanRepository lessonPlanRepository;


    @Autowired
    public LessonPlanServiceImpl(LessonPlanRepository lessonPlanRepository) {
        this.lessonPlanRepository = lessonPlanRepository;
    }

    @Override
    public LessonPlan uploadLessonPlan(String title, String content) {
        LessonPlan lessonPlan = new LessonPlan();
        lessonPlan.setTitle(title);
        lessonPlan.setContent(content);
        return lessonPlanRepository.save(lessonPlan);
    }

    @Override
    public LessonPlan getLessonPlanById(Long id) {
        return lessonPlanRepository.findById(id).orElse(null);
    }

    @Override
    public PageResultDto<LessonPlanDto> getLessonPlanList(String title, String startTime, String endTime, Pageable pageable) {
        // 解析时间参数
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startTime != null && !startTime.trim().isEmpty()) {
            try {
                start = LocalDateTime.parse(startTime.trim());
            } catch (Exception e) {
                // 忽略解析错误，使用null代替
            }
        }

        if (endTime != null && !endTime.trim().isEmpty()) {
            try {
                end = LocalDateTime.parse(endTime.trim());
            } catch (Exception e) {
                // 忽略解析错误，使用null代替
            }
        }

        // 处理title参数
        String titleParam = (title != null && !title.trim().isEmpty()) ? title.trim() : null;

        // 查询数据
        Page<LessonPlan> pageResult = lessonPlanRepository.findByConditions(titleParam, start, end, pageable);

        // 转换为DTO
        List<LessonPlanDto> content = pageResult.getContent().stream()
                .map(lessonPlan -> {
                    LessonPlanDto dto = new LessonPlanDto();
                    dto.setId(lessonPlan.getId());
                    dto.setTitle(lessonPlan.getTitle());
                    // 截取内容前100字符作为摘要
                    String contentSummary = lessonPlan.getContent().length() > 100 ? 
                            lessonPlan.getContent().substring(0, 100) + "..." : lessonPlan.getContent();
                    dto.setContentSummary(contentSummary);
                    dto.setUploadTime(lessonPlan.getUploadTime());
                    return dto;
                })
                .collect(Collectors.toList());

        // 构建分页结果
        PageResultDto<LessonPlanDto> result = new PageResultDto<>();
        result.setContent(content);
        result.setTotalElements(pageResult.getTotalElements());
        result.setTotalPages(pageResult.getTotalPages());
        result.setNumber(pageResult.getNumber());
        result.setSize(pageResult.getSize());
        result.setHasPrevious(pageResult.hasPrevious());
        result.setHasNext(pageResult.hasNext());
        result.setFirst(pageResult.isFirst());
        result.setLast(pageResult.isLast());

        return result;
    }
}