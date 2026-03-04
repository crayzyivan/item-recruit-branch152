package com.item.util;

import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileParseUtils {

    /**
     * 通用入口：docx/pdf/txt转pdf
     */
    public static String parseFile(String filePath) throws Exception {
        String name = filePath.toLowerCase();
        if (name.endsWith(".pdf")) {
            return parsePdf(filePath);
        } else if (name.endsWith(".docx")) {
            return parseDocx(filePath);
        } else if (name.endsWith(".txt")) {
            return parseTxt(filePath);
        } else {
            throw new BusinessException(GlobalStatusCode.UNSUPPORTED_FILE_TYPE,"Only PDF, DOCX, and TXT formats are supported.");
        }
    }

    /**
     * 解析docx文件内容
     */
    public static String parseDocx(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    /**
     * 解析pdf文件内容
     */
    public static String parsePdf(String filePath) throws Exception {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析pdf文件内容
     */
    public static String parsePdf(InputStream in) throws Exception {
        try (PDDocument document = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析txt文件内容
     */
    public static String parseTxt(String filePath) throws Exception {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
} 