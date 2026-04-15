package com.moke.assistant.controller;

import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.dto.StudentPortraitDto;
import com.moke.assistant.entity.StudentPortrait;
import com.moke.assistant.service.StudentPortraitService;
import com.moke.assistant.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * AI学生画像控制器
 */
@RestController
@RequestMapping("/api/student-portrait")
public class StudentPortraitController {

    private final StudentPortraitService studentPortraitService;
    private final FileUploadUtil fileUploadUtil;

    @Autowired
    public StudentPortraitController(StudentPortraitService studentPortraitService, FileUploadUtil fileUploadUtil) {
        this.studentPortraitService = studentPortraitService;
        this.fileUploadUtil = fileUploadUtil;
    }

    /**
     * 上传学生照片
     *
     * @param file 上传的照片文件
     * @return 照片的访问URL
     */
    @PostMapping("/upload-photo")
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件不能为空");
        }

        try {
            String photoUrl = fileUploadUtil.uploadImage(file);
            return ResponseEntity.ok(photoUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("文件上传失败");
        }
    }

    /**
     * 创建学生画像
     *
     * @param studentName 学生姓名
     * @param photoUrl 学生照片URL
     * @param characteristics 学生特点描述
     * @param age 学生年龄
     * @param examHistory 历史考试信息（JSON格式）
     * @return 保存的学生画像实体
     */
    @PostMapping("/create")
    public ResponseEntity<StudentPortrait> createStudentPortrait(
            @RequestParam("studentName") String studentName,
            @RequestParam(required = false) String photoUrl,
            @RequestParam(required = false) String characteristics,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String examHistory) {

        if (studentName == null || studentName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        StudentPortrait savedStudentPortrait = studentPortraitService.createStudentPortrait(
                studentName.trim(),
                photoUrl,
                characteristics,
                age,
                examHistory
        );

        return ResponseEntity.ok(savedStudentPortrait);
    }

    /**
     * 更新学生画像
     *
     * @param id 学生画像ID
     * @param studentName 学生姓名
     * @param photoUrl 学生照片URL
     * @param characteristics 学生特点描述
     * @param age 学生年龄
     * @param examHistory 历史考试信息（JSON格式）
     * @return 更新后的学生画像实体
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<StudentPortrait> updateStudentPortrait(
            @PathVariable("id") Long id,
            @RequestParam("studentName") String studentName,
            @RequestParam(required = false) String photoUrl,
            @RequestParam(required = false) String characteristics,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String examHistory) {

        if (id == null || studentName == null || studentName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        StudentPortrait updatedStudentPortrait = studentPortraitService.updateStudentPortrait(
                id,
                studentName.trim(),
                photoUrl,
                characteristics,
                age,
                examHistory
        );

        if (updatedStudentPortrait == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedStudentPortrait);
    }

    /**
     * 根据ID查询学生画像详情
     *
     * @param id 学生画像ID
     * @return 学生画像实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentPortrait> getStudentPortraitById(@PathVariable("id") Long id) {
        StudentPortrait studentPortrait = studentPortraitService.getStudentPortraitById(id);
        if (studentPortrait == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(studentPortrait);
    }

    /**
     * 根据ID删除学生画像
     *
     * @param id 学生画像ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudentPortraitById(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body("ID不能为空");
        }

        StudentPortrait studentPortrait = studentPortraitService.getStudentPortraitById(id);
        if (studentPortrait != null) {
            // 删除学生画像
            studentPortraitService.deleteStudentPortraitById(id);
            // 删除关联的照片文件
            if (studentPortrait.getPhotoUrl() != null) {
                fileUploadUtil.deleteFile(studentPortrait.getPhotoUrl());
            }
        }

        return ResponseEntity.ok("删除成功");
    }

    /**
     * 查询学生画像列表（分页+条件查询）
     *
     * @param studentName 学生姓名模糊查询（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<PageResultDto<StudentPortraitDto>> getStudentPortraitList(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 确保页码和每页大小的有效性
        page = Math.max(0, page);
        size = Math.max(1, Math.min(50, size));

        Pageable pageable = PageRequest.of(page, size);
        PageResultDto<StudentPortraitDto> result = studentPortraitService.getStudentPortraitList(
                studentName, startTime, endTime, pageable);

        return ResponseEntity.ok(result);
    }
}