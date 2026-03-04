package com.item.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Test for CandidateJobService batch application count functionality
 *
 * @author system
 * @since 2025-09-10
 */
@Slf4j
@SpringBootTest
class CandidateJobServiceBatchTest {

    @Autowired
    private CandidateJobService candidateJobService;

    /**
     * Test batch application count with empty set
     */
    @Test
    void testBatchApplicationCountWithEmptySet() {
        Set<Long> emptyJobIds = Collections.emptySet();
        Map<Long, Long> result = candidateJobService.batchApplicationCount(emptyJobIds);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        log.info("Empty set test passed: {}", result);
    }

    /**
     * Test batch application count with single jobId
     */
    @Test
    void testBatchApplicationCountWithSingleJobId() {
        Set<Long> singleJobId = Sets.newHashSet(1L);
        
        // Get result from batch method
        Map<Long, Long> batchResult = candidateJobService.batchApplicationCount(singleJobId);
        
        // Get result from single method
        Long singleResult = candidateJobService.applicationCount(1L);
        
        // Verify consistency
        assertNotNull(batchResult);
        assertEquals(1, batchResult.size());
        assertTrue(batchResult.containsKey(1L));
        assertEquals(singleResult, batchResult.get(1L));
        
        log.info("Single jobId test passed - batch: {}, single: {}", batchResult.get(1L), singleResult);
    }

    /**
     * Test batch application count with multiple jobIds (small batch)
     */
    @Test
    void testBatchApplicationCountConsistency() {
        Set<Long> jobIds = Sets.newHashSet(1L, 2L, 3L, 4L, 5L);
        
        // Get result from batch method
        Stopwatch batchStopwatch = Stopwatch.createStarted();
        Map<Long, Long> batchResult = candidateJobService.batchApplicationCount(jobIds);
        long batchTime = batchStopwatch.elapsed(TimeUnit.MILLISECONDS);
        
        // Get results from single method calls
        Stopwatch singleStopwatch = Stopwatch.createStarted();
        Map<Long, Long> singleResults = jobIds.stream()
                .collect(Collectors.toMap(
                        jobId -> jobId,
                        jobId -> candidateJobService.applicationCount(jobId)
                ));
        long singleTime = singleStopwatch.elapsed(TimeUnit.MILLISECONDS);
        
        // Verify consistency
        assertNotNull(batchResult);
        assertEquals(jobIds.size(), batchResult.size());
        
        for (Long jobId : jobIds) {
            assertTrue(batchResult.containsKey(jobId));
            assertEquals(singleResults.get(jobId), batchResult.get(jobId),
                    String.format("Inconsistent result for jobId %d", jobId));
        }
        
        log.info("Multiple jobIds consistency test passed");
        log.info("Batch method time: {}ms, Single methods time: {}ms", batchTime, singleTime);
        log.info("Batch results: {}", batchResult);
        log.info("Single results: {}", singleResults);
    }

    /**
     * Test batch application count with large dataset (>200 jobIds)
     * This should trigger the automatic batching functionality
     */
    @Test
    void testBatchApplicationCountWithLargeDataSet() {
        // Create 250 jobIds to test batch splitting
        Set<Long> largeJobIds = LongStream.rangeClosed(1L, 250L)
                .boxed()
                .collect(Collectors.toSet());
        
        log.info("Testing large dataset with {} jobIds", largeJobIds.size());
        
        // Get result from batch method
        Stopwatch batchStopwatch = Stopwatch.createStarted();
        Map<Long, Long> batchResult = candidateJobService.batchApplicationCount(largeJobIds);
        long batchTime = batchStopwatch.elapsed(TimeUnit.MILLISECONDS);
        
        // Verify results
        assertNotNull(batchResult);
        assertEquals(largeJobIds.size(), batchResult.size());
        
        // Verify all jobIds are included
        for (Long jobId : largeJobIds) {
            assertTrue(batchResult.containsKey(jobId),
                    String.format("Missing jobId %d in batch result", jobId));
            assertNotNull(batchResult.get(jobId));
            assertTrue(batchResult.get(jobId) >= 0L,
                    String.format("Invalid count for jobId %d: %d", jobId, batchResult.get(jobId)));
        }
        
        log.info("Large dataset test passed");
        log.info("Processed {} jobIds in {}ms", largeJobIds.size(), batchTime);
        
        // Performance verification - batch should be faster than individual calls for large datasets
        assertTrue(batchTime < 10000, // Should complete within 10 seconds
                String.format("Batch processing took too long: %dms", batchTime));
        Stopwatch batchStopwatch1 = Stopwatch.createStarted();
        Map<Long, Long> singleResults = largeJobIds.stream()
                .collect(Collectors.toMap(
                        jobId -> jobId,
                        jobId -> candidateJobService.applicationCount(jobId)
                ));

        long batchTime1 = batchStopwatch1.elapsed(TimeUnit.MILLISECONDS);
        log.info("Processed simple {} jobIds in {}ms", largeJobIds.size(), batchTime1);

        for (Long jobId : largeJobIds) {
            assertEquals(singleResults.get(jobId), batchResult.get(jobId),
                    String.format("Inconsistent result for jobId %d", jobId));
        }
    }

