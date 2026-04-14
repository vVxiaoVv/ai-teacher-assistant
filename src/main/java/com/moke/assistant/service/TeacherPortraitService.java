package com.moke.assistant.service;

import com.moke.assistant.dto.TeacherPortraitRequest;
import com.moke.assistant.dto.TeacherPortraitResponse;

/**
 * 教师画像服务接口
 */
public interface TeacherPortraitService {
    
    /**
     * 生成教师画像
     * 
     * @param request 请求对象
     * @return 教师画像响应
     */
    TeacherPortraitResponse generateTeacherPortrait(TeacherPortraitRequest request);
    
    /**
     * 获取指定用户的教师画像（从数据库查询）
     * 
     * @param userId 用户ID
     * @return 教师画像响应，如果不存在则返回null
     */
    TeacherPortraitResponse getTeacherPortrait(Long userId);
    
    /**
     * 获取所有用户的教师画像列表
     * 
     * @return 教师画像响应列表
     */
    java.util.List<TeacherPortraitResponse> getAllTeacherPortraits();
}


