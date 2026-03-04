package com.item.framework.constant;

import com.item.framework.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * UserIdentifyTypeEnum枚举测试
 * 测试用户身份类型枚举的功能和异常处理
 *
 * @author Test
 * @since 2025-09-15
 */
@SpringBootTest
class UserIdentifyTypeEnumTest {

    @Test
    void testUserIdentifyTypeEnumValues() {
        // 测试所有枚举值是否存在
        assertNotNull(UserIdentifyTypeEnum.CANDIDATE);
        assertNotNull(UserIdentifyTypeEnum.MASTER_RECRUIT);
        assertNotNull(UserIdentifyTypeEnum.SUB_RECRUIT);
    }

    @Test
    void testCandidateUserIdentifyType() {
        // 测试 CANDIDATE 用户身份类型
        UserIdentifyTypeEnum candidate = UserIdentifyTypeEnum.CANDIDATE;
        assertEquals(1, candidate.getCode());
        assertEquals("candidate", candidate.getDesc());
        assertEquals("Candidate", candidate.getIdentify());
        assertEquals("Candidate", candidate.getRole());
    }

    @Test
    void testMasterRecruitUserIdentifyType() {
        // 测试 MASTER_RECRUIT 用户身份类型
        UserIdentifyTypeEnum masterRecruit = UserIdentifyTypeEnum.MASTER_RECRUIT;
        assertEquals(2, masterRecruit.getCode());
        assertEquals("master recruit", masterRecruit.getDesc());
        assertEquals("Recruit", masterRecruit.getIdentify());
        assertEquals("Master Account", masterRecruit.getRole());
    }

    @Test
    void testSubRecruitUserIdentifyType() {
        // 测试 SUB_RECRUIT 用户身份类型
        UserIdentifyTypeEnum subRecruit = UserIdentifyTypeEnum.SUB_RECRUIT;
        assertEquals(3, subRecruit.getCode());
        assertEquals("sub recruit", subRecruit.getDesc());
        assertEquals("Recruit", subRecruit.getIdentify());
        assertEquals("Sub Account", subRecruit.getRole());
    }

    @Test
    void testGetByCodeValidCodes() {
        // 测试通过有效代码获取枚举
        assertEquals(UserIdentifyTypeEnum.CANDIDATE, UserIdentifyTypeEnum.getByCode(1));
        assertEquals(UserIdentifyTypeEnum.MASTER_RECRUIT, UserIdentifyTypeEnum.getByCode(2));
        assertEquals(UserIdentifyTypeEnum.SUB_RECRUIT, UserIdentifyTypeEnum.getByCode(3));
    }

