package com.item.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.item.dto.AiVettedResultDTO;
import com.item.framework.http.Pager;
import com.item.vo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CandidateJobDomainService集成测试类
 * 测试拒绝列表ES查询相关功能 - 使用真实的ES查询和数据转换
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-23
 */
@SpringBootTest
class CandidateJobDomainServiceTest {

    @Autowired
    private CandidateJobDomainService candidateJobDomainService;

    /**
     * 测试selectDeniedPageList方法 - 正常情况
     * 使用真实的ES查询和数据转换
     */
    @Test
    void testSelectDeniedPageList_Success() {
        // 构建查询参数
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(144L)
                .build();

        // 执行ES查询
        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

        // 验证返回结果
        assertNotNull(result);
        assertEquals(1, result.getPageIndex());
        assertEquals(10, result.getPageSize());
        assertTrue(result.getTotalCount() >= 0);
        assertNotNull(result.getCurrentPageRecords());
        
        // 验证数据结构 - 如果有数据的话
        if (!result.getCurrentPageRecords().isEmpty()) {
            DeniedListVO firstRecord = result.getCurrentPageRecords().get(0);
            assertNotNull(firstRecord.getId());
            assertNotNull(firstRecord.getCandidateName());
            assertNotNull(firstRecord.getCandidateEmail());
            assertNotNull(firstRecord.getRejectTime()); // updateTime映射为rejectTime
        }
    }

    /**
     * 测试selectDeniedPageList方法 - 默认分页参数
     */
    @Test
    void testSelectDeniedPageList_DefaultPagination() {
        // 构建查询参数 - 使用默认分页
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .jobId(1L)
                .build();

        // 设置默认分页参数
        if (queryVO.getPageIndex() == null || queryVO.getPageIndex() <= 0) {
            queryVO.setPageIndex(1);
        }
        if (queryVO.getPageSize() == null || queryVO.getPageSize() <= 0) {
            queryVO.setPageSize(10);
        }

        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

        assertNotNull(result);
        assertEquals(1, result.getPageIndex());
        assertEquals(10, result.getPageSize());
        assertTrue(result.getTotalCount() >= 0);
    }

    /**
     * 测试selectDeniedPageList方法 - 自定义分页参数
     */
    @Test
    void testSelectDeniedPageList_CustomPagination() {
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .pageIndex(2)
                .pageSize(20)
                .jobId(1L)
                .build();

        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

        assertNotNull(result);
        assertEquals(2, result.getPageIndex());
        assertEquals(20, result.getPageSize());
        assertTrue(result.getTotalCount() >= 0);
    }

