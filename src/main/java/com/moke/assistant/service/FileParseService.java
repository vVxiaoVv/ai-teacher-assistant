package com.moke.assistant.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件解析服务接口
 */
public interface FileParseService {

    /**
     * 解析文件为文本内容
     *
     * @param file 上传的文件
     * @return 文件文本内容
     * @throws IOException IO异常
     */
    String parseFileToString(MultipartFile file) throws IOException;

    /**
     * 判断文件类型是否支持
     *
     * @param filename 文件名
     * @return 是否支持
     */
    boolean isSupported(String filename);
}

