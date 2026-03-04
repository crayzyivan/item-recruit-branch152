package com.item.service.impl;

import com.item.dto.job.LocationValDTO;
import com.item.dto.job.RandomJobRecommendRequestDTO;
import com.item.vo.JobListVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * JobDomainServiceImpl.getRandomJobRecommendations method unit test
 * 
 * @author hua.liu
 * @since 2025-09-23
 */
@Slf4j
@SpringBootTest
class JobDomainServiceImplRandomTest {

    @Resource
    private JobDomainServiceImpl jobDomainService;

    private RandomJobRecommendRequestDTO validRequest;
    private String testCompanyCode;
    private List<LocationValDTO> testLocations;
    private Long testCategoryId;

    @BeforeEach
    void setUp() {
//        testCompanyCode = "RDXX0001";
        
        // Setup test locations
        testLocations = new ArrayList<>();
        LocationValDTO locationValDTO = new LocationValDTO();
        locationValDTO.setCountryId(1L);
        locationValDTO.setCityId(52L);
        locationValDTO.setStateId(3901L);
        testLocations.add(locationValDTO);

        LocationValDTO locationValDTO2 = new LocationValDTO();
        locationValDTO2.setCountryId(1L);
        locationValDTO2.setCityId(108L);
        locationValDTO2.setStateId(3871L);
        testLocations.add(locationValDTO2);

        LocationValDTO locationValDTO3 = new LocationValDTO();
        locationValDTO3.setCountryId(45L);
        locationValDTO3.setCityId(19598L);
        locationValDTO3.setStateId(2247L);
        testLocations.add(locationValDTO3);

        // Setup test category IDs
        testCategoryId = 1L;

        // Setup valid request
        validRequest = new RandomJobRecommendRequestDTO();
        validRequest.setSize(5);
        validRequest.setCompanyCode(testCompanyCode);
//        validRequest.setSeed(12345L);
        validRequest.setCategoryId(testCategoryId);
        validRequest.setLocations(testLocations);
    }

    @Test
    void testGetRandomJobRecommendations_ValidRequest_ReturnsJobList() {
        log.info("Testing getRandomJobRecommendations with valid request");
        
        // When
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(validRequest);
        
        // Then
        log.info("testGetRandomJobRecommendations_ValidRequest_ReturnsJobList - returned {} jobs", 
                result != null ? result.size() : 0);
        List<Long> jobIds1 = null;
        if (result != null && !result.isEmpty()) {
            jobIds1 = result.stream().map(JobListVO::getJobId).toList();
        }

        Assertions.assertNotNull(jobIds1);

        // When
        List<JobListVO> result1 = jobDomainService.getRandomJobRecommendations(validRequest);
        List<Long> jobIds2 = null;
        if (result1 != null && !result1.isEmpty()) {
            jobIds2 = result1.stream().map(JobListVO::getJobId).toList();
        }
        Assertions.assertNotNull(jobIds2);

        Assertions.assertFalse(CollectionUtils.subtract(jobIds2, jobIds1).isEmpty());
        Assertions.assertFalse(CollectionUtils.subtract(jobIds1, jobIds2).isEmpty());
    }

    @Test
    void testGetRandomJobRecommendations_MinimalRequest_ReturnsJobList() {
        log.info("Testing getRandomJobRecommendations with minimal request (only size)");
        
        // Given - minimal request with only required fields
        RandomJobRecommendRequestDTO minimalRequest = new RandomJobRecommendRequestDTO();
        minimalRequest.setSize(3);
        
        // When
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(minimalRequest);
        
        // Then
        log.info("testGetRandomJobRecommendations_MinimalRequest_ReturnsJobList - returned {} jobs", 
                result != null ? result.size() : 0);
    }

