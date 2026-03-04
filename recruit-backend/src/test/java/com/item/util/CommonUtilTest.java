package com.item.util;

import com.item.dto.CountryDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class CommonUtilTest {
    @Value("${job.publish.share.case:{0} is hiring for {1}. Click {2} to apply for the job.}")
    private String jobPublishShareCase;
    @Test
    public void test() {
        List<Long> cIds = List.of(1L, 2L, 3L);
        List<CountryDTO> cDTO = List.of(new CountryDTO());
        List<CountryDTO> cDTO1 = List.of(new CountryDTO(), new CountryDTO(), new CountryDTO());
        boolean collectionSizeEquals = CommonUtils.isCollectionSizeEquals(cDTO, cIds);
        Assertions.assertFalse(collectionSizeEquals);
        boolean collectionSizeEquals1 = CommonUtils.isCollectionSizeEquals(cDTO1, cIds);
        Assertions.assertTrue(collectionSizeEquals1);
    }

    @Test
    public void testJobShare(){
        String message = MessageFormat.format(jobPublishShareCase, "company name ", "job title", "https://baidu.com/query/job");
        log.info("jobPublishShareCase {} message {}", jobPublishShareCase, message);
    }
}
