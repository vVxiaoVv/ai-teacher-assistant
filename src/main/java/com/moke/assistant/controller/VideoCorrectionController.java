package com.moke.assistant.controller;

import com.moke.assistant.common.R;
import com.moke.assistant.dto.HistoryItemDto;
import com.moke.assistant.dto.VideoCorrectionResultDto;
import com.moke.assistant.service.VideoCorrectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频纠偏控制器
 */
@RestController
@RequestMapping("/api/video-correction")
public class VideoCorrectionController {

    private final VideoCorrectionService videoCorrectionService;

    public VideoCorrectionController(VideoCorrectionService videoCorrectionService) {
        this.videoCorrectionService = videoCorrectionService;
    }

    /**
     * 上传试讲视频，创建视频纠偏任务
     *
     * @param file  试讲视频文件
     * @param title 可选标题
     * @param request HTTP请求对象，用于获取Cookie中的userId
     * @return 任务 ID 和初始状态
     */
    @PostMapping("/upload")
    public R uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            HttpServletRequest request) {

        // 从Cookie中获取userId
        Long userId = getUserIdFromRequest(request);
        
        String taskId = videoCorrectionService.createTask(file, title, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("status", "PROCESSING");
        return R.ok().put("data", data);
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
     * 根据任务 ID 查询视频纠偏结果
     */
    @GetMapping("/result/{taskId}")
    public R getResult(@PathVariable String taskId) {
        VideoCorrectionResultDto dto = videoCorrectionService.getResult(taskId);
        return R.ok().put("data", dto);
    }

    /**
     * 获取历史记录列表
     */
    @GetMapping("/history")
    public R getHistoryList() {
        List<HistoryItemDto> historyList = videoCorrectionService.getHistoryList();
        return R.ok().put("data", historyList);
    }
}