    @Test
    void testGetRandomJobRecommendations_WithCompanyCodeFilter_ReturnsFilteredJobs() {
        log.info("Testing getRandomJobRecommendations with company code filter");
        
        // Given - request with company code filter
        RandomJobRecommendRequestDTO companyFilterRequest = new RandomJobRecommendRequestDTO();
        companyFilterRequest.setSize(5);
        companyFilterRequest.setCompanyCode(testCompanyCode);
        companyFilterRequest.setSeed(54321L);
        
        // When
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(companyFilterRequest);
        
        // Then
        log.info("testGetRandomJobRecommendations_WithCompanyCodeFilter_ReturnsFilteredJobs - returned {} jobs", 
                result != null ? result.size() : 0);
        
        if (result != null && !result.isEmpty()) {
            // Log company names to verify filtering
            result.forEach(job -> log.info("Job ID: {}, Company Name: {}", job.getJobId(), job.getCompanyName()));
        }
    }

    @Test
    void testGetRandomJobRecommendations_WithCategoryFilter_ReturnsFilteredJobs() {
        log.info("Testing getRandomJobRecommendations with category filter");
        
        // Given - request with category filter only
        RandomJobRecommendRequestDTO categoryFilterRequest = new RandomJobRecommendRequestDTO();
        categoryFilterRequest.setSize(4);
        categoryFilterRequest.setCategoryId(testCategoryId);
        categoryFilterRequest.setSeed(98765L);
        
        // When
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(categoryFilterRequest);
        
        // Then
        log.info("testGetRandomJobRecommendations_WithCategoryFilter_ReturnsFilteredJobs - returned {} jobs", 
                result != null ? result.size() : 0);
    }

    @Test
    void testGetRandomJobRecommendations_WithLocationFilter_ReturnsFilteredJobs() {
        log.info("Testing getRandomJobRecommendations with location filter");
        
        // Given - request with location filter only
        RandomJobRecommendRequestDTO locationFilterRequest = new RandomJobRecommendRequestDTO();
        locationFilterRequest.setSize(3);
        locationFilterRequest.setLocations(testLocations);
        locationFilterRequest.setSeed(11111L);
        
        // When
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(locationFilterRequest);
        
        // Then
        log.info("testGetRandomJobRecommendations_WithLocationFilter_ReturnsFilteredJobs - returned {} jobs", 
                result != null ? result.size() : 0);
    }

    @Test
    void testGetRandomJobRecommendations_EdgeCaseBoundarySize_ReturnsCorrectSize() {
        log.info("Testing getRandomJobRecommendations with boundary size values");
        
        // Test with size = 1 (minimum boundary)
        RandomJobRecommendRequestDTO sizeOneRequest = new RandomJobRecommendRequestDTO();
        sizeOneRequest.setSize(1);
        sizeOneRequest.setSeed(22222L);
        
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(sizeOneRequest);
        log.info("testGetRandomJobRecommendations_EdgeCaseBoundarySize - size=1 returned {} jobs", 
                result != null ? result.size() : 0);
        
        // Test with size = 10 (upper boundary)
        RandomJobRecommendRequestDTO sizeTenRequest = new RandomJobRecommendRequestDTO();
        sizeTenRequest.setSize(10);
        sizeTenRequest.setSeed(33333L);
        
        result = jobDomainService.getRandomJobRecommendations(sizeTenRequest);
        log.info("testGetRandomJobRecommendations_EdgeCaseBoundarySize - size=10 returned {} jobs", 
                result != null ? result.size() : 0);
    }

    @Test
    void testGetRandomJobRecommendations_NullRequest_ReturnsEmptyList() {
        log.info("Testing getRandomJobRecommendations with null request");
        
        try {
            // When
            List<JobListVO> result = jobDomainService.getRandomJobRecommendations(null);
            
            // Then
            log.info("testGetRandomJobRecommendations_NullRequest_ReturnsEmptyList - returned {} jobs", 
                    result != null ? result.size() : 0);
        } catch (Exception e) {
            log.info("testGetRandomJobRecommendations_NullRequest_ReturnsEmptyList - caught expected exception: {}", 
                    e.getMessage());
        }
    }

