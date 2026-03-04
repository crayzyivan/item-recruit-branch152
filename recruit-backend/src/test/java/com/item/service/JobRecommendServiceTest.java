package com.item.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * TODO：功能描述
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-23  14:24
 */
@SpringBootTest
public class JobRecommendServiceTest {

    @Resource
    private JobRecommendService jobRecommendService;

    @Test
    public void test(){
        jobRecommendService.recommendCandidates(145L);////132L
    }

}