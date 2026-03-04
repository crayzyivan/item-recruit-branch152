package com.item.schedule;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试Applications同步定时任务
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-11  10:26
 */

@SpringBootTest
public class ApplicationsSyncTaskTest {

    @Resource
    private ApplicationsSyncTask applicationsSyncTask;

    @Test
    public void applicationsSyncTask(){
        applicationsSyncTask.sync();
    }

}