    /**
     * Test batch application count with non-existent jobIds
     */
    @Test
    void testBatchApplicationCountWithNonExistentJobIds() {
        // Use very large jobIds that are unlikely to exist
        Set<Long> nonExistentJobIds = Sets.newHashSet(999999L, 999998L, 999997L);
        
        Map<Long, Long> batchResult = candidateJobService.batchApplicationCount(nonExistentJobIds);
        
        // Verify results
        assertNotNull(batchResult);
        assertEquals(nonExistentJobIds.size(), batchResult.size());
        
        // All non-existent jobIds should have count = 0
        for (Long jobId : nonExistentJobIds) {
            assertTrue(batchResult.containsKey(jobId));
            assertEquals(0L, batchResult.get(jobId),
                    String.format("Non-existent jobId %d should have count 0", jobId));
        }
        
        log.info("Non-existent jobIds test passed: {}", batchResult);
    }

    /**
     * Test batch application count with mixed existing and non-existing jobIds
     */
    @Test
    void testBatchApplicationCountWithMixedJobIds() {
        Set<Long> mixedJobIds = Sets.newHashSet(1L, 2L, 999999L, 999998L);
        
        // Get batch results
        Map<Long, Long> batchResult = candidateJobService.batchApplicationCount(mixedJobIds);
        
        // Get individual results for comparison
        Map<Long, Long> singleResults = mixedJobIds.stream()
                .collect(Collectors.toMap(
                        jobId -> jobId,
                        jobId -> candidateJobService.applicationCount(jobId)
                ));
        
        // Verify consistency
        assertNotNull(batchResult);
        assertEquals(mixedJobIds.size(), batchResult.size());
        
        for (Long jobId : mixedJobIds) {
            assertTrue(batchResult.containsKey(jobId));
            assertEquals(singleResults.get(jobId), batchResult.get(jobId),
                    String.format("Inconsistent result for jobId %d", jobId));
        }
        
        log.info("Mixed jobIds test passed");
        log.info("Batch results: {}", batchResult);
        log.info("Single results: {}", singleResults);
    }

    /**
     * Performance comparison test
     */
    @Test
    void testPerformanceComparison() {
        Set<Long> jobIds = LongStream.rangeClosed(1L, 50L)
                .boxed()
                .collect(Collectors.toSet());
        
        log.info("Performance comparison with {} jobIds", jobIds.size());
        
        // Test batch method performance
        Stopwatch batchStopwatch = Stopwatch.createStarted();
        Map<Long, Long> batchResult = candidateJobService.batchApplicationCount(jobIds);
        long batchTime = batchStopwatch.elapsed(TimeUnit.MILLISECONDS);
        
        // Test individual method calls performance
        Stopwatch singleStopwatch = Stopwatch.createStarted();
        Map<Long, Long> singleResults = jobIds.stream()
                .collect(Collectors.toMap(
                        jobId -> jobId,
                        jobId -> candidateJobService.applicationCount(jobId)
                ));
        long singleTime = singleStopwatch.elapsed(TimeUnit.MILLISECONDS);
        
        // Verify results are identical
        assertEquals(batchResult, singleResults);
        
        // Log performance metrics
        log.info("Performance comparison results:");
        log.info("Batch method: {}ms", batchTime);
        log.info("Individual calls: {}ms", singleTime);
        log.info("Performance improvement: {}%", 
                singleTime > 0 ? (100.0 * (singleTime - batchTime) / singleTime) : 0);
        
        // For datasets > 10 items, batch should typically be faster
        if (jobIds.size() > 10) {
            assertTrue(batchTime <= singleTime + 50, // Allow 50ms tolerance
                    String.format("Batch method should be comparable or faster: batch=%dms, single=%dms", 
                            batchTime, singleTime));
        }
    }
}