    @Test
    void testGetByCodeWithNullCode() {
        // 测试使用null代码获取枚举应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            UserIdentifyTypeEnum.getByCode(null);
        });
        
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT.getCode(), exception.getCode());
    }

    @Test
    void testGetByCodeWithInvalidCode() {
        // 测试使用无效代码获取枚举应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            UserIdentifyTypeEnum.getByCode(999);
        });
        
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT.getCode(), exception.getCode());
    }

    @Test
    void testGetByCodeWithZeroCode() {
        // 测试使用0代码获取枚举应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            UserIdentifyTypeEnum.getByCode(0);
        });
        
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT.getCode(), exception.getCode());
    }

    @Test
    void testGetByCodeWithNegativeCode() {
        // 测试使用负数代码获取枚举应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            UserIdentifyTypeEnum.getByCode(-1);
        });
        
        assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT.getCode(), exception.getCode());
    }

    @Test
    void testAllUserIdentifyTypesHaveUniqueCodes() {
        // 测试所有用户身份类型都有唯一的代码
        UserIdentifyTypeEnum[] values = UserIdentifyTypeEnum.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getCode(), values[j].getCode(),
                    String.format("Codes should be unique: %s and %s have the same code", 
                        values[i], values[j]));
            }
        }
    }

    @Test
    void testUserIdentifyTypeProperties() {
        // 测试所有用户身份类型都有非空属性
        for (UserIdentifyTypeEnum userType : UserIdentifyTypeEnum.values()) {
            assertNotNull(userType.getDesc(), 
                String.format("Description should not be null for %s", userType));
            assertNotNull(userType.getIdentify(), 
                String.format("Identify should not be null for %s", userType));
            assertNotNull(userType.getRole(), 
                String.format("Role should not be null for %s", userType));
            
            assertFalse(userType.getDesc().trim().isEmpty(), 
                String.format("Description should not be empty for %s", userType));
            assertFalse(userType.getIdentify().trim().isEmpty(), 
                String.format("Identify should not be empty for %s", userType));
            assertFalse(userType.getRole().trim().isEmpty(), 
                String.format("Role should not be empty for %s", userType));
            
            assertTrue(userType.getCode() > 0, 
                String.format("Code should be positive for %s", userType));
        }
    }

    @Test
    void testRoleTypeIntegration() {
        // 测试与 RoleType 的集成
        assertEquals(RoleType.CANDIDATE.getIdentity(), UserIdentifyTypeEnum.CANDIDATE.getIdentify());
        assertEquals(RoleType.CANDIDATE.getRole(), UserIdentifyTypeEnum.CANDIDATE.getRole());
        
        assertEquals(RoleType.MASTER_USER.getIdentity(), UserIdentifyTypeEnum.MASTER_RECRUIT.getIdentify());
        assertEquals(RoleType.MASTER_USER.getRole(), UserIdentifyTypeEnum.MASTER_RECRUIT.getRole());
        
        assertEquals(RoleType.SUB_USER.getIdentity(), UserIdentifyTypeEnum.SUB_RECRUIT.getIdentify());
        assertEquals(RoleType.SUB_USER.getRole(), UserIdentifyTypeEnum.SUB_RECRUIT.getRole());
    }

    @Test
    void testRecruitIdentityMapping() {
        // 测试招聘者身份映射
        assertEquals("Recruit", UserIdentifyTypeEnum.MASTER_RECRUIT.getIdentify());
        assertEquals("Recruit", UserIdentifyTypeEnum.SUB_RECRUIT.getIdentify());
        
        // 测试招聘者角色区分
        assertEquals("Master Account", UserIdentifyTypeEnum.MASTER_RECRUIT.getRole());
        assertEquals("Sub Account", UserIdentifyTypeEnum.SUB_RECRUIT.getRole());
    }

    @Test
    void testCandidateIdentityMapping() {
        // 测试候选人身份映射
        assertEquals("Candidate", UserIdentifyTypeEnum.CANDIDATE.getIdentify());
        assertEquals("Candidate", UserIdentifyTypeEnum.CANDIDATE.getRole());
    }

    @Test
    void testHolderClassMapInitialization() {
        // 测试内部 Holder 类的映射初始化
        // 通过多次调用 getByCode 来验证映射是否正确初始化
        for (int i = 0; i < 3; i++) {
            assertEquals(UserIdentifyTypeEnum.CANDIDATE, UserIdentifyTypeEnum.getByCode(1));
            assertEquals(UserIdentifyTypeEnum.MASTER_RECRUIT, UserIdentifyTypeEnum.getByCode(2));
            assertEquals(UserIdentifyTypeEnum.SUB_RECRUIT, UserIdentifyTypeEnum.getByCode(3));
        }
    }

    @Test
    void testEnumCount() {
        // 测试枚举值数量
        assertEquals(3, UserIdentifyTypeEnum.values().length);
    }

    @Test
    void testExceptionMessage() {
        // 测试异常消息是否正确
        try {
            UserIdentifyTypeEnum.getByCode(null);
            fail("Should throw BusinessException");
        } catch (BusinessException e) {
            assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT.getMsg(), e.getMessage());
        }
        
        try {
            UserIdentifyTypeEnum.getByCode(999);
            fail("Should throw BusinessException");
        } catch (BusinessException e) {
            assertEquals(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT.getMsg(), e.getMessage());
        }
    }
}
