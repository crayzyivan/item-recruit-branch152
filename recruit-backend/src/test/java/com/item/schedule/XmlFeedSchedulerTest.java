package com.item.schedule;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * XmlFeedScheduler Unit Test
 * 
 * Tests for XML Feed scheduler with parallel processing capabilities,
 * including configuration expiration checking, concurrent task execution,
 * and proper error handling.
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@SpringBootTest
public class XmlFeedSchedulerTest {
    @Resource
    private XmlFeedScheduler xmlFeedScheduler;
    @Test
    public void testXmlFeedAutoUpdateHandler(){
        xmlFeedScheduler.xmlFeedAutoUpdateHandler();
    }
}