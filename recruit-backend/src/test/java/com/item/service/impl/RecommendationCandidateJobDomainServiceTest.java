package com.item.service.impl;

import com.item.dto.iam.IamUserContextDTO;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.service.RecommendationCandidateJobDomainService;
import com.item.util.UserContextUtil;
import com.item.vo.RecommendJobEmailRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recommendation candidate job domain service test
 * Tests business logic for sending job recommendation emails
 *
 * @author hua.liu
 * @since 2025-10-23
 */
@Slf4j
@SpringBootTest
@Transactional
class RecommendationCandidateJobDomainServiceTest extends BaseServiceTestWithUserContext {

    @Autowired
    private RecommendationCandidateJobDomainService recommendationDomainService;

    private RecommendJobEmailRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        // Setup test request
        testRequest = new RecommendJobEmailRequestDTO();
        testRequest.setJobId(202L);
        testRequest.setCandidateId(5296L);
        testRequest.setRecommendReasons("Great fit for this position");
        IamUserContextDTO userContextDTO = new IamUserContextDTO();
        userContextDTO.setId("1952979922931400707");
        userContextDTO.setCompanyCode("RDXX0001");
        userContextDTO.setUserIdentifyCode(UserIdentifyTypeEnum.SUB_RECRUIT.getCode());
        UserContextUtil.setCurrentUser(userContextDTO);
    }

    @Test
    void testSendRecommendationEmail_Success() {

        // When: Send recommendation email
        Boolean result = recommendationDomainService.sendRecommendationEmail(testRequest);

        // Then: Verify success
        assertTrue(result);

        log.info("Test passed: sendRecommendationEmail_Success");
    }

    @AfterEach
    void after() {
        UserContextUtil.clear();
    }
}

