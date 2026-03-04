//package com.item.service;
//
//import com.item.dto.JobCategoryDto;
//import com.item.dto.JobModeDto;
//import com.item.dto.JobTypeDto;
//import com.item.dto.LocationDto;
//import com.item.dto.iam.IamUserContextDTO;
//import com.item.dto.job.JobCreateDTO;
//import com.item.dto.job.JobExcelDataDTO;
//import com.item.dto.job.JobExcelValidationErrorDTO;
//import com.item.framework.error.BusinessException;
//import com.item.service.impl.ExcelJobServiceImpl;
//import com.item.vo.JobExcelPreviewVO;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
///**
// * Excel职位服务单元测试
// *
// * @author recruit-backend
// * @since 1.0.0
// */
//@Slf4j
//@ExtendWith(MockitoExtension.class)
//public class ExcelJobServiceTest {
//
//    @Mock
//    private JobService jobService;
//
//    @Mock
//    private JobCategoryService jobCategoryService;
//
//    @Mock
//    private JobTypeService jobTypeService;
//
//    @Mock
//    private LocationService locationService;
//
//    @Mock
//    private JobModeService jobModeService;
//
//    @Mock
//    private JobDomainService jobDomainService;
//
////    @InjectMocks
////    private ExcelJobServiceImpl excelJobService;
//
//    private List<JobCategoryDto> mockCategories;
//    private List<JobTypeDto> mockTypes;
//    private List<JobModeDto> mockModes;
//    private List<LocationDto> mockLocations;
//
//    @BeforeEach
//    void setUp() {
//        // 准备Mock数据
//        setupMockData();
//        setupMockBehavior();
//    }
//
//    @Test
//    void testGenerateJobTemplate_Success() {
//        // 执行测试
////        byte[] templateData = excelJobService.generateJobTemplate();
//
//        // 验证结果
//        assertNotNull(templateData);
//        assertTrue(templateData.length > 0);
//        log.info("Generated template size: {} bytes", templateData.length);
//    }
//
//    @Test
//    void testGetDictionaryData_Success() {
//        // 执行测试
//        Map<String, List<String>> dictionaryData = excelJobService.getDictionaryData();
//
//        // 验证结果
//        assertNotNull(dictionaryData);
//        assertFalse(dictionaryData.isEmpty());
//
//        // 验证包含必要的字典数据
//        assertTrue(dictionaryData.containsKey("Job Category"));
//        assertTrue(dictionaryData.containsKey("Job Type"));
//        assertTrue(dictionaryData.containsKey("Location Type"));
//        assertTrue(dictionaryData.containsKey("Locations"));
//
//        // 验证数据内容
//        assertEquals(2, dictionaryData.get("Job Category").size());
//        assertEquals(3, dictionaryData.get("Job Type").size());
//        log.info("Dictionary data: {}", dictionaryData);
//    }
//
//    @Test
//    void testGetExcelHeaders_Success() {
//        // 执行测试
//        List<String> headers = excelJobService.getExcelHeaders();
//
//        // 验证结果
//        assertNotNull(headers);
//        assertFalse(headers.isEmpty());
//        assertEquals(20, headers.size()); // 确保包含所有必要的列
//
//        // 验证包含关键列
//        assertTrue(headers.contains("Job Title"));
//        assertTrue(headers.contains("Job Category"));
//        assertTrue(headers.contains("Job Type"));
//        assertTrue(headers.contains("Location Type"));
//        log.info("Excel headers: {}", headers);
//    }
//
//    @Test
//    void testValidateSingleRow_ValidData() {
//        // 准备测试数据
//        JobExcelDataDTO validRowData = createValidExcelData();
//
//        // 执行测试
//        List<JobExcelValidationErrorDTO> errors = excelJobService.validateSingleRow(validRowData, 2, "TEST_COMPANY");
//
//        // 验证结果
//        assertNotNull(errors);
//        assertTrue(errors.isEmpty()); // 有效数据应该没有错误
//        log.info("Validation passed for valid data");
//    }
//
//    @Test
//    void testValidateSingleRow_InvalidData() {
//        // 准备测试数据 - 缺少必填字段
//        JobExcelDataDTO invalidRowData = createInvalidExcelData();
//
//        // 执行测试
//        List<JobExcelValidationErrorDTO> errors = excelJobService.validateSingleRow(invalidRowData, 2, "TEST_COMPANY");
//
//        // 验证结果
//        assertNotNull(errors);
//        assertFalse(errors.isEmpty()); // 无效数据应该有错误
//
//        // 验证错误类型
//        boolean hasRequiredFieldError = errors.stream()
//                .anyMatch(error -> "Job Title".equals(error.getColumnName()));
//        assertTrue(hasRequiredFieldError);
//        log.info("Found {} validation errors for invalid data", errors.size());
//    }
//
//    @Test
//    void testValidateJobData_BatchValidation() {
//        // 准备测试数据
//        List<JobExcelDataDTO> excelDataList = Arrays.asList(
//                createValidExcelData(),
//                createInvalidExcelData()
//        );
//
//        // 执行测试
//        List<JobExcelValidationErrorDTO> errors = excelJobService.validateJobData(excelDataList, "TEST_COMPANY");
//
//        // 验证结果
//        assertNotNull(errors);
//        assertFalse(errors.isEmpty()); // 应该有来自第二行的错误
//        log.info("Batch validation found {} errors", errors.size());
//    }
//
//    @Test
//    void testConvertToJobCreateDTOs_Success() {
//        // 准备测试数据
//        List<JobExcelDataDTO> excelDataList = Arrays.asList(createValidExcelData());
//        IamUserContextDTO userContext = createMockUserContext();
//
//        // 执行测试
//        List<JobCreateDTO> jobCreateDTOs = excelJobService.convertToJobCreateDTOs(excelDataList, userContext);
//
//        // 验证结果
//        assertNotNull(jobCreateDTOs);
//        assertFalse(jobCreateDTOs.isEmpty());
//        assertEquals(1, jobCreateDTOs.size());
//
//        JobCreateDTO jobCreateDTO = jobCreateDTOs.get(0);
//        assertEquals("Software Engineer", jobCreateDTO.getTitle());
//        assertEquals(userContext.getCustomerId(), jobCreateDTO.getCustomerId());
//        assertEquals(userContext.getMasterAccountId(), jobCreateDTO.getMasterAccountId());
//        log.info("Converted Excel data to JobCreateDTO: {}", jobCreateDTO.getTitle());
//    }
//
//    @Test
//    void testConvertToJobCreateDTOs_EmptyList() {
//        // 准备测试数据
//        List<JobExcelDataDTO> emptyList = Collections.emptyList();
//        IamUserContextDTO userContext = createMockUserContext();
//
//        // 执行测试
//        List<JobCreateDTO> jobCreateDTOs = excelJobService.convertToJobCreateDTOs(emptyList, userContext);
//
//        // 验证结果
//        assertNotNull(jobCreateDTOs);
//        assertTrue(jobCreateDTOs.isEmpty());
//    }
//
//    @Test
//    void testCheckDuplicateJobs_WithDuplicates() {
//        // 准备测试数据 - 两个相同标题的工作
//        JobExcelDataDTO job1 = createValidExcelData();
//        JobExcelDataDTO job2 = createValidExcelData();
//        job2.setRowIndex(3);
//
//        List<JobExcelDataDTO> excelDataList = Arrays.asList(job1, job2);
//
//        // 执行测试
//        List<JobExcelValidationErrorDTO> errors = excelJobService.checkDuplicateJobs(excelDataList, "TEST_COMPANY");
//
//        // 验证结果
//        assertNotNull(errors);
//        assertFalse(errors.isEmpty()); // 应该发现重复
//
//        boolean hasDuplicateError = errors.stream()
//                .anyMatch(error -> error.getRowIndex().equals(3));
//        assertTrue(hasDuplicateError);
//        log.info("Found duplicate job error as expected");
//    }
//
//    @Test
//    void testGetFieldMappingInfo_Success() {
//        // 执行测试
//        Map<String, String> fieldMapping = excelJobService.getFieldMappingInfo();
//
//        // 验证结果
//        assertNotNull(fieldMapping);
//        assertFalse(fieldMapping.isEmpty());
//
//        // 验证关键映射
//        assertEquals("title", fieldMapping.get("Job Title"));
//        assertEquals("categoryId", fieldMapping.get("Job Category"));
//        assertEquals("typeId", fieldMapping.get("Job Type"));
//        log.info("Field mapping: {}", fieldMapping);
//    }
//
//    // Helper methods
//
//    private void setupMockData() {
//        // Job Categories
//        JobCategoryDto category1 = new JobCategoryDto();
//        category1.setId(1);
//        category1.setName("IT");
//
//        JobCategoryDto category2 = new JobCategoryDto();
//        category2.setId(2);
//        category2.setName("Marketing");
//
//        mockCategories = Arrays.asList(category1, category2);
//
//        // Job Types
//        JobTypeDto type1 = new JobTypeDto();
//        type1.setId(1);
//        type1.setName("Full Time");
//
//        JobTypeDto type2 = new JobTypeDto();
//        type2.setId(2);
//        type2.setName("Part Time");
//
//        JobTypeDto type3 = new JobTypeDto();
//        type3.setId(3);
//        type3.setName("Contract");
//
//        mockTypes = Arrays.asList(type1, type2, type3);
//
//        // Job Modes
//        JobModeDto mode1 = new JobModeDto();
//        mode1.setId(1);
//        mode1.setName("On-site");
//
//        JobModeDto mode2 = new JobModeDto();
//        mode2.setId(2);
//        mode2.setName("Remote");
//
//        mockModes = Arrays.asList(mode1, mode2);
//
//        // Locations
//        LocationDto location1 = new LocationDto();
//        location1.setId(1);
//        location1.setName("New York");
//
//        LocationDto location2 = new LocationDto();
//        location2.setId(2);
//        location2.setName("San Francisco");
//
//        mockLocations = Arrays.asList(location1, location2);
//    }
//
//    private void setupMockBehavior() {
//        when(jobService.getAllJobCategories()).thenReturn(mockCategories);
//        when(jobService.getAllJobTypes()).thenReturn(mockTypes);
//        when(jobService.getAllJobModes()).thenReturn(mockModes);
//        when(jobService.getAllLocations()).thenReturn(mockLocations);
//    }
//
//    private JobExcelDataDTO createValidExcelData() {
//        JobExcelDataDTO data = new JobExcelDataDTO();
//        data.setRowIndex(2);
//        data.setTitle("Software Engineer");
//        data.setCategoryName("IT");
//        data.setTypeName("Full Time");
//        data.setModeName("On-site");
//        data.setLocationNames(Arrays.asList("New York"));
//        data.setMinSalary("50000");
//        data.setMaxSalary("80000");
//        data.setSalaryTypeName("Yearly");
//        data.setCurrencyName("USD");
//        data.setJobDetail("Develop and maintain software applications");
//        data.setMainDuty(Arrays.asList("Code development", "Testing", "Documentation"));
//        data.setSkills(Arrays.asList("Java", "Spring", "MySQL"));
//        data.setNeedListed("Yes");
//        data.setHotList("No");
//        data.setEnableWrittenTest("No");
//        data.setInterviewLength("30");
//        return data;
//    }
//
//    private JobExcelDataDTO createInvalidExcelData() {
//        JobExcelDataDTO data = new JobExcelDataDTO();
//        data.setRowIndex(3);
//        // 缺少必填字段title
//        data.setCategoryName("InvalidCategory");
//        data.setTypeName("InvalidType");
//        data.setMinSalary("invalid_number");
//        return data;
//    }
//
//    private IamUserContextDTO createMockUserContext() {
//        IamUserContextDTO userContext = new IamUserContextDTO();
//        userContext.setId("12345");
//        userContext.setCustomerId(100L);
//        userContext.setMasterAccountId(200L);
//        userContext.setCompanyCode("TEST_COMPANY");
//        return userContext;
//    }
//}
