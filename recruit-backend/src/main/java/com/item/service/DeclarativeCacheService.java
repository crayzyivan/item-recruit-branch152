package com.item.service;

import com.item.dto.cache.CityCacheDTO;
import com.item.dto.cache.CountryCacheDTO;
import com.item.dto.cache.DictionaryCacheDTO;
import com.item.dto.cache.StateCacheDTO;
import com.item.entity.JobCategoryEntity;
import com.item.entity.JobModeEntity;
import com.item.entity.JobTypeEntity;

import java.util.List;

/**
 * @author : lh
 */
public interface DeclarativeCacheService {

    /**
     * 获取所有的category
     *
     * @return
     */
    List<JobCategoryEntity> getAllJobCategoriesAllLanguage();

    /**
     *  获取所有的jobMode
     * @return
     */
    List<JobModeEntity> getAllModeAllLanguage();

    /**
     *  获取所有的jobMode
     * @return
     */
    List<JobTypeEntity> getAllJobTypeAllLanguage();

    /**
     * 获取所有的字典数据 根据type
     * @param type
     * @return
     */
    List<DictionaryCacheDTO> listByTypeAllLanguage(String type);

    /**
     * 获取所有国家数据
     *
     * @return
     */
    List<CountryCacheDTO> getAllCountriesAllLanguage();

    /**
     * 获取国家下所有省
     *
     * @param countryId
     * @return
     */
    List<StateCacheDTO> getStatesAllLanguageByCountryId(Long countryId);

    /**
     * 根据省获取所有市
     *
     * @param stateId
     * @return
     */
    List<CityCacheDTO> getCitiesAllLanguageByStateId(Long stateId);
}
