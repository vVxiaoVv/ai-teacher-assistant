package com.moke.assistant.service.impl;

import com.moke.assistant.service.FileParseService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * 文件解析服务实现类
 */
@Service
public class FileParseServiceImpl implements FileParseService {

    private static final Logger logger = LoggerFactory.getLogger(FileParseServiceImpl.class);

    @Override
    public String parseFileToString(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(filename).toLowerCase();
        logger.info("解析文件: {}, 扩展名: {}", filename, extension);

        try (InputStream inputStream = file.getInputStream()) {
            switch (extension) {
                case ".txt":
                    return parseTxt(inputStream);
                case ".doc":
                    return parseDoc(inputStream);
                case ".docx":
                    return parseDocx(inputStream);
                case ".pdf":
                    return parsePdf(inputStream);
                default:
                    throw new IllegalArgumentException("不支持的文件格式: " + extension);
            }
        }
    }

    @Override
    public boolean isSupported(String filename) {
        if (filename == null) {
            return false;
        }
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".txt") || extension.equals(".doc") || 
               extension.equals(".docx") || extension.equals(".pdf");
    }

    /**
     * 解析TXT文件
     */
    private String parseTxt(InputStream inputStream) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            StringBuilder contentBuilder = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;
            
            while ((bytesRead = reader.read(buffer)) != -1) {
                contentBuilder.append(buffer, 0, bytesRead);
            }
            
            return contentBuilder.toString();
        }
    }

    /**
     * 解析DOC文件
     */
    private String parseDoc(InputStream inputStream) throws IOException {
        HWPFDocument document = null;
        try {
            document = new HWPFDocument(inputStream);
            
            // 方法1：尝试使用 WordExtractor（可能在某些文档上失败）
            try {
                WordExtractor extractor = new WordExtractor(document);
                String text = extractor.getText();
                extractor.close();
                if (text != null && !text.trim().isEmpty()) {
                    return text;
                }
            } catch (NullPointerException e) {
                logger.warn("WordExtractor 遇到 NullPointerException，尝试使用备用方法: {}", e.getMessage());
                // 继续使用备用方法
            } catch (Exception e) {
                logger.warn("WordExtractor 解析失败，尝试使用备用方法: {}", e.getMessage());
                // 继续使用备用方法
            }
            
            // 方法2：使用 Range 对象直接提取文本（备用方法）
            try {
                Range range = document.getRange();
                String text = range.text();
                if (text != null && !text.trim().isEmpty()) {
                    return text;
                }
            } catch (Exception e) {
                logger.warn("使用 Range 提取文本失败: {}", e.getMessage());
            }
            
            // 如果两种方法都失败，抛出异常
            throw new IOException("无法从DOC文件中提取文本内容，文档可能格式异常或已损坏");
            
        } catch (IOException e) {
            logger.error("解析DOC文件失败", e);
            throw e;
        } catch (Exception e) {
            logger.error("解析DOC文件失败", e);
            throw new IOException("解析DOC文件失败: " + e.getMessage(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    logger.warn("关闭HWPFDocument失败", e);
                }
            }
        }
    }

    /**
     * 解析DOCX文件
     */
    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (Exception e) {
            logger.error("解析DOCX文件失败", e);
            throw new IOException("解析DOCX文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析PDF文件
     */
    private String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            logger.error("解析PDF文件失败", e);
            throw new IOException("解析PDF文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}

