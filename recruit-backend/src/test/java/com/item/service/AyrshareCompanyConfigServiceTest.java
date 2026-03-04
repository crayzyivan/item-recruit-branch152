package com.item.service;

import com.item.dto.ayrshare.AyrshareTokenDTO;
import com.item.entity.AyrshareCompanyConfigEntity;
import com.item.service.impl.AyrshareCompanyConfigServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Ayrshare配置服务单元测试
 *
 * @author lh
 * @since 2025-08-28
 */
@Slf4j
@SpringBootTest
class AyrshareCompanyConfigServiceTest {

    @Resource
    private AyrshareCompanyConfigServiceImpl ayrshareCompanyConfigService;

    @Test
    public void getTokenInfoByCompanyCode() {
        AyrshareTokenDTO test = ayrshareCompanyConfigService.getTokenInfoByCompanyCode("test");
        log.info("test {}", test);
    }

    @Test
    public void findByCompanyCode() {
        AyrshareCompanyConfigEntity test = ayrshareCompanyConfigService.findByCompanyCode("test");
        log.info("test {}", test);
    }

    @Test
    public void validateApiKey() {
        Boolean validateApiKey = ayrshareCompanyConfigService.validateApiKey("t*est");
        log.info("test {}", validateApiKey);
        Boolean validateApiKey1 = ayrshareCompanyConfigService.validateApiKey("est");
        log.info("test {}", validateApiKey1);
        Boolean validateApiKey2 = ayrshareCompanyConfigService.validateApiKey("estuytrefgfd123fdx");
        log.info("test {}", validateApiKey2);
    }

    @Test
    public void saveAyrShareConfig() {
        AyrshareCompanyConfigEntity entity = new AyrshareCompanyConfigEntity();
        entity.setCompanyCode("test");
        entity.setUpdateById(1L);
        entity.setUpdateByName("update");
        entity.setCreateById(1L);
        entity.setCreateByName("create");
        entity.setAyrshareApiKey("apikey");
        entity.setAyrshareProfileKey("profilekey");
        ayrshareCompanyConfigService.saveAyrShareConfig(entity);
    }

    @Test
    public void updateAyrShareConfig() {
        AyrshareCompanyConfigEntity entity = new AyrshareCompanyConfigEntity();
        AyrshareTokenDTO test = ayrshareCompanyConfigService.getTokenInfoByCompanyCode("test");
        entity.setId(test.getId());
        entity.setUpdateById(2L);
        entity.setUpdateByName("update2");
        entity.setAyrshareApiKey("apikey2");
        entity.setAyrshareProfileKey("profilekey2");
        ayrshareCompanyConfigService.updateAyrShareConfig(entity);
    }
}
