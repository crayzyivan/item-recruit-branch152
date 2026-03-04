package com.item.service.impl;

import com.item.framework.constant.AyrshareStatus;
import com.item.service.JobDoubleDataSourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class JobDoubleServiceTest {
    @Resource
    private JobDoubleDataSourceService jobDoubleDataSourceService;

    @Test
    public void updateAyrShare(){
        jobDoubleDataSourceService.updateJobAyrShareStatus(158L, AyrshareStatus.SHARE_SUCCESS.getCode());
    }
}
