//package com.item.util;
//
//import com.item.framework.error.BusinessException;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.ByteArrayOutputStream;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
///**
// * Excel工具类单元测试
// *
// * @author recruit-backend
// * @since 1.0.0
// */
//@Slf4j
//public class ExcelUtilsTest {
//
//    @TempDir
//    Path tempDir;
//
//    private Path testExcelFile;
//    private Path emptyExcelFile;
//    private Path invalidFile;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        // 创建测试用的Excel文件
//        createTestExcelFile();
//        createEmptyExcelFile();
//        createInvalidFile();
//    }
//
//    @Test
//    void testGenerateExcelTemplate_Success() {
//        // 准备测试数据
//        List<String> headers = Arrays.asList("Job Title", "Job Category", "Job Type", "Salary");
//        Map<String, List<String>> dictionaryData = new HashMap<>();
//        dictionaryData.put("Job Category", Arrays.asList("IT", "Marketing", "Sales"));
//        dictionaryData.put("Job Type", Arrays.asList("Full Time", "Part Time", "Contract"));
//
//        // 执行测试
//        byte[] templateData = ExcelUtils.generateExcelTemplate(headers, dictionaryData);
//
//        // 验证结果
//        assertNotNull(templateData);
//        assertTrue(templateData.length > 0);
//        log.info("Generated template size: {} bytes", templateData.length);
//    }
//
//    @Test
//    void testGenerateExcelTemplate_EmptyHeaders() {
//        // 测试空headers
//        List<String> headers = Arrays.asList();
//        Map<String, List<String>> dictionaryData = new HashMap<>();
//
//        // 执行测试并验证异常
//        assertThrows(BusinessException.class, () -> {
//            ExcelUtils.generateExcelTemplate(headers, dictionaryData);
//        });
//    }
//
//    @Test
//    void testValidateExcelFormat_ValidFile() throws IOException {
//        // 创建有效的Excel文件
//        MultipartFile validFile = new MockMultipartFile(
//                "test.xlsx",
//                "test.xlsx",
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//                Files.readAllBytes(testExcelFile)
//        );
//
//        // 验证文件格式 - 应该不抛出异常
//        ExcelUtils.validateExcelFormat(validFile);
//    }
//
//    @Test
//    void testValidateExcelFormat_InvalidFormat() throws IOException {
//        // 创建无效格式的文件
//        MultipartFile invalidFile = new MockMultipartFile(
//                "test.txt",
//                "test.txt",
//                "text/plain",
//                "This is not an Excel file".getBytes()
//        );
//
//        // 验证应该抛出异常
//        assertThrows(BusinessException.class, () -> {
//            ExcelUtils.validateExcelFormat(invalidFile);
//        });
//    }
//
//    @Test
//    void testValidateExcelFormat_EmptyFile() {
//        // 创建空文件
//        MultipartFile emptyFile = new MockMultipartFile(
//                "empty.xlsx",
//                "empty.xlsx",
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//                new byte[0]
//        );
//
//        // 验证应该抛出异常
//        assertThrows(BusinessException.class, () -> {
//            ExcelUtils.validateExcelFormat(emptyFile);
//        });
//    }
//
//    @Test
//    void testReadExcelData_Success() throws IOException {
//        // 创建有效的Excel文件
//        MultipartFile validFile = new MockMultipartFile(
//                "test.xlsx",
//                "test.xlsx",
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//                Files.readAllBytes(testExcelFile)
//        );
//
//        // 执行测试
//        List<Map<String, Object>> data = ExcelUtils.readExcelData(validFile);
//
//        // 验证结果
//        assertNotNull(data);
//        assertFalse(data.isEmpty());
//        log.info("Read {} rows from Excel", data.size());
//
//        // 验证数据内容
//        Map<String, Object> firstRow = data.get(0);
//        assertTrue(firstRow.containsKey("Job Title"));
//        assertEquals("Software Engineer", firstRow.get("Job Title"));
//    }
//
//    @Test
//    void testReadExcelData_EmptyFile() throws IOException {
//        // 创建空Excel文件
//        MultipartFile emptyFile = new MockMultipartFile(
//                "empty.xlsx",
//                "empty.xlsx",
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//                Files.readAllBytes(emptyExcelFile)
//        );
//
//        // 执行测试
//        List<Map<String, Object>> data = ExcelUtils.readExcelData(emptyFile);
//
//        // 验证结果 - 应该返回空列表
//        assertNotNull(data);
//        assertTrue(data.isEmpty());
//    }
//
//    @Test
//    void testReadExcelData_InvalidFile() throws IOException {
//        // 创建无效文件
//        MultipartFile invalidFile = new MockMultipartFile(
//                "invalid.xlsx",
//                "invalid.xlsx",
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//                Files.readAllBytes(invalidFile)
//        );
//
//        // 验证应该抛出异常
//        assertThrows(BusinessException.class, () -> {
//            ExcelUtils.readExcelData(invalidFile);
//        });
//    }
//
//    // Helper methods to create test files
//
//    private void createTestExcelFile() throws IOException {
//        testExcelFile = tempDir.resolve("test.xlsx");
//
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Jobs");
//
//            // 创建表头
//            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Job Title");
//            headerRow.createCell(1).setCellValue("Job Category");
//            headerRow.createCell(2).setCellValue("Job Type");
//            headerRow.createCell(3).setCellValue("Min Salary");
//            headerRow.createCell(4).setCellValue("Max Salary");
//
//            // 创建数据行
//            org.apache.poi.ss.usermodel.Row dataRow1 = sheet.createRow(1);
//            dataRow1.createCell(0).setCellValue("Software Engineer");
//            dataRow1.createCell(1).setCellValue("IT");
//            dataRow1.createCell(2).setCellValue("Full Time");
//            dataRow1.createCell(3).setCellValue(50000);
//            dataRow1.createCell(4).setCellValue(80000);
//
//            org.apache.poi.ss.usermodel.Row dataRow2 = sheet.createRow(2);
//            dataRow2.createCell(0).setCellValue("Marketing Manager");
//            dataRow2.createCell(1).setCellValue("Marketing");
//            dataRow2.createCell(2).setCellValue("Full Time");
//            dataRow2.createCell(3).setCellValue(40000);
//            dataRow2.createCell(4).setCellValue(60000);
//
//            // 写入文件
//            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//                workbook.write(outputStream);
//                Files.write(testExcelFile, outputStream.toByteArray());
//            }
//        }
//    }
//
//    private void createEmptyExcelFile() throws IOException {
//        emptyExcelFile = tempDir.resolve("empty.xlsx");
//
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Empty");
//
//            // 只创建表头，没有数据
//            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Job Title");
//
//            // 写入文件
//            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//                workbook.write(outputStream);
//                Files.write(emptyExcelFile, outputStream.toByteArray());
//            }
//        }
//    }
//
//    private void createInvalidFile() throws IOException {
//        invalidFile = tempDir.resolve("invalid.xlsx");
//        // 创建一个非Excel格式的文件
//        Files.write(invalidFile, "This is not an Excel file".getBytes());
//    }
//}
