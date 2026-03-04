package com.item.schedule;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试同步ai面试结果
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-13  16:47
 */
@SpringBootTest
@Slf4j
public class InterviewResultSyncTaskTest {
    @Resource
    private InterviewResultSyncTask interviewResultSyncTask;

    @Test
    public void test() {
        interviewResultSyncTask.syncInterviewResults();
    }
}