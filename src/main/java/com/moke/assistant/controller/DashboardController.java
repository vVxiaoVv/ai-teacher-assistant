package com.moke.assistant.controller;

import com.moke.assistant.dto.DashboardStatsDto;
import com.moke.assistant.repository.LessonPlanRepository;
import com.moke.assistant.repository.StudentPortraitRepository;
import com.moke.assistant.repository.VideoExampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StudentPortraitRepository studentPortraitRepository;
    private final LessonPlanRepository lessonPlanRepository;
    private final VideoExampleRepository videoExampleRepository;

    @Autowired
    public DashboardController(StudentPortraitRepository studentPortraitRepository,
                               LessonPlanRepository lessonPlanRepository,
                               VideoExampleRepository videoExampleRepository) {
        this.studentPortraitRepository = studentPortraitRepository;
        this.lessonPlanRepository = lessonPlanRepository;
        this.videoExampleRepository = videoExampleRepository;
    }

    /**
     * 获取首页统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setStudentCount(studentPortraitRepository.count());
        stats.setLessonCount(lessonPlanRepository.count());
        stats.setVideoCount(videoExampleRepository.count());
        return ResponseEntity.ok(stats);
    }
}