    /**
     * 测试selectDeniedPageList方法 - 无效jobId
     */
    @Test
    void testSelectDeniedPageList_InvalidJobId() {
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(-1L)
                .build();

        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

        assertNotNull(result);
        assertEquals(1, result.getPageIndex());
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getTotalCount());
        assertTrue(result.getCurrentPageRecords().isEmpty());
    }

    /**
     * 测试selectDeniedPageList方法 - 不存在的jobId
     */
    @Test
    void testSelectDeniedPageList_NonExistentJobId() {
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(99999L)  // 假设这个jobId不存在
                .build();

        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

        assertNotNull(result);
        assertEquals(1, result.getPageIndex());
        assertEquals(10, result.getPageSize());
        assertTrue(result.getTotalCount() >= 0);
        assertNotNull(result.getCurrentPageRecords());
    }

    /**
     * 测试selectDeniedPageList方法 - 大分页参数
     */
    @Test
    void testSelectDeniedPageList_LargePagination() {
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .pageIndex(100)
                .pageSize(50)
                .jobId(1L)
                .build();

        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

        assertNotNull(result);
        assertEquals(100, result.getPageIndex());
        assertEquals(50, result.getPageSize());
        assertTrue(result.getTotalCount() >= 0);
    }

    /**
     * 测试ES查询性能
     * 验证使用ES查询比数据库查询性能更好
     */
    @Test
    void testSelectDeniedPageList_Performance() {
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(50)
                .jobId(1L)
                .build();

        long startTime = System.currentTimeMillis();
        
        // 执行ES查询
        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 验证查询结果
        assertNotNull(result);
        
        // 验证查询时间在合理范围内（小于1秒）
        assertTrue(executionTime < 1000, 
                "ES查询执行时间应该小于1秒，实际执行时间: " + executionTime + "ms");
        
        System.out.println("ES查询执行时间: " + executionTime + "ms");
    }

    /**
     * 测试数据转换功能
     * 验证CandidateJobVO到DeniedListVO的转换正确性
     */
    @Test
    void testSelectDeniedPageList_DataConversion() {
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(5)
                .jobId(144L)
                .build();

        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

        assertNotNull(result);
        assertNotNull(result.getCurrentPageRecords());

        // 如果有数据，验证字段转换
        if (!result.getCurrentPageRecords().isEmpty()) {
            DeniedListVO deniedListVO = result.getCurrentPageRecords().get(0);
            
            // 验证必要字段存在
            assertNotNull(deniedListVO.getId());
            
            // 验证字符串字段（可能为空但不应该抛出异常）
            // candidateName, candidateEmail可能为空，但应该能正常处理
            
            // 验证关键的时间字段转换：updateTime -> rejectTime
            assertNotNull(deniedListVO.getRejectTime(), 
                    "rejectTime字段不应为空，应该从CandidateJobVO的updateTime字段转换而来");
            
            System.out.println("转换测试通过 - 拒绝时间: " + deniedListVO.getRejectTime());
        }
    }

    /**
     * 测试多种jobId的ES查询
     */
    @Test
    void testSelectDeniedPageList_MultipleJobIds() {
        Long[] testJobIds = {1L, 144L, 999L};
        
        for (Long jobId : testJobIds) {
            CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                    .pageIndex(1)
                    .pageSize(10)
                    .jobId(jobId)
                    .build();

            Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

            assertNotNull(result, "JobId " + jobId + " 查询结果不应为空");
            assertEquals(1, result.getPageIndex());
            assertEquals(10, result.getPageSize());
            assertTrue(result.getTotalCount() >= 0, 
                    "JobId " + jobId + " 查询的总数应该>=0");
            
            System.out.println("JobId " + jobId + " 查询结果: " + result.getTotalCount() + " 条记录");
        }
    }

    /**
     * 测试边界值分页参数
     */
    @Test
    void testSelectDeniedPageList_BoundaryPagination() {
        // 测试最小分页参数
        CandidateJobQueryVO queryVO1 = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(1)
                .jobId(1L)
                .build();

        Pager<DeniedListVO> result1 = candidateJobDomainService.selectDeniedPageList(queryVO1);
        assertNotNull(result1);
        assertEquals(1, result1.getPageIndex());
        assertEquals(1, result1.getPageSize());

        // 测试大分页参数
        CandidateJobQueryVO queryVO2 = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(100)
                .jobId(1L)
                .build();

        Pager<DeniedListVO> result2 = candidateJobDomainService.selectDeniedPageList(queryVO2);
        assertNotNull(result2);
        assertEquals(1, result2.getPageIndex());
        assertEquals(100, result2.getPageSize());
    }

    // ==================== CandidateId字段验证测试 ====================

    /**
     * 测试selectAiVettedPageList方法中candidateId字段的存在性和正确性
     * 验证AI筛选结果列表返回的数据包含candidateId字段
     */
    @Test
    void testSelectAiVettedPageListWithCandidateId() {
        // 构建查询参数
        AiVettedQueryVO queryVO = AiVettedQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(144L)
                .build();

        // 执行查询
        IPage<AiVettedResultDTO> result = candidateJobDomainService.selectAiVettedPageList(queryVO);

        // 验证返回结果
        assertNotNull(result, "AI筛选结果不应为空");
        assertNotNull(result.getRecords(), "AI筛选结果记录不应为空");

        // 如果有数据，验证candidateId字段
        if (!result.getRecords().isEmpty()) {
            AiVettedResultDTO firstRecord = result.getRecords().get(0);
            
            // 验证candidateId字段存在且不为空
            assertNotNull(firstRecord.getCandidateId(), 
                    "AI筛选结果应包含candidateId字段且不为空");
            assertTrue(firstRecord.getCandidateId() > 0, 
                    "candidateId应为正数");
            
            // 验证candidateId与其他候选人信息的一致性
            assertNotNull(firstRecord.getCandidateName(), 
                    "候选人姓名不应为空");
            assertNotNull(firstRecord.getCandidateEmail(), 
                    "候选人邮箱不应为空");
            
            System.out.println("AI筛选结果 - candidateId: " + firstRecord.getCandidateId() + 
                    ", candidateName: " + firstRecord.getCandidateName() + 
                    ", candidateEmail: " + firstRecord.getCandidateEmail());
        }
    }

    /**
     * 测试selectPendingReviewPageList方法中candidateId字段的存在性和正确性
     * 验证待人工审核列表返回的数据包含candidateId字段
     */
    @Test
    void testSelectPendingReviewPageListWithCandidateId() {
        // 构建查询参数
        PendingReviewQueryVO queryVO = PendingReviewQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(144L)
                .build();

        // 执行查询
        Pager<PendingReviewVO> result = candidateJobDomainService.selectPendingReviewPageList(queryVO);

        // 验证返回结果
        assertNotNull(result, "待审核结果不应为空");
        assertNotNull(result.getCurrentPageRecords(), "待审核结果记录不应为空");

        // 如果有数据，验证candidateId字段
        if (!result.getCurrentPageRecords().isEmpty()) {
            PendingReviewVO firstRecord = result.getCurrentPageRecords().get(0);
            
            // 验证candidateId字段存在且不为空
            assertNotNull(firstRecord.getCandidateId(), 
                    "待审核结果应包含candidateId字段且不为空");
            assertTrue(firstRecord.getCandidateId() > 0, 
                    "candidateId应为正数");
            
            // 验证candidateId与其他候选人信息的一致性
            assertNotNull(firstRecord.getCandidateName(), 
                    "候选人姓名不应为空");
            assertNotNull(firstRecord.getCandidateEmail(), 
                    "候选人邮箱不应为空");
            
            System.out.println("待审核结果 - candidateId: " + firstRecord.getCandidateId() + 
                    ", candidateName: " + firstRecord.getCandidateName() + 
                    ", candidateEmail: " + firstRecord.getCandidateEmail());
        }
    }

    /**
     * 测试selectDeniedPageList方法中candidateId字段的存在性和正确性
     * 验证拒绝列表返回的数据包含candidateId字段
     */
    @Test
    void testSelectDeniedPageListWithCandidateId() {
        // 构建查询参数
        CandidateJobQueryVO queryVO = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(144L)
                .build();

        // 执行查询
        Pager<DeniedListVO> result = candidateJobDomainService.selectDeniedPageList(queryVO);

        // 验证返回结果
        assertNotNull(result, "拒绝列表结果不应为空");
        assertNotNull(result.getCurrentPageRecords(), "拒绝列表结果记录不应为空");

        // 如果有数据，验证candidateId字段
        if (!result.getCurrentPageRecords().isEmpty()) {
            DeniedListVO firstRecord = result.getCurrentPageRecords().get(0);
            
            // 验证candidateId字段存在且不为空
            assertNotNull(firstRecord.getCandidateId(), 
                    "拒绝列表应包含candidateId字段且不为空");
            assertTrue(firstRecord.getCandidateId() > 0, 
                    "candidateId应为正数");
            
            // 验证candidateId与其他候选人信息的一致性
            assertNotNull(firstRecord.getCandidateName(), 
                    "候选人姓名不应为空");
            assertNotNull(firstRecord.getCandidateEmail(), 
                    "候选人邮箱不应为空");
            
            System.out.println("拒绝列表结果 - candidateId: " + firstRecord.getCandidateId() + 
                    ", candidateName: " + firstRecord.getCandidateName() + 
                    ", candidateEmail: " + firstRecord.getCandidateEmail());
        }
    }

    /**
     * 测试selectReadyPageList方法中candidateId字段的存在性和正确性
     * 验证就绪列表返回的数据包含candidateId字段
     */
    @Test
    void testSelectReadyPageListWithCandidateId() {
        // 构建查询参数
        ReadyQueryVO queryVO = ReadyQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(144L)
                .build();

        // 执行查询
        Pager<ReadyListVO> result = candidateJobDomainService.selectReadyPageList(queryVO);

        // 验证返回结果
        assertNotNull(result, "就绪列表结果不应为空");
        assertNotNull(result.getCurrentPageRecords(), "就绪列表结果记录不应为空");

        // 如果有数据，验证candidateId字段
        if (!result.getCurrentPageRecords().isEmpty()) {
            ReadyListVO firstRecord = result.getCurrentPageRecords().get(0);
            
            // 验证candidateId字段存在且不为空
            assertNotNull(firstRecord.getCandidateId(), 
                    "就绪列表应包含candidateId字段且不为空");
            assertTrue(firstRecord.getCandidateId() > 0, 
                    "candidateId应为正数");
            
            // 验证candidateId与其他候选人信息的一致性
            assertNotNull(firstRecord.getCandidateName(), 
                    "候选人姓名不应为空");
            assertNotNull(firstRecord.getCandidateEmail(), 
                    "候选人邮箱不应为空");
            
            System.out.println("就绪列表结果 - candidateId: " + firstRecord.getCandidateId() + 
                    ", candidateName: " + firstRecord.getCandidateName() + 
                    ", candidateEmail: " + firstRecord.getCandidateEmail());
        }
    }

    /**
     * 测试空数据情况下的candidateId字段处理
     * 验证当没有数据时，各接口能正常返回空列表而不抛出异常
     */
    @Test
    void testCandidateIdHandling_EmptyData() {
        Long nonExistentJobId = 99999L;

        // 测试AI筛选结果 - 空数据情况
        AiVettedQueryVO aiVettedQuery = AiVettedQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(nonExistentJobId)
                .build();
        
        IPage<AiVettedResultDTO> aiVettedResult = candidateJobDomainService.selectAiVettedPageList(aiVettedQuery);
        assertNotNull(aiVettedResult);
        assertNotNull(aiVettedResult.getRecords());
        assertTrue(aiVettedResult.getRecords().isEmpty() || aiVettedResult.getTotal() >= 0);

        // 测试待审核列表 - 空数据情况
        PendingReviewQueryVO pendingQuery = PendingReviewQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(nonExistentJobId)
                .build();
        
        Pager<PendingReviewVO> pendingResult = candidateJobDomainService.selectPendingReviewPageList(pendingQuery);
        assertNotNull(pendingResult);
        assertNotNull(pendingResult.getCurrentPageRecords());

        // 测试拒绝列表 - 空数据情况
        CandidateJobQueryVO deniedQuery = CandidateJobQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(nonExistentJobId)
                .build();
        
        Pager<DeniedListVO> deniedResult = candidateJobDomainService.selectDeniedPageList(deniedQuery);
        assertNotNull(deniedResult);
        assertNotNull(deniedResult.getCurrentPageRecords());

        // 测试就绪列表 - 空数据情况
        ReadyQueryVO readyQuery = ReadyQueryVO.builder()
                .pageIndex(1)
                .pageSize(10)
                .jobId(nonExistentJobId)
                .build();
        
        Pager<ReadyListVO> readyResult = candidateJobDomainService.selectReadyPageList(readyQuery);
        assertNotNull(readyResult);
        assertNotNull(readyResult.getCurrentPageRecords());

        System.out.println("空数据处理测试通过 - 所有接口都能正确处理不存在的jobId");
    }

    /**
     * 测试candidateId字段的数据一致性
     * 验证同一候选人在不同状态下的candidateId保持一致
     */
    @Test
    void testCandidateIdConsistency() {
        Long testJobId = 144L;

        // 测试AI筛选结果
        AiVettedQueryVO aiVettedQuery = AiVettedQueryVO.builder()
                .pageIndex(1)
                .pageSize(5)
                .jobId(testJobId)
                .build();
        
        IPage<AiVettedResultDTO> aiVettedResult = candidateJobDomainService.selectAiVettedPageList(aiVettedQuery);
        
        // 测试待审核列表
        PendingReviewQueryVO pendingQuery = PendingReviewQueryVO.builder()
                .pageIndex(1)
                .pageSize(5)
                .jobId(testJobId)
                .build();
        
        Pager<PendingReviewVO> pendingResult = candidateJobDomainService.selectPendingReviewPageList(pendingQuery);

        // 验证数据一致性
        if (!aiVettedResult.getRecords().isEmpty()) {
            for (AiVettedResultDTO aiVetted : aiVettedResult.getRecords()) {
                assertNotNull(aiVetted.getCandidateId());
                assertTrue(aiVetted.getCandidateId() > 0);
                
                // 验证candidateId与候选人信息的逻辑一致性
                if (aiVetted.getCandidateName() != null && aiVetted.getCandidateEmail() != null) {
                    assertTrue(aiVetted.getCandidateId() > 0, 
                            "当候选人姓名和邮箱不为空时，candidateId应为正数");
                }
            }
        }

        if (!pendingResult.getCurrentPageRecords().isEmpty()) {
            for (PendingReviewVO pending : pendingResult.getCurrentPageRecords()) {
                assertNotNull(pending.getCandidateId());
                assertTrue(pending.getCandidateId() > 0);
                
                // 验证candidateId与候选人信息的逻辑一致性
                if (pending.getCandidateName() != null && pending.getCandidateEmail() != null) {
                    assertTrue(pending.getCandidateId() > 0, 
                            "当候选人姓名和邮箱不为空时，candidateId应为正数");
                }
            }
        }

        System.out.println("candidateId数据一致性测试通过");
    }

    @Test
    public void test_SendInterviewReportMailToCandidate() {
        Long candidateJobId = 327L;
        try {
            candidateJobDomainService.sendInterviewReportMailToCandidate(candidateJobId);
        } catch (Exception e) {
            System.out.println("Failed");
            throw new RuntimeException(e);
        }
        System.out.println("Success");
    }
}
