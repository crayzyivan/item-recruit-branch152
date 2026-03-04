package com.item.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class GenerateUrlCodeServiceTest {
    @Resource
    private GenerateUrlCodeService generateUrlCodeService;

    @Test
    void test001() {
        long epochSecond = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC);
        String s = generateUrlCodeService.generateUrlQuestionLink(797L, epochSecond + "");
        log.info("s=" + s);
    }
}
