package com.moke.assistant.service.impl;

import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.dto.VideoExampleDto;
import com.moke.assistant.entity.LessonPlan;
import com.moke.assistant.entity.VideoExample;
import com.moke.assistant.repository.LessonPlanRepository;
import com.moke.assistant.repository.VideoExampleRepository;
import com.moke.assistant.service.VideoExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频范例服务实现类
 */
@Service
public class VideoExampleServiceImpl implements VideoExampleService {

    private final VideoExampleRepository videoExampleRepository;
    private final LessonPlanRepository lessonPlanRepository;

    @Autowired
    public VideoExampleServiceImpl(VideoExampleRepository videoExampleRepository, LessonPlanRepository lessonPlanRepository) {
        this.videoExampleRepository = videoExampleRepository;
        this.lessonPlanRepository = lessonPlanRepository;
    }

    @Override
    public VideoExample saveVideoExample(Long lessonPlanId, String videoUrl, String topic, String description) {
        LessonPlan lessonPlan = lessonPlanRepository.findById(lessonPlanId)
                .orElseThrow(() -> new IllegalArgumentException("教案不存在：" + lessonPlanId));

        VideoExample videoExample = new VideoExample();
        videoExample.setLessonPlan(lessonPlan);
        videoExample.setVideoUrl(videoUrl);
        videoExample.setTopic(topic);
        videoExample.setDescription(description);
        return videoExampleRepository.save(videoExample);
    }

    @Override
    public VideoExample getVideoExampleById(Long id) {
        return videoExampleRepository.findById(id).orElse(null);
    }

    @Override
    public PageResultDto<VideoExampleDto> getVideoExampleList(String topic, Long lessonPlanId, String startTime, String endTime, Pageable pageable) {
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

        // 处理topic参数
        String topicParam = (topic != null && !topic.trim().isEmpty()) ? topic.trim() : null;

        // 查询数据
        Page<VideoExample> pageResult = videoExampleRepository.findByConditions(
                topicParam, start, end, lessonPlanId, pageable);

        // 转换为DTO
        List<VideoExampleDto> content = pageResult.getContent().stream()
                .map(videoExample -> {
                    VideoExampleDto dto = new VideoExampleDto();
                    dto.setId(videoExample.getId());
                    dto.setLessonPlanId(videoExample.getLessonPlan().getId());
                    dto.setLessonPlanTitle(videoExample.getLessonPlan().getTitle());
                    dto.setVideoUrl(videoExample.getVideoUrl());
                    dto.setTopic(videoExample.getTopic());
                    dto.setDescription(videoExample.getDescription());
                    dto.setCreateTime(videoExample.getCreateTime());
                    return dto;
                })
                .collect(Collectors.toList());

        // 构建分页结果
        PageResultDto<VideoExampleDto> result = new PageResultDto<>();
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