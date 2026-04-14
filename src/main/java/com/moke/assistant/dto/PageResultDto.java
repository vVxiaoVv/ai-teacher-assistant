package com.moke.assistant.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页结果DTO
 */
@Data
public class PageResultDto<T> {
    
    /**
     * 数据列表
     */
    private List<T> content;
    
    /**
     * 总记录数
     */
    private Long totalElements;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 当前页码（从0开始）
     */
    private Integer number;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 是否是第一页
     */
    private Boolean first;
    
    /**
     * 是否是最后一页
     */
    private Boolean last;
}

