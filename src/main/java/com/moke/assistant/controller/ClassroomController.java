package com.moke.assistant.controller;

import com.moke.assistant.dto.ClassroomDto;
import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.entity.Classroom;
import com.moke.assistant.service.ClassroomService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课堂控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/classroom")
public class ClassroomController {

    private final ClassroomService classroomService;

    @Autowired
    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    /**
     * 创建课堂
     *
     * @param requestBody 请求体（JSON格式）
     * @return 保存的课堂实体
     */
    @PostMapping("/create")
    public ResponseEntity<Classroom> createClassroom(@RequestBody CreateClassroomRequest requestBody) {
        log.info("创建课堂请求: classroomName={}, description={}, studentIds={}", 
                requestBody != null ? requestBody.getClassroomName() : "null",
                requestBody != null ? requestBody.getDescription() : "null",
                requestBody != null ? requestBody.getStudentIds() : "null");
        
        if (requestBody == null || requestBody.getClassroomName() == null || requestBody.getClassroomName().trim().isEmpty()) {
            log.warn("创建课堂失败: 课堂名称为空");
            return ResponseEntity.badRequest().body(null);
        }

        Classroom savedClassroom = classroomService.createClassroom(
                requestBody.getClassroomName().trim(),
                requestBody.getDescription(),
                requestBody.getStudentIds()
        );

        log.info("创建课堂成功: id={}, name={}", savedClassroom.getId(), savedClassroom.getName());
        return ResponseEntity.ok(savedClassroom);
    }

    /**
     * 创建课堂请求DTO
     */
    @Data
    public static class CreateClassroomRequest {
        private String classroomName;
        private String description;
        private List<Long> studentIds;
    }

    /**
     * 更新课堂
     *
     * @param id 课堂ID
     * @param requestBody 请求体（JSON格式）
     * @return 更新后的课堂实体
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Classroom> updateClassroom(
            @PathVariable("id") Long id,
            @RequestBody UpdateClassroomRequest requestBody) {

        if (id == null || requestBody == null || requestBody.getClassroomName() == null || requestBody.getClassroomName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Classroom updatedClassroom = classroomService.updateClassroom(
                id,
                requestBody.getClassroomName().trim(),
                requestBody.getDescription(),
                requestBody.getStudentIds()
        );

        if (updatedClassroom == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedClassroom);
    }

    /**
     * 更新课堂请求DTO
     */
    @Data
    public static class UpdateClassroomRequest {
        private String classroomName;
        private String description;
        private List<Long> studentIds;
    }

    /**
     * 根据ID查询课堂详情
     *
     * @param id 课堂ID
     * @return 课堂DTO（包含学生信息）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDto> getClassroomById(@PathVariable("id") Long id) {
        ClassroomDto classroomDto = classroomService.getClassroomById(id);
        if (classroomDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(classroomDto);
    }

    /**
     * 根据ID删除课堂
     *
     * @param id 课堂ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClassroomById(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body("ID不能为空");
        }

        classroomService.deleteClassroomById(id);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 查询课堂列表（分页+条件查询）
     *
     * @param classroomName 课堂名称模糊查询（可选）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<PageResultDto<ClassroomDto>> getClassroomList(
            @RequestParam(required = false) String classroomName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 确保页码和每页大小的有效性
        page = Math.max(0, page);
        size = Math.max(1, Math.min(50, size));

        Pageable pageable = PageRequest.of(page, size);
        PageResultDto<ClassroomDto> result = classroomService.getClassroomList(classroomName, pageable);

        return ResponseEntity.ok(result);
    }
}

