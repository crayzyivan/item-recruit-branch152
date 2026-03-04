package com.item.framework.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AyrshareStatus枚举测试
 * 
 * @author system
 * @since 1.0.0
 */
public class AyrshareStatusTest {

    @Test
    public void testGetByCode() {
        // 测试正常情况
        assertEquals(AyrshareStatus.NO_SHARE, AyrshareStatus.getByCode(0));
        assertEquals(AyrshareStatus.SHARING, AyrshareStatus.getByCode(1));
        assertEquals(AyrshareStatus.SHARE_SUCCESS, AyrshareStatus.getByCode(2));
        assertEquals(AyrshareStatus.PARTIAL_SUCCESS, AyrshareStatus.getByCode(3));
        assertEquals(AyrshareStatus.SHARE_FAILED, AyrshareStatus.getByCode(-1));
        
        // 测试null值
        assertEquals(AyrshareStatus.NO_SHARE, AyrshareStatus.getByCode(null));
        
        // 测试无效值
        assertNull(AyrshareStatus.getByCode(999));
    }

    @Test
    public void testStatusMethods() {
        // 测试成功状态
        assertTrue(AyrshareStatus.SHARE_SUCCESS.isSuccess());
        assertTrue(AyrshareStatus.PARTIAL_SUCCESS.isSuccess());
        assertFalse(AyrshareStatus.SHARING.isSuccess());
        assertFalse(AyrshareStatus.SHARE_FAILED.isSuccess());
        assertFalse(AyrshareStatus.NO_SHARE.isSuccess());
        
        // 测试失败状态
        assertTrue(AyrshareStatus.SHARE_FAILED.isFailed());
        assertFalse(AyrshareStatus.SHARE_SUCCESS.isFailed());
        assertFalse(AyrshareStatus.SHARING.isFailed());
        
        // 测试进行中状态
        assertTrue(AyrshareStatus.SHARING.isInProgress());
        assertFalse(AyrshareStatus.SHARE_SUCCESS.isInProgress());
        assertFalse(AyrshareStatus.SHARE_FAILED.isInProgress());
        
        // 测试终止状态
        assertTrue(AyrshareStatus.SHARE_SUCCESS.isTerminal());
        assertTrue(AyrshareStatus.PARTIAL_SUCCESS.isTerminal());
        assertTrue(AyrshareStatus.SHARE_FAILED.isTerminal());
        assertFalse(AyrshareStatus.SHARING.isTerminal());
        assertFalse(AyrshareStatus.NO_SHARE.isTerminal());
    }

    @Test
    public void testGetAll() {
        assertNotNull(AyrshareStatus.getAll());
        assertEquals(5, AyrshareStatus.getAll().size());
        assertTrue(AyrshareStatus.getAll().contains(AyrshareStatus.NO_SHARE));
        assertTrue(AyrshareStatus.getAll().contains(AyrshareStatus.SHARING));
        assertTrue(AyrshareStatus.getAll().contains(AyrshareStatus.SHARE_SUCCESS));
        assertTrue(AyrshareStatus.getAll().contains(AyrshareStatus.PARTIAL_SUCCESS));
        assertTrue(AyrshareStatus.getAll().contains(AyrshareStatus.SHARE_FAILED));
    }

    @Test
    public void testCodeAndDescription() {
        assertEquals(Integer.valueOf(0), AyrshareStatus.NO_SHARE.getCode());
        assertEquals("No Share", AyrshareStatus.NO_SHARE.getDescription());
        
        assertEquals(Integer.valueOf(1), AyrshareStatus.SHARING.getCode());
        assertEquals("Sharing", AyrshareStatus.SHARING.getDescription());
        
        assertEquals(Integer.valueOf(2), AyrshareStatus.SHARE_SUCCESS.getCode());
        assertEquals("Share Success", AyrshareStatus.SHARE_SUCCESS.getDescription());
        
        assertEquals(Integer.valueOf(3), AyrshareStatus.PARTIAL_SUCCESS.getCode());
        assertEquals("Partial Success", AyrshareStatus.PARTIAL_SUCCESS.getDescription());
        
        assertEquals(Integer.valueOf(-1), AyrshareStatus.SHARE_FAILED.getCode());
        assertEquals("Share Failed", AyrshareStatus.SHARE_FAILED.getDescription());
    }
}
