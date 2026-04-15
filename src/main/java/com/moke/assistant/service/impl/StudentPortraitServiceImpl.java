package com.moke.assistant.service.impl;

import com.moke.assistant.common.utils.UserContext;
import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.dto.StudentPortraitDto;
import com.moke.assistant.entity.StudentPortrait;
import com.moke.assistant.repository.StudentPortraitRepository;
import com.moke.assistant.service.StudentPortraitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI学生画像服务实现类
 */
@Service
public class StudentPortraitServiceImpl implements StudentPortraitService {

    private final StudentPortraitRepository studentPortraitRepository;

    @Autowired
    public StudentPortraitServiceImpl(StudentPortraitRepository studentPortraitRepository) {
        this.studentPortraitRepository = studentPortraitRepository;
    }

    @Override
    public StudentPortrait createStudentPortrait(String studentName, String photoUrl, String characteristics, Integer age, String examHistory) {
        StudentPortrait studentPortrait = new StudentPortrait();
        studentPortrait.setStudentName(studentName);
        studentPortrait.setPhotoUrl(photoUrl);
        studentPortrait.setCharacteristics(characteristics);
        studentPortrait.setAge(age);
        studentPortrait.setExamHistory(examHistory);
        studentPortrait.setCreateUserId(UserContext.getUserId());
        studentPortrait.setUpdateUserId(UserContext.getUserId());
        return studentPortraitRepository.save(studentPortrait);
    }

    @Override
    public StudentPortrait updateStudentPortrait(Long id, String studentName, String photoUrl, String characteristics, Integer age, String examHistory) {
        Optional<StudentPortrait> optionalStudentPortrait = studentPortraitRepository.findById(id);
        if (optionalStudentPortrait.isPresent()) {
            StudentPortrait studentPortrait = optionalStudentPortrait.get();
            studentPortrait.setStudentName(studentName);
            studentPortrait.setPhotoUrl(photoUrl);
            studentPortrait.setCharacteristics(characteristics);
            studentPortrait.setAge(age);
            studentPortrait.setExamHistory(examHistory);
            studentPortrait.setUpdateUserId(UserContext.getUserId());
            return studentPortraitRepository.save(studentPortrait);
        }
        return null;
    }

    @Override
    public StudentPortrait getStudentPortraitById(Long id) {
        return studentPortraitRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteStudentPortraitById(Long id) {
        studentPortraitRepository.deleteById(id);
    }

    @Override
    public PageResultDto<StudentPortraitDto> getStudentPortraitList(String studentName, String startTime, String endTime, Pageable pageable) {
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

        // 处理学生姓名参数
        String studentNameParam = (studentName != null && !studentName.trim().isEmpty()) ? studentName.trim() : null;

        // 查询数据
        Page<StudentPortrait> pageResult = studentPortraitRepository.findByConditions(studentNameParam, start, end, pageable);

        // 转换为DTO
        List<StudentPortraitDto> content = pageResult.getContent().stream()
                .map(studentPortrait -> {
                    StudentPortraitDto dto = new StudentPortraitDto();
                    dto.setId(studentPortrait.getId());
                    dto.setStudentName(studentPortrait.getStudentName());
                    dto.setPhotoUrl(studentPortrait.getPhotoUrl());
                    dto.setCharacteristics(studentPortrait.getCharacteristics());
                    dto.setAge(studentPortrait.getAge());
                    dto.setExamHistory(studentPortrait.getExamHistory());
                    // 截取特点描述前100字符作为摘要
                    String characteristicsSummary = studentPortrait.getCharacteristics() != null && studentPortrait.getCharacteristics().length() > 100 ?
                            studentPortrait.getCharacteristics().substring(0, 100) + "..." : studentPortrait.getCharacteristics();
                    dto.setCharacteristicsSummary(characteristicsSummary);
                    dto.setCreateTime(studentPortrait.getCreateTime());
                    dto.setUpdateTime(studentPortrait.getUpdateTime());
                    return dto;
                })
                .collect(Collectors.toList());

        // 构建分页结果
        PageResultDto<StudentPortraitDto> result = new PageResultDto<>();
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