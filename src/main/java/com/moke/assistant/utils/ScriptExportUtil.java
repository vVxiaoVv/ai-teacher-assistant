package com.moke.assistant.utils;

import com.moke.assistant.entity.Script;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 逐字稿导出工具类
 */
@Component
public class ScriptExportUtil {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExportUtil.class);

    /**
     * 导出为TXT格式
     *
     * @param script 逐字稿
     * @return TXT内容
     */
    public String exportToTxt(Script script) {
        if (script == null) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        content.append(script.getTitle()).append("\n\n");
        content.append(repeatString("=", 50)).append("\n\n");
        content.append(script.getContent());

        return content.toString();
    }

    /**
     * 导出为DOCX格式
     *
     * @param script 逐字稿
     * @return DOCX文件字节数组
     */
    public byte[] exportToDocx(Script script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("逐字稿不能为空");
        }

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 添加标题
            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(script.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(18);

            // 添加空行
            document.createParagraph();

            // 添加分隔线
            XWPFParagraph separatorParagraph = document.createParagraph();
            XWPFRun separatorRun = separatorParagraph.createRun();
            separatorRun.setText(repeatString("=", 50));
            separatorRun.setFontSize(10);

            // 添加空行
            document.createParagraph();
            document.createParagraph();

            // 添加内容
            String content = script.getContent();
            String[] lines = content.split("\n");

            for (String line : lines) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
                run.setFontSize(12);

                // 如果是"老师："或学生姓名开头的行，设置为粗体
                if (line.trim().startsWith("老师：") || 
                    (line.contains("：") && !line.trim().startsWith("="))) {
                    run.setBold(true);
                }
            }

            document.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            logger.error("导出DOCX失败", e);
            throw new IOException("导出DOCX失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重复字符串（Java 8兼容）
     */
    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}

