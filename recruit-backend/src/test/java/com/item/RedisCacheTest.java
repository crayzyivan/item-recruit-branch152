package com.item;

import com.item.dto.*;
import com.item.service.*;
import com.item.util.RedisSerialNumberUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
* 测试redis
*@author liyunlong
*@version 1.0
*@since 2025-07-21  15:28
*/
@Slf4j
@SpringBootTest
public class RedisCacheTest {

    @Resource
    private JobCategoryService jobCategoryService;
    @Resource
    private JobModeService jobModeService;
    @Resource
    private JobTypeService jobTypeService;
    @Resource
    private DictionaryService dictionaryService;
    @Resource
    private LocationService locationService;
    @Resource
    RedisSerialNumberUtils redisSerialNumberUtils;


    @Test
    public void testGetAllJobCategories(){
        List<JobCategoryDto> allJobCategories = jobCategoryService.getAllJobCategories();
        log.info("allJobCategories={}", allJobCategories);
    }

    @Test
    public void testGetAllJobMode(){
        List<JobModeDto> all = jobModeService.getAll();
        log.info("allJobModel={}", all);
    }

    @Test
    public void testGetAllJobType(){
        List<JobTypeDto> allJobTypes = jobTypeService.getAllJobTypes();
        log.info("allJobTypes={}", allJobTypes);
    }

    @Test
    public void testDictionary(){
        String type="gender";
        List<DictionaryDTO> dictionaryDTOS = dictionaryService.listByType(type);
        log.info("dictionaryDTOS={}", dictionaryDTOS);
    }

    @Test
    public void testLocation(){
        List<CountryDTO> allCountries = locationService.getAllCountries();
        log.info("allCountries size:{}", allCountries.size());
        if (CollectionUtils.isNotEmpty(allCountries)){
            List<StateDTO> stateDTOS = locationService.getStatesByCountryId(allCountries.get(0).getId());
            log.info("states size:{}", stateDTOS.size());
            if (CollectionUtils.isNotEmpty(stateDTOS)){
                List<CityDTO> cities = locationService.getCitiesByStateId(stateDTOS.get(0).getId());
                log.info("cities size:{}", cities.size());
            }
        }
    }
}