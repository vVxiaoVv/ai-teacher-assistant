package com.moke.assistant.controller;

import com.moke.assistant.dto.LessonPlanDto;
import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.entity.LessonPlan;
import com.moke.assistant.service.FileParseService;
import com.moke.assistant.service.LessonPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 教案控制器
 */
@RestController
@RequestMapping("/api/lesson-plan")
public class LessonPlanController {

    private static final Logger logger = LoggerFactory.getLogger(LessonPlanController.class);

    private final LessonPlanService lessonPlanService;
    private final FileParseService fileParseService;

    @Autowired
    public LessonPlanController(LessonPlanService lessonPlanService, FileParseService fileParseService) {
        this.lessonPlanService = lessonPlanService;
        this.fileParseService = fileParseService;
    }

    /**
     * 上传教案
     *
     * @param title       教案标题
     * @param content     教案文件
     * @param classroomId 关联课堂ID（可选）
     * @return 保存的教案实体
     */
    @PostMapping("/upload")
    public ResponseEntity<LessonPlan> uploadLessonPlan(
            @RequestParam("title") String title,
            @RequestParam("content") MultipartFile content,
            @RequestParam(value = "classroomId", required = false) Long classroomId) {

        logger.info("上传教案请求: title={}, classroomId={}", title, classroomId);

        // 验证参数
        if (title == null || title.trim().isEmpty()) {
            logger.warn("上传教案失败: 标题为空");
            return ResponseEntity.badRequest().body(null);
        }

        if (content == null || content.isEmpty()) {
            logger.warn("上传教案失败: 文件为空");
            return ResponseEntity.badRequest().body(null);
        }

        // 检查文件类型是否支持
        String filename = content.getOriginalFilename();
        if (!fileParseService.isSupported(filename)) {
            logger.warn("不支持的文件类型: {}", filename);
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // 解析文件内容为String
            String contentString = fileParseService.parseFileToString(content);

            if (contentString == null || contentString.trim().isEmpty()) {
                logger.warn("解析后的文件内容为空");
                return ResponseEntity.badRequest().body(null);
            }

            // 保存教案
            LessonPlan savedLessonPlan = lessonPlanService.uploadLessonPlan(
                    title.trim(),
                    contentString.trim(),
                    classroomId
            );

            logger.info("教案上传成功，ID: {}, 标题: {}", savedLessonPlan.getId(), savedLessonPlan.getTitle());
            return ResponseEntity.ok(savedLessonPlan);

        } catch (IOException e) {
            logger.error("解析文件失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        } catch (Exception e) {
            logger.error("上传教案失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 根据ID查询教案详情
     *
     * @param id 教案ID
     * @return 教案实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonPlan> getLessonPlanById(@PathVariable("id") Long id) {
        LessonPlan lessonPlan = lessonPlanService.getLessonPlanById(id);
        if (lessonPlan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lessonPlan);
    }

    /**
     * 查询教案列表（分页+条件查询）
     *
     * @param title     标题模糊查询（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param page      页码（从0开始，默认0）
     * @param size      每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<PageResultDto<LessonPlanDto>> getLessonPlanList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 确保页码和每页大小的有效性
        page = Math.max(0, page);
        size = Math.max(1, Math.min(50, size));

        Pageable pageable = PageRequest.of(page, size);
        PageResultDto<LessonPlanDto> result = lessonPlanService.getLessonPlanList(
                title, startTime, endTime, pageable);

        return ResponseEntity.ok(result);
    }
}
