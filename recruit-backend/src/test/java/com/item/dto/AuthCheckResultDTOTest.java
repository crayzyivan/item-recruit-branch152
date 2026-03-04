package com.item.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * AuthCheckResultDTO record 测试
 * 测试认证检查结果DTO的功能
 *
 * @author Test
 * @since 2025-09-15
 */
@SpringBootTest
public class AuthCheckResultDTOTest {

    @Test
    public void testAuthCheckResultDTOCreation() {
        // 测试创建成功的认证结果
        AuthCheckResultDTO successResult = new AuthCheckResultDTO(true, null, null);
        
        assertTrue(successResult.authSuccess());
        assertNull(successResult.code());
        assertNull(successResult.msg());
    }

    @Test
    public void testAuthCheckResultDTOWithFailureAndCode() {
        // 测试创建失败的认证结果，包含错误码和消息
        Integer errorCode = 70700;
        String errorMessage = "Current user identify is not a Recruit.";
        
        AuthCheckResultDTO failureResult = new AuthCheckResultDTO(false, errorCode, errorMessage);
        
        assertFalse(failureResult.authSuccess());
        assertEquals(errorCode, failureResult.code());
        assertEquals(errorMessage, failureResult.msg());
    }

    @Test
    public void testAuthCheckResultDTOWithNullValues() {
        // 测试创建包含null值的认证结果
        AuthCheckResultDTO nullResult = new AuthCheckResultDTO(false, null, null);
        
        assertFalse(nullResult.authSuccess());
        assertNull(nullResult.code());
        assertNull(nullResult.msg());
    }

    @Test
    public void testAuthCheckResultDTOEquality() {
        // 测试两个相同的AuthCheckResultDTO对象相等性
        AuthCheckResultDTO result1 = new AuthCheckResultDTO(true, null, null);
        AuthCheckResultDTO result2 = new AuthCheckResultDTO(true, null, null);
        
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    public void testAuthCheckResultDTOInequality() {
        // 测试两个不同的AuthCheckResultDTO对象不相等
        AuthCheckResultDTO successResult = new AuthCheckResultDTO(true, null, null);
        AuthCheckResultDTO failureResult = new AuthCheckResultDTO(false, 70700, "Error message");
        
        assertNotEquals(successResult, failureResult);
        assertNotEquals(successResult.hashCode(), failureResult.hashCode());
    }

    @Test
    public void testAuthCheckResultDTOToString() {
        // 测试toString方法
        AuthCheckResultDTO result = new AuthCheckResultDTO(false, 70700, "Error message");
        String toString = result.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("false"));
        assertTrue(toString.contains("70700"));
        assertTrue(toString.contains("Error message"));
    }

    @Test
    public void testAuthCheckResultDTOWithDifferentCodes() {
        // 测试不同错误码的认证结果
        AuthCheckResultDTO identifyNotFound = new AuthCheckResultDTO(false, 70400, "Current user identify not fount.");
        AuthCheckResultDTO identifyNotMatch = new AuthCheckResultDTO(false, 70700, "Current user identify is not a Recruit.");
        AuthCheckResultDTO roleNotMatch = new AuthCheckResultDTO(false, 70800, "Current user role is not a Master Account.");
        AuthCheckResultDTO notPass = new AuthCheckResultDTO(false, 70900, "Current user's identify or role does not allow access.");
        
        // 验证所有失败结果
        assertFalse(identifyNotFound.authSuccess());
        assertFalse(identifyNotMatch.authSuccess());
        assertFalse(roleNotMatch.authSuccess());
        assertFalse(notPass.authSuccess());
        
        // 验证错误码
        assertEquals(70400, identifyNotFound.code());
        assertEquals(70700, identifyNotMatch.code());
        assertEquals(70800, roleNotMatch.code());
        assertEquals(70900, notPass.code());
        
        // 验证错误消息
        assertEquals("Current user identify not fount.", identifyNotFound.msg());
        assertEquals("Current user identify is not a Recruit.", identifyNotMatch.msg());
        assertEquals("Current user role is not a Master Account.", roleNotMatch.msg());
        assertEquals("Current user's identify or role does not allow access.", notPass.msg());
    }

    @Test
    public void testAuthCheckResultDTORecordProperties() {
        // 测试record的特性
        AuthCheckResultDTO result = new AuthCheckResultDTO(true, 200, "Success");
        
        // Record应该是final类
        assertTrue(result.getClass().isRecord());
        
        // 测试record的组件
        assertEquals(3, result.getClass().getRecordComponents().length);
        assertEquals("authSuccess", result.getClass().getRecordComponents()[0].getName());
        assertEquals("code", result.getClass().getRecordComponents()[1].getName());
        assertEquals("msg", result.getClass().getRecordComponents()[2].getName());
    }

    @Test
    public void testAuthCheckResultDTOWithEmptyMessage() {
        // 测试空消息的认证结果
        AuthCheckResultDTO emptyMsgResult = new AuthCheckResultDTO(false, 70700, "");
        
        assertFalse(emptyMsgResult.authSuccess());
        assertEquals(70700, emptyMsgResult.code());
        assertEquals("", emptyMsgResult.msg());
    }

    @Test
    public void testAuthCheckResultDTOWithLongMessage() {
        // 测试长消息的认证结果
        String longMessage = "This is a very long error message that might be used in some complex authentication scenarios where detailed information is needed.";
        AuthCheckResultDTO longMsgResult = new AuthCheckResultDTO(false, 70700, longMessage);
        
        assertFalse(longMsgResult.authSuccess());
        assertEquals(70700, longMsgResult.code());
        assertEquals(longMessage, longMsgResult.msg());
    }

    @Test
    public void testAuthCheckResultDTOImmutability() {
        // 测试record的不可变性
        AuthCheckResultDTO result = new AuthCheckResultDTO(true, 200, "Success");
        
        // Record的字段应该是final的，无法修改
        // 这是通过编译时检查保证的，这里只是验证值的获取
        assertTrue(result.authSuccess());
        assertEquals(200, result.code());
        assertEquals("Success", result.msg());
        
        // 创建新的实例来验证不可变性
        AuthCheckResultDTO newResult = new AuthCheckResultDTO(false, 400, "Error");
        assertNotEquals(result, newResult);
    }
}
