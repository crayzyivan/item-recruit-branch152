package com.item.framework;

/**
 * @author : lh
 */

import com.item.dto.AyrShareRequestDTO;
import com.item.service.AyrShareService;
import com.item.service.ShortIdGenerator;
import com.item.util.CommonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class HttpClient5ServiceTest {
    @Resource
    private ShortIdGenerator shortIdGenerator;
    @Resource
    private AyrShareService ayrShareService;
    @Test
    public void test() throws InterruptedException {
        String idStr = shortIdGenerator.generateShortId(155L);
        String publishURL = CommonUtils.joinInclinedRod("https://recruit-dev.item.pub", "candidate", "company-test-java");
        String publishURLIdStr = CommonUtils.join(publishURL, idStr) + "&type=list";
        log.info("publishURLIdStr {}", publishURLIdStr);
        ayrShareService.asyncSendToAyrShare(AyrShareRequestDTO.build("java" + " " + publishURLIdStr, List.of("all"), null));
        Timeout.ofSeconds(10).sleep();
    }
}
