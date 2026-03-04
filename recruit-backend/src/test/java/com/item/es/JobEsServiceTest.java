package com.item.es;

import com.item.es.entity.JobEsEntity;
import com.item.es.impl.JobEsServiceImpl;
import com.item.framework.http.Pager;
import com.item.util.JsonUtils;
import com.item.vo.JobOptionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

/**
 * JobEsService单元测试
 * 
 * @author hua.liu
 * @since 2025-08-26
 */
@Slf4j
@SpringBootTest
class JobEsServiceTest {

    @Resource
    private JobEsServiceImpl jobEsService;

    private String companyCode;
    private String keyword;
    private int pageIndex;
    private int pageSize;

    @BeforeEach
    void setUp() {
        companyCode = "RDXX0001";
        keyword = "Java";
        pageIndex = 1;
        pageSize = 10;
    }

    @Test
    void testSearchHistoryJobsWithDeduplication_Success() throws Exception {

        // When
        Pager<JobOptionVO> result = jobEsService.searchHistoryJobsWithDeduplication(
                keyword, pageIndex, pageSize, companyCode);

        // Then
        assertNotNull(result);
        log.info(" ================= {}", result);
    }

    @Test
    void testSearchHistoryJobsWithDeduplication_EmptyKeyword() throws Exception {
        keyword = "";
        // When
        Pager<JobOptionVO> result = jobEsService.searchHistoryJobsWithDeduplication(
                keyword, pageIndex, pageSize, companyCode);

        // Then
        assertNotNull(result);
        log.info(" ================= {}", result);
    }

    @Test
    void test001(){
        List<JobEsEntity> jobIntelligenceScoreRuleByIds = jobEsService.listJobIntelligenceScoreRuleByIds(Set.of(246L, 245L));
        log.info("{}", JsonUtils.toJson(jobIntelligenceScoreRuleByIds));
    }

}
