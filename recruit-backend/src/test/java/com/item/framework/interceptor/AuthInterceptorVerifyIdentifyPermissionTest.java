package com.item.framework.interceptor;

import com.item.dto.AuthCheckResultDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.framework.constant.AuthResponseCode;
import com.item.framework.constant.RoleType;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.util.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthInterceptor verifyIdentifyPermission 方法单元测试
 * 测试新增的权限验证方法，返回详细的认证结果
 *
 * @author Test
 * @since 2025-09-15
 */
@Slf4j
@SpringBootTest
class AuthInterceptorVerifyIdentifyPermissionTest {

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor();
        log.debug("AuthInterceptor verifyIdentifyPermission test setup completed");
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
        context.setAccountId("account123");
        context.setCompanyCode("TEST_COMPANY");
        context.setContactNumber("1234567890");
        context.setEmail("test@example.com");
        context.setFirstName("Test");
        context.setLastName("User");
        context.setUserName("Test User");
        context.setUserStatus("ACTIVE");
        context.setUserIdentifyCode(userIdentifyType.getCode());
        context.setCreatedAt(LocalDateTime.now());
        context.setUpdatedAt(LocalDateTime.now());
        context.setOrigin("TEST");
        
        // 根据用户身份类型设置相应的userType和primaryUser
        switch (userIdentifyType) {
            case CANDIDATE:
                context.setUserType(2);
                context.setPrimaryUser(null);
                context.setCandidateOneselfId(Long.parseLong(context.getId()));
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
     * 使用反射调用私有的verifyIdentifyPermission方法
     */
    private AuthCheckResultDTO callVerifyIdentifyPermission(RoleType roleType) throws Exception {
        Method verifyIdentifyPermissionMethod = AuthInterceptor.class.getDeclaredMethod("verifyIdentifyPermission", RoleType.class);
        verifyIdentifyPermissionMethod.setAccessible(true);
        return (AuthCheckResultDTO) verifyIdentifyPermissionMethod.invoke(authInterceptor, roleType);
    }

    // ==================== NONE 权限测试 ====================

    @Test
    void testVerifyIdentifyPermission_NoneRole_ShouldReturnSuccess() throws Exception {
        // Given - 不设置任何用户上下文
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.NONE);
        
        // Then
        assertTrue(result.authSuccess());
        assertNull(result.code());
        assertNull(result.msg());
        log.info("NONE role test - No user context required, result: {}", result);
    }

    // ==================== CANDIDATE 权限测试 ====================

    @Test
    void testVerifyIdentifyPermission_CandidateUser_CanAccessCandidateRole() throws Exception {
        // Given
        IamUserContextDTO candidateUser = createMockUser(UserIdentifyTypeEnum.CANDIDATE);
        UserContextUtil.setCurrentUser(candidateUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.CANDIDATE);
        
        // Then
        assertTrue(result.authSuccess());
        assertNull(result.code());
        assertNull(result.msg());
        log.info("Candidate user accessing CANDIDATE role - Expected: success, Actual: {}", result);
    }

    @Test
    void testVerifyIdentifyPermission_CandidateUser_CannotAccessMasterRole() throws Exception {
        // Given
        IamUserContextDTO candidateUser = createMockUser(UserIdentifyTypeEnum.CANDIDATE);
        UserContextUtil.setCurrentUser(candidateUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertFalse(result.authSuccess());
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH.getCode(), result.code());
        assertTrue(result.msg().contains("Recruit"));
        log.info("Candidate user accessing MASTER_USER role - Expected: identity not match, Actual: {}", result);
    }

