package com.moke.assistant.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件上传工具类
 */
@Component
public class FileUploadUtil {

    // 上传文件保存路径
    private static final String UPLOAD_PATH = "src/main/resources/static/upload/";

    // 允许的图片文件类型
    private static final String[] ALLOWED_IMAGE_TYPES = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    /**
     * 上传图片文件
     *
     * @param file 上传的图片文件
     * @return 图片的访问URL
     * @throws IOException IO异常
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        boolean isAllowedType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(fileExtension)) {
                isAllowedType = true;
                break;
            }
        }

        if (!isAllowedType) {
            throw new IllegalArgumentException("只允许上传图片文件（jpg、jpeg、png、gif、bmp）");
        }

        // 创建上传目录
        File uploadDir = new File(UPLOAD_PATH);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String uniqueFilename = generateUniqueFilename(fileExtension);

        // 保存文件
        File dest = new File(UPLOAD_PATH + uniqueFilename);
        file.transferTo(dest);

        // 返回文件访问URL
        return "/upload/" + uniqueFilename;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名（包含点号）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 生成唯一文件名
     *
     * @param fileExtension 文件扩展名
     * @return 唯一文件名
     */
    private String generateUniqueFilename(String fileExtension) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timeStr = now.format(formatter);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return timeStr + "_" + uuid + fileExtension;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        // 去除URL前缀，获取实际文件路径
        String actualPath = filePath;
        if (filePath.startsWith("/upload/")) {
            actualPath = UPLOAD_PATH + filePath.substring(8);
        }

        File file = new File(actualPath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }
}