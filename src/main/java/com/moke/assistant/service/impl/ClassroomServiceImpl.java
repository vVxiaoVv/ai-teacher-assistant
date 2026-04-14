package com.moke.assistant.service.impl;

import com.moke.assistant.dto.ClassroomDto;
import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.dto.StudentPortraitDto;
import com.moke.assistant.entity.Classroom;
import com.moke.assistant.entity.ClassroomStudent;
import com.moke.assistant.entity.StudentPortrait;
import com.moke.assistant.repository.ClassroomRepository;
import com.moke.assistant.repository.ClassroomStudentRepository;
import com.moke.assistant.repository.StudentPortraitRepository;
import com.moke.assistant.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课堂服务实现类
 */
@Service
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final StudentPortraitRepository studentPortraitRepository;

    @Autowired
    public ClassroomServiceImpl(
            ClassroomRepository classroomRepository,
            ClassroomStudentRepository classroomStudentRepository,
            StudentPortraitRepository studentPortraitRepository) {
        this.classroomRepository = classroomRepository;
        this.classroomStudentRepository = classroomStudentRepository;
        this.studentPortraitRepository = studentPortraitRepository;
    }

    @Override
    @Transactional
    public Classroom createClassroom(String classroomName, String description, List<Long> studentIds) {
        Classroom classroom = new Classroom();
        classroom.setName(classroomName);
        classroom.setDescription(description);
        Classroom savedClassroom = classroomRepository.save(classroom);

        // 保存学生关联关系
        if (studentIds != null && !studentIds.isEmpty()) {
            for (Long studentId : studentIds) {
                StudentPortrait student = studentPortraitRepository.findById(studentId).orElse(null);
                if (student != null) {
                    ClassroomStudent classroomStudent = new ClassroomStudent();
                    classroomStudent.setClassroom(savedClassroom);
                    classroomStudent.setStudent(student);
                    classroomStudentRepository.save(classroomStudent);
                }
            }
        }

        return savedClassroom;
    }

    @Override
    @Transactional
    public Classroom updateClassroom(Long id, String classroomName, String description, List<Long> studentIds) {
        Classroom classroom = classroomRepository.findById(id).orElse(null);
        if (classroom == null) {
            return null;
        }

        classroom.setName(classroomName);
        classroom.setDescription(description);
        Classroom savedClassroom = classroomRepository.save(classroom);

        // 删除旧的关联关系
        classroomStudentRepository.deleteByClassroomId(id);

        // 保存新的关联关系
        if (studentIds != null && !studentIds.isEmpty()) {
            for (Long studentId : studentIds) {
                StudentPortrait student = studentPortraitRepository.findById(studentId).orElse(null);
                if (student != null) {
                    ClassroomStudent classroomStudent = new ClassroomStudent();
                    classroomStudent.setClassroom(savedClassroom);
                    classroomStudent.setStudent(student);
                    classroomStudentRepository.save(classroomStudent);
                }
            }
        }

        return savedClassroom;
    }

    @Override
    public ClassroomDto getClassroomById(Long id) {
        Classroom classroom = classroomRepository.findById(id).orElse(null);
        if (classroom == null) {
            return null;
        }

        ClassroomDto dto = new ClassroomDto();
        dto.setId(classroom.getId());
        dto.setClassroomName(classroom.getName());
        dto.setDescription(classroom.getDescription());
        dto.setCreateTime(classroom.getCreateTime());
        dto.setUpdateTime(classroom.getUpdateTime());

        // 加载学生信息
        List<ClassroomStudent> classroomStudents = classroomStudentRepository.findByClassroomId(id);
        List<StudentPortraitDto> students = new ArrayList<>();
        List<Long> studentIds = new ArrayList<>();

        for (ClassroomStudent cs : classroomStudents) {
            StudentPortrait student = cs.getStudent();
            if (student != null) {
                StudentPortraitDto studentDto = new StudentPortraitDto();
                studentDto.setId(student.getId());
                studentDto.setStudentName(student.getStudentName());
                studentDto.setPhotoUrl(student.getPhotoUrl());
                studentDto.setCharacteristics(student.getCharacteristics());
                students.add(studentDto);
                studentIds.add(student.getId());
            }
        }

        dto.setStudents(students);
        dto.setStudentIds(studentIds);
        dto.setStudentCount(students.size());

        return dto;
    }

    @Override
    @Transactional
    public void deleteClassroomById(Long id) {
        // 删除关联关系
        classroomStudentRepository.deleteByClassroomId(id);
        // 删除课堂
        classroomRepository.deleteById(id);
    }

    @Override
    public PageResultDto<ClassroomDto> getClassroomList(String classroomName, Pageable pageable) {
        Page<Classroom> page;
        if (classroomName != null && !classroomName.trim().isEmpty()) {
            page = classroomRepository.findByConditions(classroomName.trim(), pageable);
        } else {
            page = classroomRepository.findAll(pageable);
        }

        List<ClassroomDto> dtoList = page.getContent().stream().map(classroom -> {
            ClassroomDto dto = new ClassroomDto();
            dto.setId(classroom.getId());
            // 确保 name 不为 null
            String name = classroom.getName();
            if (name == null || name.trim().isEmpty()) {
                name = "未命名课堂";
            }
            dto.setClassroomName(name);
            dto.setDescription(classroom.getDescription());
            dto.setCreateTime(classroom.getCreateTime());
            dto.setUpdateTime(classroom.getUpdateTime());

            // 统计学生数量
            List<ClassroomStudent> classroomStudents = classroomStudentRepository.findByClassroomId(classroom.getId());
            dto.setStudentCount(classroomStudents.size());

            return dto;
        }).collect(Collectors.toList());

        PageResultDto<ClassroomDto> result = new PageResultDto<>();
        result.setContent(dtoList);
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setNumber(page.getNumber());
        result.setSize(page.getSize());
        result.setHasPrevious(page.hasPrevious());
        result.setHasNext(page.hasNext());
        result.setFirst(page.isFirst());
        result.setLast(page.isLast());

        return result;
    }
}


