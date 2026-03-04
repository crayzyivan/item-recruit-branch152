package com.item.service;

import com.item.dto.iam.IamUserDetailResponseDTO;
import com.item.service.client.adapter.IamRpcAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class IamRpcAdapterTest {
    @Autowired
    private IamRpcAdapter iamRpcAdapter;

    @Test
    void testGet(){
        IamUserDetailResponseDTO userDetailByIdentifier = iamRpcAdapter.getUserDetailByIdentifier("1952979922931400707");
        log.info("userDetailByIdentifier:{}", userDetailByIdentifier);
    }

    @Test
    void testSwitchUserTenant(){
        // Test with valid parameters
        boolean result = iamRpcAdapter.switchUserTenant(1952979922931400707L, "test-tenant-id");
        log.info("switchUserTenant result:{}", result);
    }

    @Test
    void testSwitchUserTenant_NullUserId(){
        // Test with null userId
        boolean result = iamRpcAdapter.switchUserTenant(null, "test-tenant-id");
        log.info("switchUserTenant with null userId result:{}", result);
        assert !result : "Expected false when userId is null";
    }

    @Test
    void testSwitchUserTenant_BlankTenantId(){
        // Test with blank tenantId
        boolean result = iamRpcAdapter.switchUserTenant(1952979922931400707L, "");
        log.info("switchUserTenant with blank tenantId result:{}", result);
        assert !result : "Expected false when tenantId is blank";
    }
}
