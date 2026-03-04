package com.item.service.impl;

import com.item.service.CompanyDomainService;
import com.item.vo.CompanyInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * @author : lh
 * Company Domain Service Performance Test
 */
@Slf4j
@SpringBootTest
class CompanyDomainServiceTest {
    
    @Resource
    private CompanyDomainService companyDomainService;
    
    private static final String TEST_COMPANY_CODE = "RDXX0001";
    private static final int WARMUP_ITERATIONS = 3;
    
    @Test
    void getCompanyInfoVO() {
        CompanyInfoVO result = companyDomainService.getCompanyInfoByCode(TEST_COMPANY_CODE);
        assertNotNull(result);
        log.info("Basic test completed, result: {}", result);
    }
    
    /**
     * Warmup method to prepare JVM for performance testing
     */
    private void warmup() {
        log.info("Starting warmup with {} iterations", WARMUP_ITERATIONS);
        long startTime = System.nanoTime();
        
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            companyDomainService.getCompanyInfoByCode(TEST_COMPANY_CODE);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        log.info("Warmup completed in {} ms", TimeUnit.NANOSECONDS.toMillis(duration));
    }
    
    /**
     * Performance test helper method
     */
    private void performanceTest(int iterations, String testName) {
        log.info("Starting {} - {} iterations", testName, iterations);
        
        // Warmup before each test
        warmup();
        
        // Force garbage collection
        System.gc();
        
        long startTime = System.nanoTime();
        long totalResponseTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        
        for (int i = 0; i < iterations; i++) {
            long iterationStart = System.nanoTime();
            
            CompanyInfoVO result = companyDomainService.getCompanyInfoByCode(TEST_COMPANY_CODE);
            assertNotNull(result, "Result should not be null at iteration " + (i + 1));
            
            long iterationEnd = System.nanoTime();
            long iterationTime = iterationEnd - iterationStart;
            
            totalResponseTime += iterationTime;
            minTime = Math.min(minTime, iterationTime);
            maxTime = Math.max(maxTime, iterationTime);
        }
        
        long endTime = System.nanoTime();
        long totalDuration = endTime - startTime;
        
        // Calculate statistics
        double averageTime = (double) totalResponseTime / iterations;
        double totalTimeMs = TimeUnit.NANOSECONDS.toMillis(totalDuration);
        double averageTimeMs = TimeUnit.NANOSECONDS.toMillis((long) averageTime);
        double minTimeMs = TimeUnit.NANOSECONDS.toMillis(minTime);
        double maxTimeMs = TimeUnit.NANOSECONDS.toMillis(maxTime);
        double throughput = (double) iterations / (totalTimeMs / 1000.0);
        
        // Log performance results
        log.info("=== {} Performance Results ===", testName);
        log.info("Total iterations: {}", iterations);
        log.info("Total time: {} ms", totalTimeMs);
        log.info("Average response time: {} ms", averageTimeMs);
        log.info("Min response time: {} ms", minTimeMs);
        log.info("Max response time: {} ms", maxTimeMs);
        log.info("Throughput: {} requests/second", throughput);
        log.info("=== End {} Results ===\n", testName);
    }
    
    @Test
    void performanceTest10Calls() {
        performanceTest(10, "10 Calls Performance Test");
    }
    
    @Test
    void performanceTest100Calls() {
        performanceTest(100, "100 Calls Performance Test");
    }
    
    @Test
    void performanceTest300Calls() {
        performanceTest(300, "300 Calls Performance Test");
    }
    
    @Test
    void performanceTest600Calls() {
        performanceTest(600, "600 Calls Performance Test");
    }
    
    @Test
    void performanceTest1000Calls() {
        performanceTest(1000, "1000 Calls Performance Test");
    }
    
    /**
     * Comprehensive performance test that runs all scenarios
     */
    @Test
    void comprehensivePerformanceTest() throws InterruptedException {
        log.info("Starting comprehensive performance test suite");
        
        int[] testCases = {10, 100, 300, 600, 1000};
        
        for (int testCase : testCases) {
            performanceTest(testCase, "Comprehensive Test - " + testCase + " Calls");
            TimeUnit.SECONDS.sleep(5L);
        }
        
        log.info("Comprehensive performance test suite completed");
    }
}
