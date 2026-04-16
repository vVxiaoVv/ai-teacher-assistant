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

    @PostMapping("/upload")
    public ResponseEntity<?> uploadLessonPlan(
            @RequestParam("title") String title,
            @RequestParam("content") MultipartFile content,
            @RequestParam(value = "classroomId", required = false) Long classroomId) {
        
        try {
            if (content == null || content.isEmpty()) {
                return ResponseEntity.badRequest().body("教案文件不能为空");
            }
            
            String originalFilename = content.getOriginalFilename();
            if (!fileParseService.isSupported(originalFilename)) {
                logger.warn("不支持的文件类型: {}", originalFilename);
                return ResponseEntity.badRequest().body("不支持的文件类型，仅支持 .txt, .doc, .docx, .pdf");
            }
            
            String contentString = fileParseService.parseFileToString(content);
            
            if (contentString == null || contentString.trim().isEmpty()) {
                logger.warn("解析后的文件内容为空");
                return ResponseEntity.badRequest().body("文件内容为空，无法解析");
            }
            
            LessonPlan savedLessonPlan = lessonPlanService.uploadLessonPlan(
                    title.trim(), 
                    contentString.trim(), 
                    classroomId
            );
            logger.info("教案上传成功，ID: {}, 标题: {}", savedLessonPlan.getId(), savedLessonPlan.getTitle());
            return ResponseEntity.ok(savedLessonPlan);
            
        } catch (Exception e) {
            logger.error("上传教案失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonPlan> getLessonPlanById(@PathVariable("id") Long id) {
        LessonPlan lessonPlan = lessonPlanService.getLessonPlanById(id);
        if (lessonPlan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lessonPlan);
    }

    @GetMapping("/list")
    public ResponseEntity<PageResultDto<LessonPlanDto>> getLessonPlanList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        page = Math.max(0, page);
        size = Math.max(1, Math.min(50, size));
        
        Pageable pageable = PageRequest.of(page, size);
        PageResultDto<LessonPlanDto> result = lessonPlanService.getLessonPlanList(
                title, startTime, endTime, pageable);
        
        return ResponseEntity.ok(result);
    }
}
