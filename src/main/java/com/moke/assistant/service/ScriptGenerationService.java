package com.moke.assistant.service;

import com.moke.assistant.entity.Script;

/**
 * 逐字稿生成服务接口
 */
public interface ScriptGenerationService {

    /**
     * 生成逐字稿
     *
     * @param lessonPlanId 教案ID
     * @return 生成的逐字稿
     */
    Script generateScript(Long lessonPlanId);

    /**
     * 根据教案ID获取逐字稿
     *
     * @param lessonPlanId 教案ID
     * @return 逐字稿，如果不存在返回null
     */
    Script getScriptByLessonPlanId(Long lessonPlanId);

    /**
     * 根据ID获取逐字稿
     *
     * @param id 逐字稿ID
     * @return 逐字稿，如果不存在返回null
     */
    Script getScriptById(Long id);
}

