package com.item.service.impl;

import com.item.dto.DictionaryDTO;
import com.item.service.DictionaryService;
import com.item.util.UserContextUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class DictionaryServiceTest {
    @Resource
    private DictionaryService dictionaryService;

    @Test
    void testList() {
        dictionaryService.listByType("pay_cycle");
    }

    @Test
    void testList01() {
        UserContextUtil.setLanguageLocal(Locale.ENGLISH);
        List<DictionaryDTO> dictionaryDTOS = dictionaryService.listByTypes(List.of("pay_cycle"));
        log.info("dictionaryDTOS={}", dictionaryDTOS);
        UserContextUtil.clear();
    }
}
