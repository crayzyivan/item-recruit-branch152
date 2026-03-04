package com.item.framework.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.MessageFormat;

/**
 * AuthResponseCode枚举测试
 * 测试认证响应码的功能和状态码计算
 *
 * @author Test
 * @since 2025-09-15
 */
@SpringBootTest
public class AuthResponseCodeTest {

    @Test
    public void testAuthResponseCodeValues() {
        // 测试所有枚举值是否存在
        assertNotNull(AuthResponseCode.AUTH_NOT);
        assertNotNull(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT);
        assertNotNull(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH);
        assertNotNull(AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH);
        assertNotNull(AuthResponseCode.AUTH_CURRENT_USER_NOT_PASS);
    }

    @Test
    public void testAuthNotCode() {
        // 测试 AUTH_NOT 的状态码和消息
        AuthResponseCode authNot = AuthResponseCode.AUTH_NOT;
        assertEquals(70100, authNot.getCode()); // AUTH_MODULE(70000) + 100
        assertEquals("The current user's information was not found.", authNot.getMsg());
    }

    @Test
    public void testAuthCurrentUserIdentifyNotFountCode() {
        // 测试 AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT 的状态码和消息
        AuthResponseCode authCode = AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT;
        assertEquals(70400, authCode.getCode()); // AUTH_MODULE(70000) + 400
        assertEquals("The identity information of the current user was not found.", authCode.getMsg());
    }

    @Test
    public void testAuthCurrentUserIdentifyNotMatchCode() {
        // 测试 AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH 的状态码和消息
        AuthResponseCode authCode = AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH;
        assertEquals(70700, authCode.getCode()); // AUTH_MODULE(70000) + 700
        assertEquals("The current user''s identity is not that of {0}.", authCode.getMsg());
    }

    @Test
    public void testAuthCurrentUserRoleNotMatchCode() {
        // 测试 AUTH_CURRENT_USER_ROLE_NOT_MATCH 的状态码和消息
        AuthResponseCode authCode = AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH;
        assertEquals(70800, authCode.getCode()); // AUTH_MODULE(70000) + 800
        assertEquals("The current user''s role is not a {0}.", authCode.getMsg());
    }

    @Test
    public void testAuthCurrentUserNotPassCode() {
        // 测试 AUTH_CURRENT_USER_NOT_PASS 的状态码和消息
        AuthResponseCode authCode = AuthResponseCode.AUTH_CURRENT_USER_NOT_PASS;
        assertEquals(70900, authCode.getCode()); // AUTH_MODULE(70000) + 900
        assertEquals("The current user's identify or role does not allow access.", authCode.getMsg());
    }

    @Test
    public void testIGlobalStatusCodeInterface() {
        // 测试枚举实现了 IGlobalStatusCode 接口
        assertTrue(AuthResponseCode.AUTH_NOT instanceof IGlobalStatusCode);
        
        // 测试接口方法
        IGlobalStatusCode statusCode = AuthResponseCode.AUTH_NOT;
        assertEquals(70100, statusCode.getCode());
        assertEquals("The current user's information was not found.", statusCode.getMsg());
    }

    @Test
    public void testAllEnumValuesHaveUniqueStatusCodes() {
        // 测试所有枚举值都有唯一的状态码
        AuthResponseCode[] values = AuthResponseCode.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getCode(), values[j].getCode(),
                    String.format("Status codes should be unique: %s and %s have the same code", 
                        values[i], values[j]));
            }
        }
    }

    @Test
    public void testAllEnumValuesHaveMessages() {
        // 测试所有枚举值都有非空消息
        for (AuthResponseCode authCode : AuthResponseCode.values()) {
            assertNotNull(authCode.getMsg(), 
                String.format("Message should not be null for %s", authCode));
            assertFalse(authCode.getMsg().trim().isEmpty(), 
                String.format("Message should not be empty for %s", authCode));
        }
    }

    @Test
    public void testStatusCodeCalculation() {
        // 测试状态码计算逻辑 (AUTH_MODULE + statusCode)
        // 验证 AUTH_MODULE 常量值
        assertEquals(70000, IGlobalStatusCode.AUTH_MODULE);
        
        // 验证状态码计算
        AuthResponseCode authNot = AuthResponseCode.AUTH_NOT;
        assertEquals(IGlobalStatusCode.AUTH_MODULE + 100, authNot.getCode());
    }

    @Test
    public void testStatusCodeCalculationMsg() {
        // 验证状态码计算
        AuthResponseCode authNot = AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH;
        String format = MessageFormat.format(authNot.getMsg(), RoleType.CANDIDATE.getIdentity());
        assertEquals("The current user's identity is not that of Candidate.", format);

        AuthResponseCode authNot1 = AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH;
        String format1 = MessageFormat.format(authNot1.getMsg(), RoleType.MASTER_USER.getIdentity());
        assertEquals("The current user's identity is not that of Recruit.", format1);

        AuthResponseCode authNot2 = AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH;
        String format2 = MessageFormat.format(authNot2.getMsg(), RoleType.MASTER_USER.getRole());
        assertEquals("The current user's role is not a Master Account.", format2);

        AuthResponseCode authNot3 = AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH;
        String format3 = MessageFormat.format(authNot3.getMsg(), RoleType.SUB_USER.getRole());
        assertEquals("The current user's role is not a Sub Account.", format3);

        AuthResponseCode authNot4 = AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH;
        String format4 = MessageFormat.format(authNot4.getMsg(), RoleType.CANDIDATE.getRole());
        assertEquals("The current user's role is not a Candidate.", format4);
    }
}
