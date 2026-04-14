package com.moke.assistant.controller;

import com.moke.assistant.common.exception.ServiceException;
import com.moke.assistant.entity.Script;
import com.moke.assistant.service.ScriptGenerationService;
import com.moke.assistant.utils.ScriptExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * 逐字稿控制器
 */
@RestController
@RequestMapping("/api/script")
public class ScriptController {

    private static final Logger logger = LoggerFactory.getLogger(ScriptController.class);

    private final ScriptGenerationService scriptGenerationService;
    private final ScriptExportUtil scriptExportUtil;

    @Autowired
    public ScriptController(ScriptGenerationService scriptGenerationService,
                           ScriptExportUtil scriptExportUtil) {
        this.scriptGenerationService = scriptGenerationService;
        this.scriptExportUtil = scriptExportUtil;
    }

    /**
     * 生成逐字稿
     *
     * @param lessonPlanId 教案ID
     * @return 生成的逐字稿
     */
    @PostMapping("/generate/{lessonPlanId}")
    public ResponseEntity<?> generateScript(@PathVariable("lessonPlanId") Long lessonPlanId) {
        try {
            logger.info("开始生成逐字稿，教案ID: {}", lessonPlanId);
            Script script = scriptGenerationService.generateScript(lessonPlanId);
            logger.info("逐字稿生成成功，ID: {}", script.getId());
            return ResponseEntity.ok(script);
        } catch (ServiceException e) {
            logger.error("生成逐字稿失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Broken pipe 异常会在全局异常处理器中处理，这里只处理其他异常
            logger.error("生成逐字稿失败", e);
            return ResponseEntity.internalServerError().body("生成逐字稿失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取逐字稿详情
     *
     * @param id 逐字稿ID
     * @return 逐字稿
     */
    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptById(@PathVariable("id") Long id) {
        try {
            Script script = scriptGenerationService.getScriptById(id);
            if (script == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(script);
        } catch (Exception e) {
            logger.error("获取逐字稿失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据教案ID获取逐字稿
     *
     * @param lessonPlanId 教案ID
     * @return 逐字稿
     */
    @GetMapping("/lesson-plan/{lessonPlanId}")
    public ResponseEntity<Script> getScriptByLessonPlanId(@PathVariable("lessonPlanId") Long lessonPlanId) {
        try {
            Script script = scriptGenerationService.getScriptByLessonPlanId(lessonPlanId);
            if (script == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(script);
        } catch (Exception e) {
            logger.error("获取逐字稿失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出逐字稿为TXT格式
     *
     * @param id 逐字稿ID
     * @return 文件下载
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<Resource> exportScript(@PathVariable("id") Long id) {
        try {
            Script script = scriptGenerationService.getScriptById(id);
            if (script == null) {
                return ResponseEntity.notFound().build();
            }

            String txtContent = scriptExportUtil.exportToTxt(script);
            byte[] contentBytes = txtContent.getBytes(StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(contentBytes);

            String filename = script.getTitle().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_") + ".txt";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(contentBytes.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("导出逐字稿失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出逐字稿为DOCX格式
     *
     * @param id 逐字稿ID
     * @return 文件下载
     */
    @GetMapping("/{id}/export/docx")
    public ResponseEntity<Resource> exportScriptAsDocx(@PathVariable("id") Long id) {
        try {
            Script script = scriptGenerationService.getScriptById(id);
            if (script == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] docxBytes = scriptExportUtil.exportToDocx(script);
            ByteArrayResource resource = new ByteArrayResource(docxBytes);

            String filename = script.getTitle().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_") + ".docx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(docxBytes.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("导出逐字稿失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

