package com.moke.assistant.controller;

import com.moke.assistant.common.R;
import com.moke.assistant.common.utils.UserContext;
import com.moke.assistant.dto.TeacherPortraitRequest;
import com.moke.assistant.dto.TeacherPortraitResponse;
import com.moke.assistant.service.TeacherPortraitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 教师画像控制器
 */
@RestController
@RequestMapping("/api/teacher-portrait")
public class TeacherPortraitController {
    
    private static final Logger logger = LoggerFactory.getLogger(TeacherPortraitController.class);
    
    private final TeacherPortraitService teacherPortraitService;
    
    public TeacherPortraitController(TeacherPortraitService teacherPortraitService) {
        this.teacherPortraitService = teacherPortraitService;
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
                logger.warn("Header中的userId格式错误: {}", userIdFromHeader);
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
                        logger.warn("Cookie中的userId格式错误: {}", cookie.getValue());
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 生成教师画像
     * 
     * @param request 请求对象（可选，如果不提供userId则使用当前登录用户）
     * @param httpRequest HTTP请求对象
     * @return 教师画像响应
     */
    @PostMapping("/generate")
    public R generateTeacherPortrait(@RequestBody(required = false) TeacherPortraitRequest request,
                                     HttpServletRequest httpRequest) {
        logger.info("收到生成教师画像请求，request: {}", request);
        try {
            if (request == null) {
                request = new TeacherPortraitRequest();
            }
            
            // 如果请求中没有指定userId，尝试从UserContext或请求中获取
            if (request.getUserId() == null) {
                Long userIdFromContext = UserContext.getUserId();
                if (userIdFromContext != null) {
                    request.setUserId(userIdFromContext);
                    logger.info("从UserContext获取到userId: {}", userIdFromContext);
                } else {
                    // 如果UserContext中没有，尝试从请求中获取
                    Long userIdFromRequest = getUserIdFromRequest(httpRequest);
                    if (userIdFromRequest != null) {
                        request.setUserId(userIdFromRequest);
                        logger.info("从请求中获取到userId: {}", userIdFromRequest);
                    }
                }
            }
            
            TeacherPortraitResponse response = teacherPortraitService.generateTeacherPortrait(request);
            logger.info("教师画像生成成功");
            return R.ok().put("data", response);
        } catch (Exception e) {
            logger.error("生成教师画像失败", e);
            throw e; // 让全局异常处理器处理
        }
    }
    
    /**
     * 获取指定用户的教师画像（从数据库查询）
     * 
     * @param userId 用户ID（可选，如果不提供则使用当前登录用户）
     * @param httpRequest HTTP请求对象
     * @return 教师画像响应
     */
    @GetMapping("/get")
    public R getTeacherPortrait(@RequestParam(required = false) Long userId,
                                HttpServletRequest httpRequest) {
        // 确定用户ID
        Long targetUserId = userId;
        if (targetUserId == null) {
            Long userIdFromContext = UserContext.getUserId();
            if (userIdFromContext != null) {
                targetUserId = userIdFromContext;
            } else {
                targetUserId = getUserIdFromRequest(httpRequest);
            }
        }
        
        if (targetUserId == null) {
            return R.error(400, "无法确定用户ID，请先登录");
        }
        
        TeacherPortraitResponse response = teacherPortraitService.getTeacherPortrait(targetUserId);
        if (response == null) {
            return R.error(404, "该用户暂无教师画像，请先生成");
        }
        
        return R.ok().put("data", response);
    }
    
    /**
     * 获取所有用户的教师画像列表
     * 
     * @return 教师画像列表
     */
    @GetMapping("/list")
    public R getAllTeacherPortraits() {
        try {
            List<TeacherPortraitResponse> list = teacherPortraitService.getAllTeacherPortraits();
            return R.ok().put("data", list);
        } catch (Exception e) {
            logger.error("获取教师画像列表失败", e);
            return R.error(500, "获取教师画像列表失败: " + e.getMessage());
        }
    }
}

