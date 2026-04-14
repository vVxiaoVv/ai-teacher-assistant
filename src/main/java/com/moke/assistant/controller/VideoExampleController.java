package com.moke.assistant.controller;

import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.dto.VideoExampleDto;
import com.moke.assistant.entity.VideoExample;
import com.moke.assistant.service.VideoExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 视频范例控制器
 */
@RestController
@RequestMapping("/api/video-example")
public class VideoExampleController {

    private final VideoExampleService videoExampleService;

    @Autowired
    public VideoExampleController(VideoExampleService videoExampleService) {
        this.videoExampleService = videoExampleService;
    }

    /**
     * 保存视频范例
     *
     * @param lessonPlanId 关联的教案ID
     * @param videoUrl     视频URL
     * @param topic        视频主题
     * @param description  视频描述（可选）
     * @return 保存的视频范例实体
     */
    @PostMapping("/save")
    public ResponseEntity<VideoExample> saveVideoExample(
            @RequestParam("lessonPlanId") Long lessonPlanId,
            @RequestParam("videoUrl") String videoUrl,
            @RequestParam("topic") String topic,
            @RequestParam(required = false) String description) {

        if (lessonPlanId == null) {
            return ResponseEntity.badRequest().body(null);
        }

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        if (topic == null || topic.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        VideoExample savedVideoExample = videoExampleService.saveVideoExample(
                lessonPlanId, videoUrl.trim(), topic.trim(), description);
        return ResponseEntity.ok(savedVideoExample);
    }

    /**
     * 根据ID查询视频范例详情
     *
     * @param id 视频范例ID
     * @return 视频范例实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<VideoExample> getVideoExampleById(@PathVariable("id") Long id) {
        VideoExample videoExample = videoExampleService.getVideoExampleById(id);
        if (videoExample == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(videoExample);
    }

    /**
     * 查询视频范例列表（分页+条件查询）
     *
     * @param topic        主题模糊查询（可选）
     * @param lessonPlanId 关联的教案ID（可选）
     * @param startTime    开始时间（可选）
     * @param endTime      结束时间（可选）
     * @param page         页码（从0开始，默认0）
     * @param size         每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<PageResultDto<VideoExampleDto>> getVideoExampleList(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Long lessonPlanId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 确保页码和每页大小的有效性
        page = Math.max(0, page);
        size = Math.max(1, Math.min(50, size));

        Pageable pageable = PageRequest.of(page, size);
        PageResultDto<VideoExampleDto> result = videoExampleService.getVideoExampleList(
                topic, lessonPlanId, startTime, endTime, pageable);

        return ResponseEntity.ok(result);
    }
}