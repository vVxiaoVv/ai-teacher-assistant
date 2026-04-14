package com.moke.assistant.common.utils;

import com.moke.assistant.dto.PageResultDto;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页工具类
 */
public class PageUtils {

    /**
     * 将Spring Data JPA的Page转为分页信息
     */
    public static <T> PageResultDto<T> toPageResult(Page<T> page) {
        PageResultDto<T> pageResult = new PageResultDto<>();
        pageResult.setContent(page.getContent());
        pageResult.setTotalElements(page.getTotalElements());
        pageResult.setTotalPages(page.getTotalPages());
        pageResult.setNumber(page.getNumber());
        pageResult.setSize(page.getSize());
        pageResult.setHasPrevious(page.hasPrevious());
        pageResult.setHasNext(page.hasNext());
        pageResult.setFirst(page.isFirst());
        pageResult.setLast(page.isLast());
        
        return pageResult;
    }

    /**
     * 将列表转为分页信息（简单版，用于不分页的情况）
     */
    public static <T> PageResultDto<T> toSimplePageResult(List<T> list) {
        PageResultDto<T> pageResult = new PageResultDto<>();
        pageResult.setContent(list);
        pageResult.setTotalElements((long) list.size());
        pageResult.setTotalPages(1);
        pageResult.setNumber(0);
        pageResult.setSize(list.size());
        pageResult.setHasPrevious(false);
        pageResult.setHasNext(false);
        pageResult.setFirst(true);
        pageResult.setLast(true);
        
        return pageResult;
    }
}
