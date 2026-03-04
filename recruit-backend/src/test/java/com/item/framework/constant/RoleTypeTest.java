package com.item.framework.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * RoleType枚举测试
 * 测试角色类型枚举的功能和映射关系
 *
 * @author Test
 * @since 2025-09-15
 */
@SpringBootTest
public class RoleTypeTest {

    @Test
    public void testRoleTypeValues() {
        // 测试所有枚举值是否存在
        assertNotNull(RoleType.NONE);
        assertNotNull(RoleType.CANDIDATE);
        assertNotNull(RoleType.MASTER_USER);
        assertNotNull(RoleType.SUB_USER);
    }

    @Test
    public void testNoneRoleType() {
        // 测试 NONE 角色类型
        RoleType none = RoleType.NONE;
        assertEquals("None", none.getName());
        assertEquals("None", none.getIdentity());
        assertEquals("None", none.getRole());
    }

    @Test
    public void testCandidateRoleType() {
        // 测试 CANDIDATE 角色类型
        RoleType candidate = RoleType.CANDIDATE;
        assertEquals("candidate", candidate.getName());
        assertEquals("Candidate", candidate.getIdentity());
        assertEquals("Candidate", candidate.getRole());
    }

    @Test
    public void testMasterUserRoleType() {
        // 测试 MASTER_USER 角色类型
        RoleType masterUser = RoleType.MASTER_USER;
        assertEquals("masterUser", masterUser.getName());
        assertEquals("Recruit", masterUser.getIdentity());
        assertEquals("Master Account", masterUser.getRole());
    }

    @Test
    public void testSubUserRoleType() {
        // 测试 SUB_USER 角色类型
        RoleType subUser = RoleType.SUB_USER;
        assertEquals("subUser", subUser.getName());
        assertEquals("Recruit", subUser.getIdentity());
        assertEquals("Sub Account", subUser.getRole());
    }

    @Test
    public void testGetByNameMethod() {
        // 测试通过名称获取角色类型
        assertEquals(RoleType.NONE, RoleType.getByName("None"));
        assertEquals(RoleType.CANDIDATE, RoleType.getByName("candidate"));
        assertEquals(RoleType.MASTER_USER, RoleType.getByName("masterUser"));
        assertEquals(RoleType.SUB_USER, RoleType.getByName("subUser"));
    }

    @Test
    public void testGetByNameWithInvalidName() {
        // 测试使用无效名称获取角色类型
        assertNull(RoleType.getByName("invalidName"));
        assertNull(RoleType.getByName(""));
        assertNull(RoleType.getByName(null));
    }

    @Test
    public void testGetByNameCaseSensitive() {
        // 测试名称大小写敏感
        assertNull(RoleType.getByName("CANDIDATE")); // 大写
        assertNull(RoleType.getByName("Candidate")); // 首字母大写
        assertEquals(RoleType.CANDIDATE, RoleType.getByName("candidate")); // 小写
    }

    @Test
    public void testAllRoleTypesHaveUniqueValues() {
        // 测试所有角色类型都有唯一的名称（由于getValue是私有的，我们测试名称唯一性）
        RoleType[] values = RoleType.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getName(), values[j].getName(),
                    String.format("Names should be unique: %s and %s have the same name", 
                        values[i], values[j]));
            }
        }
    }

    @Test
    public void testAllRoleTypesHaveUniqueNames() {
        // 测试所有角色类型都有唯一的名称
        RoleType[] values = RoleType.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getName(), values[j].getName(),
                    String.format("Names should be unique: %s and %s have the same name", 
                        values[i], values[j]));
            }
        }
    }

    @Test
    public void testRoleTypeProperties() {
        // 测试所有角色类型都有非空属性
        for (RoleType roleType : RoleType.values()) {
            assertNotNull(roleType.getName(), 
                String.format("Name should not be null for %s", roleType));
            assertNotNull(roleType.getIdentity(), 
                String.format("Identity should not be null for %s", roleType));
            assertNotNull(roleType.getRole(), 
                String.format("Role should not be null for %s", roleType));
            
            assertFalse(roleType.getName().trim().isEmpty(), 
                String.format("Name should not be empty for %s", roleType));
            assertFalse(roleType.getIdentity().trim().isEmpty(), 
                String.format("Identity should not be empty for %s", roleType));
            assertFalse(roleType.getRole().trim().isEmpty(), 
                String.format("Role should not be empty for %s", roleType));
        }
    }

    @Test
    public void testRecruitIdentityMapping() {
        // 测试招聘者身份映射
        assertEquals("Recruit", RoleType.MASTER_USER.getIdentity());
        assertEquals("Recruit", RoleType.SUB_USER.getIdentity());
        
        // 测试招聘者角色区分
        assertEquals("Master Account", RoleType.MASTER_USER.getRole());
        assertEquals("Sub Account", RoleType.SUB_USER.getRole());
    }

    @Test
    public void testCandidateIdentityMapping() {
        // 测试候选人身份映射
        assertEquals("Candidate", RoleType.CANDIDATE.getIdentity());
        assertEquals("Candidate", RoleType.CANDIDATE.getRole());
    }

    @Test
    public void testStaticMapInitialization() {
        // 测试静态映射初始化
        // 通过多次调用 getByName 来验证静态映射是否正确初始化
        for (int i = 0; i < 3; i++) {
            assertEquals(RoleType.CANDIDATE, RoleType.getByName("candidate"));
            assertEquals(RoleType.MASTER_USER, RoleType.getByName("masterUser"));
            assertEquals(RoleType.SUB_USER, RoleType.getByName("subUser"));
        }
    }

    @Test
    public void testEnumCount() {
        // 测试枚举值数量
        assertEquals(4, RoleType.values().length);
    }
}
