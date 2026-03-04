package com.item.service.impl;

import com.item.dto.iam.IamUserContextDTO;
import com.item.util.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
//@ActiveProfiles("test")
public abstract class BaseServiceTestWithUserContextAyrShare {

    /**
     * 默认的测试公司代码
     */
    protected static final String DEFAULT_COMPANY_CODE = "RDXX0001";

    /**
     * 默认的测试用户ID
     */
    protected static final String DEFAULT_USER_ID = "1952979922931400707";

    /**
     * 在每个测试方法执行前设置用户上下文
     */
    @BeforeEach
    void setUpUserContext() {
        IamUserContextDTO mockUser = createDefaultMockUser();
        UserContextUtil.setCurrentUser(mockUser);
        log.debug("测试用户上下文已设置: companyCode={}, userId={}",
                mockUser.getCompanyCode(), mockUser.getId());
    }

    /**
     * 在每个测试方法执行后清理用户上下文
     */
    @AfterEach
    void tearDownUserContext() {
        UserContextUtil.clear();
        log.debug("测试用户上下文已清理");
    }

    /**
     * 创建默认的模拟用户
     */
    protected IamUserContextDTO createDefaultMockUser() {
        return createMockRecruiterUser(DEFAULT_COMPANY_CODE, DEFAULT_USER_ID);
    }

    /**
     * 创建指定公司代码的模拟招聘者用户
     */
    protected IamUserContextDTO createMockRecruiterUser(String companyCode, String userId) {
        IamUserContextDTO user = new IamUserContextDTO();
        user.setId(userId);
        user.setAccountId("account" + userId);
        user.setCompanyCode(companyCode);
        user.setEmail("test.recruiter@junit.test.com");
        user.setUserName("Test Recruiter");
        user.setFirstName("Test");
        user.setLastName("Recruiter");
        user.setUserStatus("ACTIVE");
        user.setUserType(0); // 招聘者类型
        user.setPrimaryUser(true); // 主账号
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setOrigin("TEST");

        return user;
    }
//
//    /**
//     * 创建模拟的应聘者用户
//     */
//    protected IamUserContextDTO createMockCandidateUser(String userId) {
//        IamUserContextDTO user = new IamUserContextDTO();
//        user.setId(userId);
//        user.setAccountId("candidate" + userId);
//        user.setEmail("test.candidate@example.com");
//        user.setUserName("Test Candidate");
//        user.setFirstName("Test");
//        user.setLastName("Candidate");
//        user.setUserStatus("ACTIVE");
//        user.setUserType(2); // 应聘者类型
//        user.setPrimaryUser(false); // 非主账号
//        user.setCreatedAt(LocalDateTime.now());
//        user.setUpdatedAt(LocalDateTime.now());
//        user.setOrigin("TEST");
//        user.setCandidateOneselfId(Long.parseLong(userId));
//
//        return user;
//    }
//
//    /**
//     * 切换到指定的用户上下文
//     */
//    protected void switchToUser(IamUserContextDTO user) {
//        UserContextUtil.setCurrentUser(user);
//        log.debug("已切换到用户: companyCode={}, userId={}",
//                user.getCompanyCode(), user.getId());
//    }
//
//    /**
//     * 切换到指定公司的招聘者
//     */
//    protected void switchToRecruiter(String companyCode, String userId) {
//        IamUserContextDTO recruiter = createMockRecruiterUser(companyCode, userId);
//        switchToUser(recruiter);
//    }
//
//    /**
//     * 切换到应聘者
//     */
//    protected void switchToCandidate(String userId) {
//        IamUserContextDTO candidate = createMockCandidateUser(userId);
//        switchToUser(candidate);
//    }
}