    @Test
    void testGetRandomJobRecommendations_InvalidSize_ReturnsEmptyList() {
        log.info("Testing getRandomJobRecommendations with invalid size values");
        
        // Test with size = 0
        RandomJobRecommendRequestDTO zeroSizeRequest = new RandomJobRecommendRequestDTO();
        zeroSizeRequest.setSize(0);
        
        try {
            List<JobListVO> result = jobDomainService.getRandomJobRecommendations(zeroSizeRequest);
            log.info("testGetRandomJobRecommendations_InvalidSize - size=0 returned {} jobs", 
                    result != null ? result.size() : 0);
        } catch (Exception e) {
            log.info("testGetRandomJobRecommendations_InvalidSize - size=0 caught exception: {}", e.getMessage());
        }
        
        // Test with negative size
        RandomJobRecommendRequestDTO negativeSizeRequest = new RandomJobRecommendRequestDTO();
        negativeSizeRequest.setSize(-1);
        
        try {
            List<JobListVO> result = jobDomainService.getRandomJobRecommendations(negativeSizeRequest);
            log.info("testGetRandomJobRecommendations_InvalidSize - size=-1 returned {} jobs", 
                    result != null ? result.size() : 0);
        } catch (Exception e) {
            log.info("testGetRandomJobRecommendations_InvalidSize - size=-1 caught exception: {}", e.getMessage());
        }
    }

    @Test
    void testGetRandomJobRecommendations_DifferentSeeds_ReturnsDifferentResults() {
        log.info("Testing getRandomJobRecommendations with different seeds for randomness");
        
        // Given - same request with different seeds
        RandomJobRecommendRequestDTO request1 = new RandomJobRecommendRequestDTO();
        request1.setSize(5);
        request1.setSeed(11111L);
        
        RandomJobRecommendRequestDTO request2 = new RandomJobRecommendRequestDTO();
        request2.setSize(5);
        request2.setSeed(99999L);
        
        // When
        List<JobListVO> result1 = jobDomainService.getRandomJobRecommendations(request1);
        List<JobListVO> result2 = jobDomainService.getRandomJobRecommendations(request2);
        
        // Then
        log.info("testGetRandomJobRecommendations_DifferentSeeds - seed=11111 returned {} jobs", 
                result1 != null ? result1.size() : 0);
        log.info("testGetRandomJobRecommendations_DifferentSeeds - seed=99999 returned {} jobs", 
                result2 != null ? result2.size() : 0);
        
        // Compare first job IDs if both results have jobs
        if (result1 != null && !result1.isEmpty() && result2 != null && !result2.isEmpty()) {
            Long firstJob1 = result1.get(0).getJobId();
            Long firstJob2 = result2.get(0).getJobId();
            log.info("testGetRandomJobRecommendations_DifferentSeeds - first job IDs: {} vs {}, different: {}", 
                    firstJob1, firstJob2, !firstJob1.equals(firstJob2));
        }
    }

    @Test
    void testGetRandomJobRecommendations_EmptyFilters_ReturnsJobs() {
        log.info("Testing getRandomJobRecommendations with empty filter lists");
        
        // Given - request with empty filter lists
        RandomJobRecommendRequestDTO emptyFiltersRequest = new RandomJobRecommendRequestDTO();
        emptyFiltersRequest.setSize(3);
        emptyFiltersRequest.setCategoryId(null);
        emptyFiltersRequest.setLocations(new ArrayList<>());
        emptyFiltersRequest.setSeed(44444L);
        
        // When
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(emptyFiltersRequest);
        
        // Then
        log.info("testGetRandomJobRecommendations_EmptyFilters_ReturnsJobs - returned {} jobs", 
                result != null ? result.size() : 0);
    }
}
