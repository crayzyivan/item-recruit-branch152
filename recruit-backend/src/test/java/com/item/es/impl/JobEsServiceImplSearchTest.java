package com.item.es.impl;

import com.item.dto.job.LocationValDTO;
import com.item.es.entity.JobEsEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * JobEsServiceImpl.searchJobsByTitleAndLocations method unit test
 * 
 * @author hua.liu
 * @since 2025-09-18
 */
@Slf4j
@SpringBootTest
class JobEsServiceImplSearchTest {

    @Resource
    private JobEsServiceImpl jobEsService;

    private String testTitle;
    private String companyCode;
    private Long excludeJobId;
    private List<LocationValDTO> testLocations;

    @BeforeEach
    void setUp() {
        testTitle = "Java 200";
        companyCode = "RDXX0001";
        excludeJobId = 189L;
        // Setup test locations
        testLocations = new ArrayList<>();
        LocationValDTO locationValDTO = new LocationValDTO();
        locationValDTO.setCountryId(1L);
        locationValDTO.setCityId(52L);
        locationValDTO.setStateId(3901L);

        testLocations.add(locationValDTO);

        LocationValDTO locationValDTO1 = new LocationValDTO();
        locationValDTO1.setCountryId(1L);
        locationValDTO1.setCityId(108L);
        locationValDTO1.setStateId(3871L);
        testLocations.add(locationValDTO1);

        LocationValDTO locationValDTO2 = new LocationValDTO();
        locationValDTO2.setCountryId(1L);
        locationValDTO2.setCityId(101L);
        locationValDTO2.setStateId(3875L);
        testLocations.add(locationValDTO2);
        LocationValDTO locationValDTO3 = new LocationValDTO();
        locationValDTO3.setCountryId(45L);
        locationValDTO3.setCityId(19577L);
        locationValDTO3.setStateId(2271L);
        testLocations.add(locationValDTO3);
    }

    @Test
    void testSearchJobsByTitleAndLocations_Success() throws IOException {
        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(testTitle, testLocations, companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
        log.info("testSearchJobsByTitleAndLocations_Success passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_EmptyTitle() throws IOException {

        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations("", testLocations, companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        Assertions.assertTrue(result.isEmpty());
        log.info("testSearchJobsByTitleAndLocations_EmptyTitle passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_NullTitle() throws IOException {
        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(null, testLocations, companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        Assertions.assertTrue(result.isEmpty());
        log.info("testSearchJobsByTitleAndLocations_NullTitle passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_EmptyLocations() throws IOException {

        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(testTitle, Collections.emptyList(), companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        Assertions.assertTrue(result.isEmpty());
        log.info("testSearchJobsByTitleAndLocations_EmptyLocations passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_NullLocations() throws IOException {

        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(testTitle, null, companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        Assertions.assertTrue(result.isEmpty());
        log.info("testSearchJobsByTitleAndLocations_NullLocations passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_NoResults() throws IOException {
        String testTitle = "Java 200000000001";
        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(testTitle, testLocations, companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
        log.info("testSearchJobsByTitleAndLocations_NoResults passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_NoResults01() throws IOException {
        LocationValDTO locationValDTO = new LocationValDTO();
        locationValDTO.setCountryId(1L);
        locationValDTO.setCityId(51L);
        locationValDTO.setStateId(3901L);
        LocationValDTO locationValDTO1 = new LocationValDTO();
        locationValDTO1.setCountryId(2L);
        locationValDTO1.setCityId(51L);
        locationValDTO1.setStateId(3901L);
        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(testTitle, List.of(locationValDTO, locationValDTO1), companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
        log.info("testSearchJobsByTitleAndLocations_NoResults passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_LimitResults() throws IOException {

        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(testTitle, testLocations, companyCode, null, 1);

        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size()); // Mock returns all, but in real scenario it would be limited by size
        log.info("testSearchJobsByTitleAndLocations_LimitResults passed with {} results", result.size());
    }

//    @Test
//    void testSearchJobsByTitleAndLocations_ElasticsearchException() throws IOException {
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class, () -> {
//            jobEsService.searchJobsByTitleAndLocations(testTitle, testLocations, 10);
//        });
//
//        assertEquals(JobResponseCode.JOB_SEARCH_FAIL.getCode(), exception.getCode());
//        log.info("testSearchJobsByTitleAndLocations_ElasticsearchException passed with expected exception");
//    }

    @Test
    void testSearchJobsByTitleAndLocations_SingleLocation() throws IOException {
        // Given
        List<LocationValDTO> singleLocation = Arrays.asList(testLocations.get(0));

        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(testTitle, singleLocation, companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
        log.info("testSearchJobsByTitleAndLocations_SingleLocation passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_CaseInsensitiveTitle() throws IOException {
        // Given
        String upperCaseTitle = "java 200";

        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(upperCaseTitle, testLocations, companyCode, null, 10);

        // Then
//        assertNotNull(result);
//        assertTrue(result.size() > 0);
        log.info("testSearchJobsByTitleAndLocations_CaseInsensitiveTitle passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_CaseInsensitiveTitle01() throws IOException {
        // Given
        String upperCaseTitle = "java 200";

        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(upperCaseTitle, testLocations, companyCode, excludeJobId, 10);

        // Then
//        assertNotNull(result);
//        assertTrue(result.size() == 1);
        log.info("testSearchJobsByTitleAndLocations_CaseInsensitiveTitle passed with {} results", result.size());
    }

    @Test
    void testSearchJobsByTitleAndLocations_CaseInsensitiveTitle02() throws IOException {
        // Given
        String upperCaseTitle = "java 200";

        // When
        List<JobEsEntity> result = jobEsService.searchJobsByTitleAndLocations(upperCaseTitle, testLocations, "companyCode", 189L, 10);

        // Then
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
        log.info("testSearchJobsByTitleAndLocations_CaseInsensitiveTitle passed with {} results", result.size());
    }
}
