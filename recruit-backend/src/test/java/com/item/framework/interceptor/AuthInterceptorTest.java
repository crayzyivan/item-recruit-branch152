package com.item.framework.interceptor;

import com.item.dto.iam.IamUserContextDTO;
import com.item.framework.constant.RoleType;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.util.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * AuthInterceptor 单元测试
 * 测试子招聘者角色的权限验证逻辑
 *
 * @author Test
 * @since 2025-09-11
 */
@Slf4j
@SpringBootTest
class AuthInterceptorTest {

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor();
        log.debug("AuthInterceptor test setup completed");
    }

    @AfterEach
    void tearDown() {
        UserContextUtil.clear();
        log.debug("User context cleared after test");
    }

    /**
     * 创建模拟的用户上下文
     */
    private IamUserContextDTO createMockUser(UserIdentifyTypeEnum userIdentifyType) {
        IamUserContextDTO context = new IamUserContextDTO();
        context.setId("1952979922931400707");
        context.setCompanyCode("TEST_COMPANY");
        context.setUserName("Test User");
        context.setEmail("test@example.com");
        context.setUserIdentifyCode(userIdentifyType.getCode());
        
        // 根据用户身份类型设置相应的userType和primaryUser
        switch (userIdentifyType) {
            case CANDIDATE:
                context.setUserType(2);
                context.setPrimaryUser(null);
                break;
            case MASTER_RECRUIT:
                context.setUserType(1);
                context.setPrimaryUser(true);
                break;
            case SUB_RECRUIT:
                context.setUserType(1);
                context.setPrimaryUser(false);
                break;
        }
        
        return context;
    }

    /**
     * 使用反射调用私有的verifyPermission方法
     */
    private boolean callVerifyPermission(RoleType roleType) throws Exception {
        Method verifyPermissionMethod = AuthInterceptor.class.getDeclaredMethod("verifyPermission", RoleType.class);
        verifyPermissionMethod.setAccessible(true);
        return (Boolean) verifyPermissionMethod.invoke(authInterceptor, roleType);
    }

    // ==================== NONE 权限测试 ====================

    @Test
    void testVerifyPermission_NoneRole_ShouldAlwaysReturnTrue() throws Exception {
        // Given - 不设置任何用户上下文
        
        // When
        boolean result = callVerifyPermission(RoleType.NONE);
        
        // Then
        assertTrue(result);
        log.info("NONE role test - No user context required, result: {}", result);
    }

    // ==================== CANDIDATE 权限测试 ====================

    @Test
    void testVerifyPermission_CandidateUser_CanAccessCandidateRole() throws Exception {
        // Given
        IamUserContextDTO candidateUser = createMockUser(UserIdentifyTypeEnum.CANDIDATE);
        UserContextUtil.setCurrentUser(candidateUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.CANDIDATE);
        
        // Then
        assertTrue(result);
        log.info("Candidate user accessing CANDIDATE role - Expected: true, Actual: {}", result);
    }

    @Test
    void testVerifyPermission_CandidateUser_CannotAccessMasterRole() throws Exception {
        // Given
        IamUserContextDTO candidateUser = createMockUser(UserIdentifyTypeEnum.CANDIDATE);
        UserContextUtil.setCurrentUser(candidateUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertFalse(result);
        log.info("Candidate user accessing MASTER_USER role - Expected: false, Actual: {}", result);
    }

    @Test
    void testVerifyPermission_CandidateUser_CannotAccessSubRole() throws Exception {
        // Given
        IamUserContextDTO candidateUser = createMockUser(UserIdentifyTypeEnum.CANDIDATE);
        UserContextUtil.setCurrentUser(candidateUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.SUB_USER);
        
        // Then
        assertFalse(result);
        log.info("Candidate user accessing SUB_USER role - Expected: false, Actual: {}", result);
    }

    // ==================== MASTER_RECRUIT 权限测试 ====================

    @Test
    void testVerifyPermission_MasterRecruitUser_CanAccessMasterRole() throws Exception {
        // Given
        IamUserContextDTO masterUser = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        UserContextUtil.setCurrentUser(masterUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertTrue(result);
        log.info("Master recruit user accessing MASTER_USER role - Expected: true, Actual: {}", result);
    }

    @Test
    void testVerifyPermission_MasterRecruitUser_CanAccessSubRole() throws Exception {
        // Given
        IamUserContextDTO masterUser = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        UserContextUtil.setCurrentUser(masterUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.SUB_USER);
        
        // Then
        assertTrue(result);
        log.info("Master recruit user accessing SUB_USER role - Expected: true, Actual: {}", result);
    }

    @Test
    void testVerifyPermission_MasterRecruitUser_CannotAccessCandidateRole() throws Exception {
        // Given
        IamUserContextDTO masterUser = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        UserContextUtil.setCurrentUser(masterUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.CANDIDATE);
        
        // Then
        assertFalse(result);
        log.info("Master recruit user accessing CANDIDATE role - Expected: false, Actual: {}", result);
    }

    // ==================== SUB_RECRUIT 权限测试 (新功能) ====================

    @Test
    void testVerifyPermission_SubRecruitUser_CanAccessSubRole() throws Exception {
        // Given
        IamUserContextDTO subUser = createMockUser(UserIdentifyTypeEnum.SUB_RECRUIT);
        UserContextUtil.setCurrentUser(subUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.SUB_USER);
        
        // Then
        assertTrue(result);
        log.info("Sub recruit user accessing SUB_USER role - Expected: true, Actual: {}", result);
    }

    @Test
    void testVerifyPermission_SubRecruitUser_CannotAccessMasterRole() throws Exception {
        // Given
        IamUserContextDTO subUser = createMockUser(UserIdentifyTypeEnum.SUB_RECRUIT);
        UserContextUtil.setCurrentUser(subUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertFalse(result);
        log.info("Sub recruit user accessing MASTER_USER role - Expected: false, Actual: {}", result);
    }

    @Test
    void testVerifyPermission_SubRecruitUser_CannotAccessCandidateRole() throws Exception {
        // Given
        IamUserContextDTO subUser = createMockUser(UserIdentifyTypeEnum.SUB_RECRUIT);
        UserContextUtil.setCurrentUser(subUser);
        
        // When
        boolean result = callVerifyPermission(RoleType.CANDIDATE);
        
        // Then
        assertFalse(result);
        log.info("Sub recruit user accessing CANDIDATE role - Expected: false, Actual: {}", result);
    }

    // ==================== 异常情况测试 ====================

    @Test
    void testVerifyPermission_NoUserContext_ShouldReturnFalse() throws Exception {
        // Given - 没有设置用户上下文
        
        // When
        boolean result = callVerifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertFalse(result);
        log.info("No user context test - Expected: false, Actual: {}", result);
    }

    @Test
    void testVerifyPermission_NullUserIdentifyCode_ShouldReturnFalse() throws Exception {
        // Given
        IamUserContextDTO user = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        user.setUserIdentifyCode(null);
        UserContextUtil.setCurrentUser(user);
        
        // When
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            callVerifyPermission(RoleType.MASTER_USER);
        });

        // Then
//        assertEquals(CommonResponseCode.COMMON_USER_IDENTIFY_NOT_INVALID.getCode(), exception.getCode());
        log.info("Null user identify code test - Expected: false, Actual: ", exception);
    }

    // ==================== 综合权限矩阵测试 ====================

    @Test
    void testPermissionMatrix_CompleteVerification() throws Exception {
        log.info("=== Permission Matrix Test ===");
        
        // Test all combinations
        UserIdentifyTypeEnum[] userTypes = {UserIdentifyTypeEnum.CANDIDATE, UserIdentifyTypeEnum.MASTER_RECRUIT, UserIdentifyTypeEnum.SUB_RECRUIT};
        RoleType[] roleTypes = {RoleType.CANDIDATE, RoleType.MASTER_USER, RoleType.SUB_USER};
        
        for (UserIdentifyTypeEnum userType : userTypes) {
            IamUserContextDTO user = createMockUser(userType);
            UserContextUtil.setCurrentUser(user);
            
            for (RoleType roleType : roleTypes) {
                boolean result = callVerifyPermission(roleType);
                boolean expected = isExpectedPermission(userType, roleType);
                
                if (result == expected) {
                    log.info("✓ {} accessing {} - Expected: {}, Actual: {}", userType, roleType, expected, result);
                } else {
                    log.error("✗ {} accessing {} - Expected: {}, Actual: {}", userType, roleType, expected, result);
                }
                
                // 断言验证
                if (expected) {
                    assertTrue(result, String.format("%s should be able to access %s", userType, roleType));
                } else {
                    assertFalse(result, String.format("%s should NOT be able to access %s", userType, roleType));
                }
            }
            
            UserContextUtil.clear();
        }
    }

    /**
     * 根据用户类型和角色类型判断期望的权限结果
     */
    private boolean isExpectedPermission(UserIdentifyTypeEnum userType, RoleType roleType) {
        switch (userType) {
            case CANDIDATE:
                return roleType == RoleType.CANDIDATE;
            case MASTER_RECRUIT:
                return roleType == RoleType.MASTER_USER || roleType == RoleType.SUB_USER;
            case SUB_RECRUIT:
                return roleType == RoleType.SUB_USER;
            default:
                return false;
        }
    }
}
