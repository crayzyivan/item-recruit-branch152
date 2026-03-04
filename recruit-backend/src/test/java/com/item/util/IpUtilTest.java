package com.item.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class IpUtilTest {

    @Test
    public void test_isIpAllowed() {
        Assertions.assertTrue( IpUtil.isIpAllowed("176.3.123.101", List.of("176.3.123.101")) );
        Assertions.assertFalse( IpUtil.isIpAllowed("176.3.122.101", List.of("176.3.123.101")) );

        Assertions.assertTrue( IpUtil.isIpAllowed("176.2.1.1", List.of("176.*.*.*")) );
        Assertions.assertTrue( IpUtil.isIpAllowed("176.1.10.1", List.of("176.1.*.*")) );
        Assertions.assertTrue( IpUtil.isIpAllowed("176.152.148.1", List.of("176.*.148.1")) );
        Assertions.assertFalse( IpUtil.isIpAllowed("176.2.1.1", List.of("176.1.*.*")) );
        Assertions.assertFalse( IpUtil.isIpAllowed("182.1.1.1", List.of("176.*.*.*")) );
        Assertions.assertFalse( IpUtil.isIpAllowed("176.111.10.1", List.of("176.100.*.*")) );
        Assertions.assertFalse( IpUtil.isIpAllowed("176.152.148.2", List.of("176.*.148.1")) );

        Assertions.assertTrue( IpUtil.isIpAllowed("176.100.2.6", List.of("176.100.2.5~100")) );
        Assertions.assertTrue( IpUtil.isIpAllowed("176.100.2.100", List.of("176.100.2.5~100")) );
        Assertions.assertFalse( IpUtil.isIpAllowed("176.100.2.4", List.of("176.100.2.5~100")) );
        Assertions.assertFalse( IpUtil.isIpAllowed("176.100.2.101", List.of("176.100.2.5~100")) );

        Assertions.assertTrue( IpUtil.isIpAllowed("10.7.127.5", List.of("10.7.0~128.*")) );
        Assertions.assertFalse( IpUtil.isIpAllowed("10.7.130.5", List.of("10.7.0~128.*")) );
    }

}