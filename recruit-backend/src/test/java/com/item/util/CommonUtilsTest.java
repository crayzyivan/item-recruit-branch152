package com.item.util;

import com.item.dto.iam.ExternalInfoDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.framework.error.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * CommonUtils单元测试
 *
 * @author lh
 * @since 2025-08-28
 */
@Slf4j
@SpringBootTest
class CommonUtilsTest {

    @Resource
    private RecruitCommonNacosConfig recruitCommonNacosConfig;

    // ==================== 用户场景测试 (RP-314) ====================

    @Test
    void testGetUserIdentify_CEndUser_PrimaryUserEnableDisabled() {
        // Given: candidatePrimaryUserEnable=false时，无论primaryUser是什么都返回CANDIDATE
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("MKT");
        context.setPrimaryUser(true); // 即使为true也应该返回CANDIDATE

        RecruitCommonNacosConfig.CandidateUserIdentify candidateUserIdentify = new RecruitCommonNacosConfig.CandidateUserIdentify();
        candidateUserIdentify.setCandidatePrimaryUserEnable(false);
        candidateUserIdentify.setCandidateCompanyCodes(Set.of("MKT"));

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, candidateUserIdentify);

        // Then
        assertEquals(UserIdentifyTypeEnum.CANDIDATE, result);
        log.info("C-end user with primaryUserEnable=false test - Expected: CANDIDATE, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_CEndUser_PrimaryUserFalse() {
        // Given: candidatePrimaryUserEnable=true且primaryUser=false时返回CANDIDATE
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("MKT");
        context.setPrimaryUser(false);

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.CANDIDATE, result);
        log.info("C-end user with primaryUser=false test - Expected: CANDIDATE, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_CEndUser_PrimaryUserTrue() {
        // Given: candidatePrimaryUserEnable=true且primaryUser=true时返回MASTER_RECRUIT
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("MKT");
        context.setPrimaryUser(true);

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.MASTER_RECRUIT, result);
        log.info("C-end user with primaryUser=true test - Expected: MASTER_RECRUIT, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_CEndUser_PrimaryUserNull() {
        // Given: candidatePrimaryUserEnable=true且primaryUser=null时返回CANDIDATE
        // 实际实现：只有primaryUser明确为true才返回MASTER_RECRUIT
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("MKT");
        context.setPrimaryUser(null);

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.CANDIDATE, result);
        log.info("C-end user with primaryUser=null test - Expected: CANDIDATE, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_CEndUser_CompanyCodeNull() {
        // Given: companyCode为null时的边界情况
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode(null);
        context.setPrimaryUser(true);

        // When & Then: companyCode为null时应该抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());
        });
        
        assertEquals(CommonResponseCode.CURRENT_USER_INFO_EXCEPTION.getCode(), exception.getCode());
        log.info("C-end user with companyCode=null test - Exception correctly thrown: {}", exception.getMessage());
    }

    @Test
    void testGetUserIdentify_CEndUser_EmptyCandidateCompanyCodes() {
        // Given: candidateCompanyCodes为空集合
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("MKT");
        context.setPrimaryUser(false);

        RecruitCommonNacosConfig.CandidateUserIdentify candidateUserIdentify = new RecruitCommonNacosConfig.CandidateUserIdentify();
        candidateUserIdentify.setCandidateCompanyCodes(null);
        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, candidateUserIdentify);

        // Then: 不匹配C端逻辑，走B端逻辑，primaryUser=false返回SUB_RECRUIT
        assertEquals(UserIdentifyTypeEnum.SUB_RECRUIT, result);
        log.info("C-end user with empty candidateCompanyCodes test - Expected: SUB_RECRUIT (B-end logic), Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_CEndUser_NullCandidateUserIdentify() {
        // Given: candidateUserIdentify为null
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("MKT");
        context.setPrimaryUser(true);

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, null);

        // Then: candidateUserIdentify为null，走B端逻辑，primaryUser=true返回MASTER_RECRUIT
        assertEquals(UserIdentifyTypeEnum.MASTER_RECRUIT, result);
        log.info("C-end user with null candidateUserIdentify test - Expected: MASTER_RECRUIT (B-end logic), Actual: {}", result);
    }

    // ==================== 新增：B端用户场景测试 (RP-314) ====================

    @Test
    void testGetUserIdentify_BEndUser_PrimaryUserTrue() {
        // Given: B端用户，primaryUser=true时返回MASTER_RECRUIT
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("COMPANY_B");
        context.setPrimaryUser(true);


        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.MASTER_RECRUIT, result);
        log.info("B-end user with primaryUser=true test - Expected: MASTER_RECRUIT, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_BEndUser_RecruitAccountTypePrimary() {
        // Given: B端用户，recruitAccountType=PRIMARY时返回MASTER_RECRUIT
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("COMPANY_B");
        context.setPrimaryUser(false);

        ExternalInfoDTO externalInfo = new ExternalInfoDTO();
        externalInfo.setRecruitAccountType("primary");
        context.setExternalInfo(externalInfo);

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.MASTER_RECRUIT, result);
        log.info("B-end user with recruitAccountType=PRIMARY test - Expected: MASTER_RECRUIT, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_BEndUser_BothConditionsTrue() {
        // Given: B端用户，OR逻辑：primaryUser=true且recruitAccountType=PRIMARY都满足
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("COMPANY_B");
        context.setPrimaryUser(true);

        ExternalInfoDTO externalInfo = new ExternalInfoDTO();
        externalInfo.setRecruitAccountType("primary");
        context.setExternalInfo(externalInfo);


        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.MASTER_RECRUIT, result);
        log.info("B-end user with both conditions true test - Expected: MASTER_RECRUIT, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_BEndUser_SubRecruit() {
        // Given: B端用户，primaryUser=false且recruitAccountType=undefined时返回SUB_RECRUIT
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("COMPANY_B");
        context.setPrimaryUser(false);

        ExternalInfoDTO externalInfo = new ExternalInfoDTO();
        externalInfo.setRecruitAccountType("undefined");
        context.setExternalInfo(externalInfo);

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.SUB_RECRUIT, result);
        log.info("B-end user with neither condition true test - Expected: SUB_RECRUIT, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_BEndUser_PrimaryUserNull_SubRecruit() {
        // Given: B端用户，primaryUser=null且recruitAccountType=undefined时返回SUB_RECRUIT
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("COMPANY_B");
        context.setPrimaryUser(null);

        ExternalInfoDTO externalInfo = new ExternalInfoDTO();
        externalInfo.setRecruitAccountType("undefined");
        context.setExternalInfo(externalInfo);

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.SUB_RECRUIT, result);
        log.info("B-end user with primaryUser=null and recruitAccountType=undefined test - Expected: SUB_RECRUIT, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_BEndUser_PrimaryUserNull_RecruitAccountTypePrimary() {
        // Given: B端用户，primaryUser=null但recruitAccountType=PRIMARY时返回MASTER_RECRUIT
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("COMPANY_B");
        context.setPrimaryUser(null);

        ExternalInfoDTO externalInfo = new ExternalInfoDTO();
        externalInfo.setRecruitAccountType("primary");
        context.setExternalInfo(externalInfo);

        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.MASTER_RECRUIT, result);
        log.info("B-end user with primaryUser=null but recruitAccountType=PRIMARY test - Expected: MASTER_RECRUIT, Actual: {}", result);
    }

    @Test
    void testGetUserIdentify_BEndUser_ExternalInfoNull() {
        // Given: B端用户，externalInfo=null且primaryUser=false时返回SUB_RECRUIT
        IamUserContextDTO context = new IamUserContextDTO();
        context.setUserType(0);
        context.setCompanyCode("COMPANY_B");
        context.setPrimaryUser(false);
        context.setExternalInfo(null);
        // When
        UserIdentifyTypeEnum result = CommonUtils.getUserIdentify(context, recruitCommonNacosConfig.getCandidateUserIdentify());

        // Then
        assertEquals(UserIdentifyTypeEnum.SUB_RECRUIT, result);
        log.info("B-end user with externalInfo=null test - Expected: SUB_RECRUIT, Actual: {}", result);
    }

    @Test
    void test001(){
        String candidateAnswerQuestion5sRoute = recruitCommonNacosConfig.getCandidateAnswerQuestion5sRoute();
        log.info("candidateAnswerQuestion5sRoute={}", candidateAnswerQuestion5sRoute);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String nowStr = now.toEpochSecond(ZoneOffset.UTC) + "";
        // 2. 秒级时间戳 转 LocalDateTime
        LocalDateTime localDateTime = Instant.ofEpochSecond(Long.parseLong(nowStr)).atZone(ZoneOffset.UTC).toLocalDateTime();
        log.info("candidateAnswerQuestion5sRoute {} {} {}", now, nowStr, localDateTime);
    }
}
