package com.moke.assistant.common.utils;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 查询参数类（renren-fast风格）
 */
@Data
public class Query implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 当前页码
     */
    private int page = 1;
    
    /**
     * 每页显示条数，默认 10
     */
    private int limit = 10;
    
    /**
     * 排序字段
     */
    private String orderField;
    
    /**
     * 排序方式，可选值(asc、desc)
     */
    private String order;
    
    /**
     * 数据权限过滤
     */
    private String sqlFilter;
    
    /**
     * 查询条件
     */
    private Map<String, Object> params = new LinkedHashMap<>();
    
    /**
     * 添加查询条件
     */
    public Query put(String key, Object value) {
        params.put(key, value);
        return this;
    }
    
    /**
     * 获取查询条件
     */
    public Object get(String key) {
        return params.get(key);
    }
}

