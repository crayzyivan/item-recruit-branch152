package com.item.util;

import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * TODO：功能描述
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-09  16:40
 */
@Slf4j
public class PdfModificationUtils {

    /**
     * 修改PDF中的链接
     * @param pdfBytes 原始PDF字节数组
     * @param linkReplacements 链接替换映射 (原链接关键字 -> 新链接)
     * @return 修改后的PDF字节数组
     */
    public static byte[] modifyPdfLinks(byte[] pdfBytes, Map<String, String> linkReplacements) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR, "PDF content cannot be empty");
        }

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 遍历每一页
            for (PDPage page : document.getPages()) {
                List<PDAnnotation> annotations = page.getAnnotations();

                for (PDAnnotation annotation : annotations) {
                    if (annotation instanceof PDAnnotationLink) {
                        PDAnnotationLink link = (PDAnnotationLink) annotation;
                        PDAction action = link.getAction();

                        if (action instanceof PDActionURI) {
                            PDActionURI uriAction = (PDActionURI) action;
                            String oldUrl = uriAction.getURI();
                            if (oldUrl != null) {
                                // 检查是否需要替换链接
                                for (Map.Entry<String, String> replacement : linkReplacements.entrySet()) {
                                    if (oldUrl.contains(replacement.getKey())) {
                                        uriAction.setURI(replacement.getValue());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 保存修改后的PDF
            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Failed to modify PDF links", e);
            throw new BusinessException(GlobalStatusCode.FAIL, "Failed to modify PDF content: " + e.getMessage());
        }
    }
}