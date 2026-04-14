package com.moke.assistant.controller;

import com.moke.assistant.common.R;
import com.moke.assistant.common.exception.ServiceException;
import com.moke.assistant.common.utils.UserContext;
import com.moke.assistant.dto.*;
import com.moke.assistant.entity.User;
import com.moke.assistant.entity.VideoAnalysisRecord;
import com.moke.assistant.repository.UserRepository;
import com.moke.assistant.repository.VideoAnalysisRecordRepository;
import com.moke.assistant.service.VideoAnalysisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频分析控制器
 */
@RestController
@RequestMapping("/api/video-analysis")
public class VideoAnalysisController {

    private final VideoAnalysisService videoAnalysisService;
    private final VideoAnalysisRecordRepository recordRepository;
    private final UserRepository userRepository;

    public VideoAnalysisController(VideoAnalysisService videoAnalysisService, 
                                   VideoAnalysisRecordRepository recordRepository,
                                   UserRepository userRepository) {
        this.videoAnalysisService = videoAnalysisService;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    /**
     * 从请求中获取userId（从Cookie或Header）
     */
    private Long getUserIdFromRequest(HttpServletRequest httpRequest) {
        // 优先从Header获取
        String userIdFromHeader = httpRequest.getHeader("X-User-Id");
        if (userIdFromHeader != null && !userIdFromHeader.trim().isEmpty()) {
            try {
                return Long.parseLong(userIdFromHeader.trim());
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        // 从Cookie获取
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("userId".equals(cookie.getName())) {
                    try {
                        return Long.parseLong(cookie.getValue());
                    } catch (NumberFormatException e) {
                        // 忽略解析错误
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 分析视频内容
     * 
     * @param request 包含视频URL的请求对象
     * @param httpRequest HTTP请求对象
     * @return 格式化后的分析结果
     */
    @PostMapping("/analyze")
    public R analyzeVideo(@RequestBody VideoAnalysisRequest request, HttpServletRequest httpRequest) {
        if (request.getVideoUrl() == null || request.getVideoUrl().trim().isEmpty()) {
            throw new ServiceException("视频URL不能为空");
        }
        
        // 确保UserContext中有用户信息（如果UserContext中没有，尝试从请求中获取）
        if (UserContext.getUser() == null) {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    UserContext.setUser(user);
                }
            }
        }
        
        VideoAnalysisResponse response = videoAnalysisService.analyzeVideo(request.getVideoUrl().trim());
        return R.ok().put("data", response);
    }

    /**
     * 查询视频分析历史记录（分页+条件查询）
     * 
     * @param videoUrl 视频URL（模糊查询，可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param createUserId 提交人ID（可选）
     * @param page 页码（从0开始，默认0）123456
     *
     * @param size 每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping("/history")
    public R getHistory(
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long createUserId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        // 解析时间参数
        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        
        if (startTime != null && !startTime.isEmpty()) {
            try {
                start = java.time.LocalDateTime.parse(startTime);
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        
        if (endTime != null && !endTime.isEmpty()) {
            try {
                end = java.time.LocalDateTime.parse(endTime);
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        
        // 处理videoUrl参数
        String videoUrlParam = (videoUrl != null && !videoUrl.trim().isEmpty()) ? videoUrl.trim() : null;
        
        // 查询数据
        Page<VideoAnalysisRecord> pageResult = recordRepository.findByConditions(
                videoUrlParam, start, end, createUserId, pageable);
        
        // 转换为DTO，并查询提交人信息
        List<VideoAnalysisHistoryItemDto> content = pageResult.getContent().stream()
                .map(record -> {
                    VideoAnalysisHistoryItemDto dto = new VideoAnalysisHistoryItemDto();
                    dto.setId(record.getId());
                    dto.setVideoUrl(record.getVideoUrl());
                    dto.setTitle(record.getTitle());
                    dto.setFormattedMessage(record.getFormattedMessage());
                    dto.setRawMessage(record.getRawMessage());
                    dto.setCreateTime(record.getCreateTime());
                    dto.setCreateUserId(record.getCreateUserId());
                    
                    // 查询提交人用户名
                    Long userId = record.getCreateUserId();
                    if (userId != null) {
                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null) {
                            dto.setCreateUsername(user.getUsername());
                        }
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        // 构建分页结果
        PageResultDto<VideoAnalysisHistoryItemDto> result = new PageResultDto<>();
        result.setContent(content);
        result.setTotalElements(pageResult.getTotalElements());
        result.setTotalPages(pageResult.getTotalPages());
        result.setNumber(pageResult.getNumber());
        result.setSize(pageResult.getSize());
        result.setHasPrevious(pageResult.hasPrevious());
        result.setHasNext(pageResult.hasNext());
        result.setFirst(pageResult.isFirst());
        result.setLast(pageResult.isLast());
        
        return R.ok().put("data", result);
    }
}

