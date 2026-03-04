package com.item.service.impl;

import com.item.dto.job.LocationValDTO;
import com.item.dto.job.RandomJobRecommendRequestDTO;
import com.item.vo.JobListVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JobDomainServiceImpl.getRandomJobRecommendations performance test
 * 
 * @author hua.liu
 * @since 2025-09-23
 */
@Slf4j
@SpringBootTest
class JobDomainServiceImplRandomPerformanceTest {

    @Resource
    private JobDomainServiceImpl jobDomainService;

    private RandomJobRecommendRequestDTO baseRequest;
    private List<LocationValDTO> testLocations;
    private Long testCategoryIds;

    @BeforeEach
    void setUp() {
        // Setup test locations
        testLocations = new ArrayList<>();
        LocationValDTO location1 = new LocationValDTO();
        location1.setCountryId(1L);
        location1.setCityId(52L);
        location1.setStateId(3901L);
        testLocations.add(location1);

        LocationValDTO location2 = new LocationValDTO();
        location2.setCountryId(1L);
        location2.setCityId(108L);
        location2.setStateId(3871L);
        testLocations.add(location2);

        // Setup test category IDs
        testCategoryIds = 1L;

        // Setup base request
        baseRequest = new RandomJobRecommendRequestDTO();
        baseRequest.setSize(10);
        baseRequest.setCompanyCode("RDXX0001");
        baseRequest.setSeed(12345L);
        baseRequest.setCategoryId(testCategoryIds);
        baseRequest.setLocations(testLocations);
    }

    @Test
    void testPerformance_SingleRequest() {
        log.info("=== Performance Test: Single Request ===");
        
        long startTime = System.nanoTime();
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(baseRequest);
        long endTime = System.nanoTime();
        
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        log.info("Single request performance:");
        log.info("- Duration: {} ms", durationMs);
        log.info("- Results: {} jobs", result != null ? result.size() : 0);
        log.info("- Average per job: {} ms", result != null && !result.isEmpty() ? 
                (double) durationMs / result.size() : "N/A");
    }

    @Test
    void testPerformance_MultipleRequests() {
        log.info("=== Performance Test: Multiple Requests ===");
        
        int requestCount = 10;
        long totalDuration = 0;
        int totalResults = 0;
        
        for (int i = 0; i < requestCount; i++) {
            // Vary the seed for different results
            baseRequest.setSeed(12345L + i);
            
            long startTime = System.nanoTime();
            List<JobListVO> result = jobDomainService.getRandomJobRecommendations(baseRequest);
            long endTime = System.nanoTime();
            
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            totalDuration += durationMs;
            totalResults += (result != null ? result.size() : 0);
            
            log.info("Request {}: {} ms, {} results", i + 1, durationMs, 
                    result != null ? result.size() : 0);
        }
        
        log.info("Multiple requests performance summary:");
        log.info("- Total requests: {}", requestCount);
        log.info("- Total duration: {} ms", totalDuration);
        log.info("- Average per request: {} ms", (double) totalDuration / requestCount);
        log.info("- Total results: {} jobs", totalResults);
        log.info("- Average results per request: {}", (double) totalResults / requestCount);
    }

    @Test
    void testPerformance_DifferentSizes() {
        log.info("=== Performance Test: Different Sizes ===");
        
        int[] sizes = {1, 5, 10, 20, 50};
        
        for (int size : sizes) {
            baseRequest.setSize(size);
            
            long startTime = System.nanoTime();
            List<JobListVO> result = jobDomainService.getRandomJobRecommendations(baseRequest);
            long endTime = System.nanoTime();
            
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            
            log.info("Size {} performance:", size);
            log.info("- Duration: {} ms", durationMs);
            log.info("- Results: {} jobs", result != null ? result.size() : 0);
            log.info("- Time per job: {} ms", result != null && !result.isEmpty() ? 
                    (double) durationMs / result.size() : "N/A");
        }
    }

    @Test
    void testPerformance_WithAndWithoutFilters() {
        log.info("=== Performance Test: Filter Impact ===");
        
        // Test 1: No filters
        RandomJobRecommendRequestDTO noFiltersRequest = new RandomJobRecommendRequestDTO();
        noFiltersRequest.setSize(10);
        noFiltersRequest.setSeed(12345L);
        
        long startTime = System.nanoTime();
        List<JobListVO> result = jobDomainService.getRandomJobRecommendations(noFiltersRequest);
        long endTime = System.nanoTime();
        long noFilterDuration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        log.info("No filters:");
        log.info("- Duration: {} ms", noFilterDuration);
        log.info("- Results: {} jobs", result != null ? result.size() : 0);
        
        // Test 2: With all filters
        startTime = System.nanoTime();
        result = jobDomainService.getRandomJobRecommendations(baseRequest);
        endTime = System.nanoTime();
        long allFiltersDuration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        log.info("With all filters (company + category + location):");
        log.info("- Duration: {} ms", allFiltersDuration);
        log.info("- Results: {} jobs", result != null ? result.size() : 0);
        
        // Performance comparison
        double overhead = ((double) allFiltersDuration - noFilterDuration) / noFilterDuration * 100;
        log.info("Filter overhead: {}%", overhead);
    }

    @Test
    void testPerformance_MemoryUsage() {
        log.info("=== Performance Test: Memory Usage ===");
        
        // Force GC before test
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Execute multiple requests
        for (int i = 0; i < 20; i++) {
            baseRequest.setSeed(12345L + i);
            List<JobListVO> result = jobDomainService.getRandomJobRecommendations(baseRequest);
            // Simulate processing results
            if (result != null) {
                result.forEach(job -> {
                    // Access some fields to ensure objects are fully loaded
                    String title = job.getTitle();
                    Long jobId = job.getJobId();
                });
            }
        }
        
        // Check memory after
        System.gc(); // Suggest GC to clean up
        Thread.yield(); // Give GC a chance
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        
        long memoryUsed = afterMemory - beforeMemory;
        log.info("Memory usage test:");
        log.info("- Before: {} bytes ({} MB)", beforeMemory, beforeMemory / 1024 / 1024);
        log.info("- After: {} bytes ({} MB)", afterMemory, afterMemory / 1024 / 1024);
        log.info("- Used: {} bytes ({} MB)", memoryUsed, memoryUsed / 1024 / 1024);
        log.info("- Per request: {} bytes", memoryUsed / 20);
    }
}
