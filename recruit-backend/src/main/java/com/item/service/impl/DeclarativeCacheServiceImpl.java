package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.item.convert.DictionaryConverter;
import com.item.convert.LocationConverter;
import com.item.dto.cache.CityCacheDTO;
import com.item.dto.cache.CountryCacheDTO;
import com.item.dto.cache.DictionaryCacheDTO;
import com.item.dto.cache.StateCacheDTO;
import com.item.entity.CityEntity;
import com.item.entity.CountryEntity;
import com.item.entity.DictionaryEntity;
import com.item.entity.JobCategoryEntity;
import com.item.entity.JobModeEntity;
import com.item.entity.JobTypeEntity;
import com.item.entity.StateEntity;
import com.item.framework.constant.CommonConstants;
import com.item.mapper.CityMapper;
import com.item.mapper.CountryMapper;
import com.item.mapper.DictionaryMapper;
import com.item.mapper.JobCategoryMapper;
import com.item.mapper.JobModeMapper;
import com.item.mapper.JobTypeMapper;
import com.item.mapper.StateMapper;
import com.item.service.DeclarativeCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeclarativeCacheServiceImpl implements DeclarativeCacheService {
    private final JobCategoryMapper jobCategoryMapper;
    private final JobModeMapper jobModeMapper;
    private final JobTypeMapper jobTypeMapper;
    private final DictionaryMapper dictionaryMapper;
    private final CountryMapper countryMapper;
    private final StateMapper stateMapper;
    private final CityMapper cityMapper;
    private final DictionaryConverter dictionaryConverter;
    private final LocationConverter locationConverter;

    @Override
    @Cacheable(value = CommonConstants.REDIS_CACHE_JOB_CATEGORY, key = "'all'")
    public List<JobCategoryEntity> getAllJobCategoriesAllLanguage() {
        return jobCategoryMapper.selectList(Wrappers.emptyWrapper());
    }

    @Override
    @Cacheable(value = CommonConstants.REDIS_CACHE_JOB_MODE, key = "'all'")
    public List<JobModeEntity> getAllModeAllLanguage() {
        return jobModeMapper.selectList(Wrappers.emptyWrapper());
    }

    @Override
    @Cacheable(value = CommonConstants.REDIS_CACHE_JOB_TYPE, key = "'all'")
    public List<JobTypeEntity> getAllJobTypeAllLanguage() {
        return jobTypeMapper.selectList(Wrappers.emptyWrapper());
    }

    @Override
    @Cacheable(value = CommonConstants.REDIS_CACHE_DICTIONARY_BY_TYPE, key = "#type")
    public List<DictionaryCacheDTO> listByTypeAllLanguage(String type) {
        LambdaQueryWrapper<DictionaryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictionaryEntity::getType, type);
        wrapper.eq(DictionaryEntity::getStatus, 1);
        wrapper.orderByAsc(DictionaryEntity::getSort);
        return dictionaryConverter.convert2Caches(dictionaryMapper.selectList(wrapper));
    }

    @Override
    @Cacheable(value = CommonConstants.REDIS_CACHE_LOCATION_COUNTRIES, key = "'all'")
    public List<CountryCacheDTO> getAllCountriesAllLanguage() {
        LambdaQueryWrapper<CountryEntity> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(CountryEntity::getName);
        return locationConverter.convert2Caches(countryMapper.selectList(qw));
    }

    @Override
    @Cacheable(value = CommonConstants.REDIS_CACHE_LOCATION_STATES, key = "#countryId")
    public List<StateCacheDTO> getStatesAllLanguageByCountryId(Long countryId) {
        LambdaQueryWrapper<StateEntity> queryWrapper = new LambdaQueryWrapper<StateEntity>()
                .eq(StateEntity::getCountryId, countryId)
                .orderByAsc(StateEntity::getName);
        return locationConverter.convert2StateCaches(stateMapper.selectList(queryWrapper));
    }

    @Override
    @Cacheable(value = CommonConstants.REDIS_CACHE_LOCATION_CITIES, key = "#stateId")
    public List<CityCacheDTO> getCitiesAllLanguageByStateId(Long stateId) {
        LambdaQueryWrapper<CityEntity> queryWrapper = new LambdaQueryWrapper<CityEntity>()
                .eq(CityEntity::getStateId, stateId)
                .orderByAsc(CityEntity::getName);
        return locationConverter.convert2CityCaches(cityMapper.selectList(queryWrapper));
    }
}