    @Test
    void testVerifyIdentifyPermission_CandidateUser_CannotAccessSubRole() throws Exception {
        // Given
        IamUserContextDTO candidateUser = createMockUser(UserIdentifyTypeEnum.CANDIDATE);
        UserContextUtil.setCurrentUser(candidateUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.SUB_USER);
        
        // Then
        assertFalse(result.authSuccess());
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH.getCode(), result.code());
        assertTrue(result.msg().contains("Recruit"));
        log.info("Candidate user accessing SUB_USER role - Expected: identity not match, Actual: {}", result);
    }

    // ==================== MASTER_RECRUIT 权限测试 ====================

    @Test
    void testVerifyIdentifyPermission_MasterRecruitUser_CanAccessMasterRole() throws Exception {
        // Given
        IamUserContextDTO masterUser = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        UserContextUtil.setCurrentUser(masterUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertTrue(result.authSuccess());
        assertNull(result.code());
        assertNull(result.msg());
        log.info("Master recruit user accessing MASTER_USER role - Expected: success, Actual: {}", result);
    }

    @Test
    void testVerifyIdentifyPermission_MasterRecruitUser_CanAccessSubRole() throws Exception {
        // Given
        IamUserContextDTO masterUser = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        UserContextUtil.setCurrentUser(masterUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.SUB_USER);
        
        // Then
        assertTrue(result.authSuccess());
        assertNull(result.code());
        assertNull(result.msg());
        log.info("Master recruit user accessing SUB_USER role - Expected: success, Actual: {}", result);
    }

    @Test
    void testVerifyIdentifyPermission_MasterRecruitUser_CannotAccessCandidateRole() throws Exception {
        // Given
        IamUserContextDTO masterUser = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        UserContextUtil.setCurrentUser(masterUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.CANDIDATE);
        
        // Then
        assertFalse(result.authSuccess());
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH.getCode(), result.code());
        assertTrue(result.msg().contains("Candidate"));
        log.info("Master recruit user accessing CANDIDATE role - Expected: identity not match, Actual: {}", result);
    }

    // ==================== SUB_RECRUIT 权限测试 ====================

    @Test
    void testVerifyIdentifyPermission_SubRecruitUser_CanAccessSubRole() throws Exception {
        // Given
        IamUserContextDTO subUser = createMockUser(UserIdentifyTypeEnum.SUB_RECRUIT);
        UserContextUtil.setCurrentUser(subUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.SUB_USER);
        
        // Then
        assertTrue(result.authSuccess());
        assertNull(result.code());
        assertNull(result.msg());
        log.info("Sub recruit user accessing SUB_USER role - Expected: success, Actual: {}", result);
    }

    @Test
    void testVerifyIdentifyPermission_SubRecruitUser_CannotAccessMasterRole() throws Exception {
        // Given
        IamUserContextDTO subUser = createMockUser(UserIdentifyTypeEnum.SUB_RECRUIT);
        UserContextUtil.setCurrentUser(subUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertFalse(result.authSuccess());
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH.getCode(), result.code());
        assertTrue(result.msg().contains("Master Account"));
        log.info("Sub recruit user accessing MASTER_USER role - Expected: role not match, Actual: {}", result);
    }

    @Test
    void testVerifyIdentifyPermission_SubRecruitUser_CannotAccessCandidateRole() throws Exception {
        // Given
        IamUserContextDTO subUser = createMockUser(UserIdentifyTypeEnum.SUB_RECRUIT);
        UserContextUtil.setCurrentUser(subUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.CANDIDATE);
        
        // Then
        assertFalse(result.authSuccess());
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH.getCode(), result.code());
        assertTrue(result.msg().contains("Candidate"));
        log.info("Sub recruit user accessing CANDIDATE role - Expected: identity not match, Actual: {}", result);
    }

    // ==================== 异常情况测试 ====================

    @Test
    void testVerifyIdentifyPermission_NoUserContext_ShouldThrowException() throws Exception {
        // Given - 没有设置用户上下文
        
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            callVerifyIdentifyPermission(RoleType.MASTER_USER);
        });
        
        // 验证是因为调用getCurrentUserNeedLogin()而抛出的异常
        assertTrue(exception.getCause().getMessage().contains("Invalid authentication") || 
                  exception.getMessage().contains("Invalid authentication"));
        log.info("No user context test - Expected exception thrown: {}", exception.getMessage());
    }

    @Test
    void testVerifyIdentifyPermission_NullUserIdentifyCode_ShouldThrowException() throws Exception {
        // Given
        IamUserContextDTO user = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        user.setUserIdentifyCode(null);
        UserContextUtil.setCurrentUser(user);
        
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            callVerifyIdentifyPermission(RoleType.MASTER_USER);
        });
        
        // 验证是因为UserIdentifyTypeEnum.getByCode(null)而抛出的异常
        log.info("Null user identify code test - Expected exception thrown: {}", exception.getMessage());
    }

    @Test
    void testVerifyIdentifyPermission_InvalidUserIdentifyCode_ShouldThrowException() throws Exception {
        // Given
        IamUserContextDTO user = createMockUser(UserIdentifyTypeEnum.MASTER_RECRUIT);
        user.setUserIdentifyCode(999); // 无效的身份代码
        UserContextUtil.setCurrentUser(user);
        
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            callVerifyIdentifyPermission(RoleType.MASTER_USER);
        });
        
        // 验证是因为UserIdentifyTypeEnum.getByCode(999)而抛出的异常
        log.info("Invalid user identify code test - Expected exception thrown: {}", exception.getMessage());
    }

    // ==================== 错误消息格式化测试 ====================

    @Test
    void testVerifyIdentifyPermission_IdentityNotMatchMessageFormatting() throws Exception {
        // Given
        IamUserContextDTO candidateUser = createMockUser(UserIdentifyTypeEnum.CANDIDATE);
        UserContextUtil.setCurrentUser(candidateUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertFalse(result.authSuccess());
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH.getCode(), result.code());
        assertEquals("Current user identify is not a Recruit.", result.msg());
        log.info("Identity not match message formatting test - Message: {}", result.msg());
    }

    @Test
    void testVerifyIdentifyPermission_RoleNotMatchMessageFormatting() throws Exception {
        // Given
        IamUserContextDTO subUser = createMockUser(UserIdentifyTypeEnum.SUB_RECRUIT);
        UserContextUtil.setCurrentUser(subUser);
        
        // When
        AuthCheckResultDTO result = callVerifyIdentifyPermission(RoleType.MASTER_USER);
        
        // Then
        assertFalse(result.authSuccess());
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH.getCode(), result.code());
        assertEquals("Current user role is not a Master Account.", result.msg());
        log.info("Role not match message formatting test - Message: {}", result.msg());
    }

    // ==================== 综合权限矩阵测试 ====================

    @Test
    void testVerifyIdentifyPermission_CompletePermissionMatrix() throws Exception {
        log.info("=== Complete Permission Matrix Test for verifyIdentifyPermission ===");
        
        // Test all combinations
        UserIdentifyTypeEnum[] userTypes = {UserIdentifyTypeEnum.CANDIDATE, UserIdentifyTypeEnum.MASTER_RECRUIT, UserIdentifyTypeEnum.SUB_RECRUIT};
        RoleType[] roleTypes = {RoleType.CANDIDATE, RoleType.MASTER_USER, RoleType.SUB_USER};
        
        for (UserIdentifyTypeEnum userType : userTypes) {
            IamUserContextDTO user = createMockUser(userType);
            UserContextUtil.setCurrentUser(user);
            
            for (RoleType roleType : roleTypes) {
                AuthCheckResultDTO result = callVerifyIdentifyPermission(roleType);
                boolean expectedSuccess = isExpectedPermission(userType, roleType);
                
                if (result.authSuccess() == expectedSuccess) {
                    log.info("✓ {} accessing {} - Expected: {}, Actual: {}", userType, roleType, expectedSuccess, result.authSuccess());
                } else {
                    log.error("✗ {} accessing {} - Expected: {}, Actual: {}", userType, roleType, expectedSuccess, result.authSuccess());
                }
                
                // 断言验证
                assertEquals(expectedSuccess, result.authSuccess(), 
                    String.format("%s accessing %s should return %s", userType, roleType, expectedSuccess));
                
                // 验证失败情况的错误码和消息
                if (!expectedSuccess) {
                    assertNotNull(result.code(), "Error code should not be null for failed auth");
                    assertNotNull(result.msg(), "Error message should not be null for failed auth");
                    assertFalse(result.msg().trim().isEmpty(), "Error message should not be empty for failed auth");
                } else {
                    assertNull(result.code(), "Error code should be null for successful auth");
                    assertNull(result.msg(), "Error message should be null for successful auth");
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

    // ==================== 边界条件测试 ====================

    @Test
    void testVerifyIdentifyPermission_AllFailureCases_ShouldReturnProperErrorCodes() throws Exception {
        log.info("=== Testing All Failure Cases with Proper Error Codes ===");
        
        // 测试身份不匹配的情况
        IamUserContextDTO candidateUser = createMockUser(UserIdentifyTypeEnum.CANDIDATE);
        UserContextUtil.setCurrentUser(candidateUser);
        
        AuthCheckResultDTO masterResult = callVerifyIdentifyPermission(RoleType.MASTER_USER);
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH.getCode(), masterResult.code());
        
        AuthCheckResultDTO subResult = callVerifyIdentifyPermission(RoleType.SUB_USER);
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH.getCode(), subResult.code());
        
        // 测试角色不匹配的情况（子账号访问主账号权限）
        IamUserContextDTO subUser = createMockUser(UserIdentifyTypeEnum.SUB_RECRUIT);
        UserContextUtil.setCurrentUser(subUser);
        
        AuthCheckResultDTO roleNotMatchResult = callVerifyIdentifyPermission(RoleType.MASTER_USER);
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH.getCode(), roleNotMatchResult.code());
        
        log.info("All failure cases tested successfully with proper error codes");
    }
}
