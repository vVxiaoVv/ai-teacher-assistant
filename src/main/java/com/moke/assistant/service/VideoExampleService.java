package com.moke.assistant.service;

import com.moke.assistant.dto.PageResultDto;
import com.moke.assistant.dto.VideoExampleDto;
import com.moke.assistant.entity.VideoExample;
import org.springframework.data.domain.Pageable;

/**
 * 视频范例服务接口
 */
public interface VideoExampleService {
    
    /**
     * 保存视频范例
     * 
     * @param lessonPlanId 关联的教案ID
     * @param videoUrl 视频URL
     * @param topic 视频主题
     * @param description 视频描述（可选）
     * @return 保存的视频范例实体
     */
    VideoExample saveVideoExample(Long lessonPlanId, String videoUrl, String topic, String description);
    
    /**
     * 根据ID查询视频范例
     * 
     * @param id 视频范例ID
     * @return 视频范例实体
     */
    VideoExample getVideoExampleById(Long id);
    
    /**
     * 分页查询视频范例列表（支持条件查询）
     * 
     * @param topic 主题模糊匹配（可选）
     * @param lessonPlanId 关联的教案ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    PageResultDto<VideoExampleDto> getVideoExampleList(String topic, Long lessonPlanId, String startTime, String endTime, Pageable pageable);
}