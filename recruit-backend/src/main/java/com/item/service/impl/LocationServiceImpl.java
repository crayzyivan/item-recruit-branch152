package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.convert.LocationConverter;
import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.CountryIsoDTO;
import com.item.dto.FullLocationDTO;
import com.item.dto.LanguageDTO;
import com.item.dto.LocationDto;
import com.item.dto.LocationSearchDTO;
import com.item.dto.StateDTO;
import com.item.dto.cache.CityCacheDTO;
import com.item.dto.cache.CountryCacheDTO;
import com.item.dto.cache.StateCacheDTO;
import com.item.dto.googlemap.GoogleMapAutocompleteResponseDTO;
import com.item.dto.googlemap.GoogleMapPlaceDetailsResponseDTO;
import com.item.dto.googlemap.PredictionDTO;
import com.item.dto.job.LocationValDTO;
import com.item.entity.*;
import com.item.framework.config.GoogleMapConfig;
import com.item.framework.constant.DictionaryEnum;
import com.item.framework.constant.LocationMatchTypeEnum;
import com.item.framework.http.Pager;
import com.item.framework.net.HttpClient5Service;
import com.item.mapper.CityMapper;
import com.item.mapper.CountryMapper;
import com.item.mapper.LanguageMapper;
import com.item.mapper.LocationMapper;
import com.item.mapper.StateMapper;
import com.item.service.*;
import com.item.util.JsonUtils;
import com.item.util.LanguageLocalUtils;
import com.item.util.UserContextUtil;
import com.item.vo.LocationSearchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl extends ServiceImpl<LocationMapper, LocationEntity> implements LocationService {
    
    private static final int MAX_SEARCH_RESULTS = 50;
    private static final int MAX_STATE_SEARCH_RESULTS = 5;
    private static final int MAX_COUNTRY_SEARCH_RESULTS = 3;

    private final CountryMapper countryMapper;
    private final StateMapper stateMapper;
    private final CityMapper cityMapper;
    private final LanguageMapper languageMapper;
    private final CityService cityService;
    private final StateService stateService;
    private final CountryService countryService;
    private final DeclarativeCacheService declarativeCacheService;
    private final DictionaryService dictionaryService;
    private final GoogleMapConfig googleMapConfig;
    private final HttpClient5Service httpClient5Service;
    private final PlaceService placeService;
    private final ThreadPoolTaskExecutor locationTaskExecutor;
    private Map<String, Map<String, DictionaryEntity>> dictionaryMap;

    @Override
    public List<LocationDto> findAll() {
        return LocationConverter.INSTANCE.convertToListDto(this.list());
    }

    @Override
    public List<CountryDTO> getAllCountries() {
        List<CountryCacheDTO> allCountriesAllLanguage = declarativeCacheService.getAllCountriesAllLanguage();
        Function<CountryCacheDTO, String> functionCountryCacheName = LanguageLocalUtils.getFunctionCountryCacheName(UserContextUtil.getLanguageLocal());
        return LocationConverter.INSTANCE.toCountryDTOs(allCountriesAllLanguage, functionCountryCacheName);
    }

    @Override
    public List<LanguageDTO> getAllLanguages() {
        boolean chinese = LanguageLocalUtils.isChinese(UserContextUtil.getLanguageLocal());
        LambdaQueryWrapper<LanguageEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LanguageEntity::getCode);
        List<LanguageEntity> languageEntities = languageMapper.selectList(queryWrapper);
        return LocationConverter.INSTANCE.toLanguageDTOList(languageEntities, chinese);
    }

    @Override
    public List<StateDTO> getStatesByCountryId(Long countryId) {
        if (countryId == null || countryId <= 0) {
            return List.of();
        }
        List<StateCacheDTO> statesAllLanguageByCountryId = declarativeCacheService.getStatesAllLanguageByCountryId(countryId);
        if(CollectionUtils.isEmpty(statesAllLanguageByCountryId)) {
            return List.of();
        }
        Function<StateCacheDTO, String> functionStateCacheName = LanguageLocalUtils.getFunctionStateCacheName(UserContextUtil.getLanguageLocal());
        return LocationConverter.INSTANCE.toStateDTOs(statesAllLanguageByCountryId, functionStateCacheName);
    }

    @Override
    public List<CityDTO> getCitiesByStateId(Long stateId) {
        if (stateId == null || stateId <= 0) {
            return List.of();
        }
        List<CityCacheDTO> cityEntities = declarativeCacheService.getCitiesAllLanguageByStateId(stateId);
        if(CollectionUtils.isEmpty(cityEntities)) {
            return List.of();
        }
        Function<CityCacheDTO, String> functionCityCacheName = LanguageLocalUtils.getFunctionCityCacheName(UserContextUtil.getLanguageLocal());
        return LocationConverter.INSTANCE.toCityDTOs(cityEntities, functionCityCacheName);
    }

    @Override
    public Object getLocationDetail(Long countryId, Long stateId, Long cityId) {
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("getLocationDetail countryId {} stateId {} cityId {} chinese {}", countryId, stateId, cityId, languageLocal);
        if (cityId != null) {
            Function<CityEntity, String> functionCityName = LanguageLocalUtils.getFunctionCityName(languageLocal);
            return LocationConverter.INSTANCE.entityToCityDTO(cityMapper.selectById(cityId), functionCityName);
        } else if (stateId != null) {
            Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(languageLocal);
            return LocationConverter.INSTANCE.entityToStateDTO(stateMapper.selectById(stateId), functionStateName);
        } else if (countryId != null) {
            Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(languageLocal);
            return LocationConverter.INSTANCE.entityToCountryDTO(countryMapper.selectById(countryId), functionCountryName);
        }
        return null;
    }

    @Override
    public CountryDTO getCountryByCityId(Long cityId) {
        CityEntity city = cityMapper.selectById(cityId);
        if (city == null || city.getCountryId() == null) {
            return null;
        }
        log.info("getCountryByCityId cityId {}", cityId);
        Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(UserContextUtil.getLanguageLocal());
        return LocationConverter.INSTANCE.entityToCountryDTO(countryMapper.selectById(city.getCountryId()), functionCountryName);
    }

    @Override
    public StateDTO getStateByCityId(Long cityId) {
        CityEntity city = cityMapper.selectById(cityId);
        if (city == null || city.getStateId() == null) {
            return null;
        }
        log.info("getStateByCityId stateId {}", cityId);
        Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(UserContextUtil.getLanguageLocal());
        return LocationConverter.INSTANCE.entityToStateDTO(stateMapper.selectById(city.getStateId()), functionStateName);
    }

    /**
     * 根据国家id列表批量获取国家信息
     * @param countryIds
     * @return
     */
    @Override
    public List<CountryDTO> listByCountryIds(List<Long> countryIds) {
        if (CollectionUtils.isNotEmpty(countryIds)){
            log.info("listByCountryIds countryIds {}", countryIds);
            List<CountryEntity> countryEntities = countryMapper.selectByIds(countryIds);
            Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(UserContextUtil.getLanguageLocal());
            return LocationConverter.INSTANCE.entityToCountryDTOs(countryEntities, functionCountryName);

        }
        return new ArrayList<>();
    }

    /**
     * 查询全部国家
     * @return
     */
    @Override
    public List<CountryIsoDTO> selectAllCountryIso() {
        List<CountryEntity> countryEntities = countryMapper.selectList(new LambdaQueryWrapper<>());
        return LocationConverter.INSTANCE.toCountryIsoDTOList(countryEntities);
    }

    /**
     * 根据省id列表批量获取省信息
     * @param stateIds
     * @return
     */
    @Override
    public List<StateDTO> listByStateIds(List<Long> stateIds) {
        if (CollectionUtils.isNotEmpty(stateIds)){
            log.info("listByStateIds stateIds {}", stateIds);
            List<StateEntity> stateEntities=stateMapper.selectByIds(stateIds);
            Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(UserContextUtil.getLanguageLocal());
            return LocationConverter.INSTANCE.entityToStateDTOs(stateEntities, functionStateName);
        }
        return new ArrayList<>();
    }

    /**
     * 根据市id列表批量获取市信息
     * @param cityIds
     * @return
     */
    @Override
    public List<CityDTO> listByCityIds(List<Long> cityIds) {
        if (CollectionUtils.isNotEmpty(cityIds)){
            log.info("listByCityIds cityIds {}", cityIds);
            List<CityEntity> cityEntities = cityMapper.selectByIds(cityIds);
            Function<CityEntity, String> functionCityName = LanguageLocalUtils.getFunctionCityName(UserContextUtil.getLanguageLocal());
            return LocationConverter.INSTANCE.entityToCityDTOs(cityEntities, functionCityName);
        }
        return new ArrayList<>();
    }

    @Override
    public FullLocationDTO getFullLocation(Long cityId) {
        CityEntity city = cityMapper.selectById(cityId);
        if (city == null) {
            return new FullLocationDTO();
        }
        
        StateEntity stateEntity = null;
        CountryEntity countryEntity = null;
        
        if (city.getStateId() != null) {
            stateEntity = stateMapper.selectById(city.getStateId());
            if (stateEntity != null && stateEntity.getCountryId() != null) {
                countryEntity = countryMapper.selectById(stateEntity.getCountryId());
            }
        }

        FullLocationDTO dto = new FullLocationDTO();
        dto.setCityName(city.getName());
        if (stateEntity != null) {
            dto.setStateName(stateEntity.getName());
        }
        if (countryEntity != null) {
            dto.setCountryName(countryEntity.getName());
        }

        return dto;
    }

    @Override
    public Pager<LocationSearchResultVO> fuzzySearchLocation(LocationSearchDTO searchDTO) {
        log.info("Starting fuzzy search for location, keyword: {}, page: {}, size: {}", 
                searchDTO.getKeyword(), searchDTO.getPage(), searchDTO.getSize());
        
        try {
            // 参数验证
            if (StringUtils.isBlank(searchDTO.getKeyword())) {
                log.warn("Search keyword is blank, returning empty result");
                return Pager.buildEmpty();
            }
            
            // 设置默认分页参数
            int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
            int size = searchDTO.getSize() != null ? searchDTO.getSize() : 20;
            List<DictionaryEntity> dictionaryEntities = dictionaryService.list();
            dictionaryMap = dictionaryEntities.stream()
                    // 按type分组
                    .collect(Collectors.groupingBy(
                            DictionaryEntity::getType,
                            // 每组内再按value分组，保留对应的实体
                            Collectors.toMap(
                                    entity -> entity.getCode(),
                                    entity -> entity,
                                    // 若存在相同value的实体，保留第一个
                                    (existing, replacement) -> existing
                            )
                    ));

            List<LocationSearchResultVO> allResults = new ArrayList<>();
            
            // 按优先级搜索：城市 > 省份 > 国家
            // 1. 优先搜索城市
            if (!Boolean.TRUE.equals(searchDTO.getStateOnly()) && !Boolean.TRUE.equals(searchDTO.getCountryOnly())) {
                List<LocationSearchResultVO> cityResults = searchCities(searchDTO.getKeyword());
                if (!cityResults.isEmpty()) {
                    allResults.addAll(cityResults);
                    log.debug("Found {} cities matching keyword: {}", cityResults.size(), searchDTO.getKeyword());
                } else {
                    // 2. 如果城市没匹配到，搜索省份并返回该省份下的城市
                    if (!Boolean.TRUE.equals(searchDTO.getCityOnly()) && !Boolean.TRUE.equals(searchDTO.getCountryOnly())) {
                        List<LocationSearchResultVO> stateResults = searchStatesWithCities(searchDTO.getKeyword());
                        if (!stateResults.isEmpty()) {
                            allResults.addAll(stateResults);
                            log.debug("Found {} states with cities matching keyword: {}", stateResults.size(), searchDTO.getKeyword());
                        } else {
                            // 3. 如果省市都没匹配到，搜索国家并返回该国家下的省市数据
                            if (!Boolean.TRUE.equals(searchDTO.getCityOnly()) && !Boolean.TRUE.equals(searchDTO.getStateOnly())) {
                                List<LocationSearchResultVO> countryResults = searchCountriesWithStatesAndCities(searchDTO.getKeyword());
                                allResults.addAll(countryResults);
                                log.debug("Found {} countries with states and cities matching keyword: {}", countryResults.size(), searchDTO.getKeyword());
                            }
                        }
                    }
                }
            } else {
                // 如果指定了只搜索省份或国家，则按原逻辑搜索
                if (!Boolean.TRUE.equals(searchDTO.getCityOnly()) && !Boolean.TRUE.equals(searchDTO.getCountryOnly())) {
                    List<LocationSearchResultVO> stateResults = searchStates(searchDTO.getKeyword());
                    allResults.addAll(stateResults);
                    log.debug("Found {} states matching keyword: {}", stateResults.size(), searchDTO.getKeyword());
                }
                
                if (!Boolean.TRUE.equals(searchDTO.getCityOnly()) && !Boolean.TRUE.equals(searchDTO.getStateOnly())) {
                    List<LocationSearchResultVO> countryResults = searchCountries(searchDTO.getKeyword());
                    allResults.addAll(countryResults);
                    log.debug("Found {} countries matching keyword: {}", countryResults.size(), searchDTO.getKeyword());
                }
            }
            
            // 确保最终结果包含城市级别的信息
            allResults = ensureCityLevelResults(allResults, searchDTO.getKeyword());
            
            // 手动分页
            int totalCount = allResults.size();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalCount);
            
            List<LocationSearchResultVO> pageResults;
            if (startIndex >= totalCount) {
                pageResults = new ArrayList<>();
            } else {
                pageResults = allResults.subList(startIndex, endIndex);
            }
            
            // 构建分页结果
            Pager<LocationSearchResultVO> result = new Pager<>();
            result.setPageIndex(page+1);
            result.setPageSize(size);
            result.setTotalCount(totalCount);
            result.setCurrentPageRecords(pageResults);
            
            log.info("Fuzzy search completed, total found: {}, page results: {}", totalCount, pageResults.size());
            return result;
            
        } catch (Exception e) {
            log.error("Error occurred during fuzzy search for keyword: {}", searchDTO.getKeyword(), e);
            return Pager.buildEmpty();
        }
    }

    @Override
    public CountryDTO getCountryById(Long id) {
        return LocationConverter.INSTANCE.toCountryDTO(countryMapper.selectById(id));
    }


    @Override
    public Map<Long, String> listIdNameMapByCountryIds(List<Long> countryIds) {
        List<CountryDTO> countryDTOS = listByCountryIds(countryIds);
        if(CollectionUtils.isEmpty(countryDTOS)) {
            return new HashMap<>();
        }
        return countryDTOS.stream().collect(Collectors.toMap(CountryDTO::getId, CountryDTO::getName, (n1, n2) -> n1));
    }

    @Override
    public Map<Long, String> listIdNameMapByStateIds(List<Long> stateIds) {
        List<StateDTO> stateDTOS = listByStateIds(stateIds);
        if(CollectionUtils.isEmpty(stateDTOS)) {
            return new HashMap<>();
        }
        return stateDTOS.stream().collect(Collectors.toMap(StateDTO::getId, StateDTO::getName, (n1, n2) -> n1));
    }

    @Override
    public Map<Long, String> listIdNameMapByCityIds(List<Long> cityIds) {
        List<CityDTO> cityDTOS = listByCityIds(cityIds);
        if(CollectionUtils.isEmpty(cityDTOS)) {
            return new HashMap<>();
        }
        return cityDTOS.stream().collect(Collectors.toMap(CityDTO::getId, CityDTO::getName, (n1, n2) -> n1));
    }

    @Override
    public List<CountryDTO> listEnCountryByCountryIds(List<Long> countryIds) {
        if (CollectionUtils.isNotEmpty(countryIds)){
            List<CountryEntity> countryEntities = countryMapper.selectByIds(countryIds);
            return LocationConverter.INSTANCE.entityToCountryDTOs(countryEntities, CountryEntity::getName);

        }
        return List.of();
    }

    @Override
    public List<StateDTO> listEnStateByStateIds(List<Long> stateIds) {
        if (CollectionUtils.isNotEmpty(stateIds)){
            List<StateEntity> stateEntities = stateMapper.selectByIds(stateIds);
            return LocationConverter.INSTANCE.entityToStateDTOs(stateEntities, StateEntity::getName);

        }
        return List.of();
    }

    @Override
    public List<CityDTO> listEnCityByCityIds(List<Long> cityIds) {
        if (CollectionUtils.isNotEmpty(cityIds)){
            List<CityEntity> cityEntities = cityMapper.selectByIds(cityIds);
            return LocationConverter.INSTANCE.entityToCityDTOs(cityEntities, CityEntity::getName);

        }
        return List.of();
    }

    /**
     * 搜索城市
     */
    private List<LocationSearchResultVO> searchCities(String keyword) {
//        LambdaQueryWrapper<CityEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.like(CityEntity::getName, keyword)
//                   .orderByAsc(CityEntity::getName)
//                   .last("LIMIT " + MAX_SEARCH_RESULTS); // 限制结果数量
//
//        List<CityEntity> cities = cityMapper.selectList(queryWrapper);
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        List<CityEntity> cities = cityService.searchCitiesByLanguage(keyword, MAX_SEARCH_RESULTS, languageLocal);
        Function<CityEntity, String> functionCityName = LanguageLocalUtils.getFunctionCityName(languageLocal);
        Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(languageLocal);
        Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(languageLocal);
        return cities.stream().map(city -> {
            LocationSearchResultVO result = new LocationSearchResultVO();
            result.setId(city.getId());
//            result.setName(city.getName());
            result.setMatchType(LocationMatchTypeEnum.CITY.getCode());
            result.setMatchTypeName(LocationMatchTypeEnum.CITY.getName());
            result.setCityId(city.getId());
//            result.setCityName(city.getName());
            result.fillCityNameByLanguage(functionCityName, city);
            result.setName(result.getCityName());
            result.setStateId(city.getStateId());
            result.setCountryId(city.getCountryId());
            result.setLongitude(city.getLongitude() != null ? city.getLongitude().doubleValue() : null);
            result.setLatitude(city.getLatitude() != null ? city.getLatitude().doubleValue() : null);
            
            // 获取完整的省和国家信息
            if (city.getStateId() != null) {
                StateEntity state = stateMapper.selectById(city.getStateId());
                if (state != null) {
                    result.fillStateNameByLanguage(functionStateName, state);
//                    result.setStateName(state.getName());
                    if (city.getCountryId() != null) {
                        CountryEntity country = countryMapper.selectById(city.getCountryId());
                        if (country != null) {
//                            result.setCountryName(country.getName());
                            result.fillCountryNameByLanguage(functionCountryName, country);
                            result.setCurrency(getCurrency(country.getCurrency()));
                        }
                    }
                }
            }
            
            // 构建完整地址
            StringBuilder fullAddress = new StringBuilder();
            if (result.getCityName() != null) {
                fullAddress.append(result.getCityName());
            }
            if (result.getStateName() != null) {
                if (!fullAddress.isEmpty()) fullAddress.append(", ");
                fullAddress.append(result.getStateName());
            }
            if (result.getCountryName() != null) {
                if (!fullAddress.isEmpty()) fullAddress.append(", ");
                fullAddress.append(result.getCountryName());
            }
            result.setFullAddress(fullAddress.toString());
            
            return result;
        }).collect(Collectors.toList());
    }
    
    /**
     * 搜索省份
     */
    private List<LocationSearchResultVO> searchStates(String keyword) {
//        LambdaQueryWrapper<StateEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.like(StateEntity::getName, keyword)
//                   .orderByAsc(StateEntity::getName)
//                   .last("LIMIT " + MAX_SEARCH_RESULTS); // 限制结果数量
//
//        List<StateEntity> states = stateMapper.selectList(queryWrapper);
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        List<StateEntity> states = stateService.searchStatesByLanguage(keyword, MAX_SEARCH_RESULTS, languageLocal);
        Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(languageLocal);
        Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(languageLocal);
        return states.stream().map(state -> {
            LocationSearchResultVO result = new LocationSearchResultVO();
            result.setId(state.getId());
//            result.setName(state.getName());
            result.setMatchType(LocationMatchTypeEnum.STATE.getCode());
            result.setMatchTypeName(LocationMatchTypeEnum.STATE.getName());
            result.setStateId(state.getId());
//            result.setStateName(state.getName());
            result.fillStateNameByLanguage(functionStateName, state);
            result.setCountryId(state.getCountryId());
            result.setLongitude(state.getLongitude() != null ? state.getLongitude().doubleValue() : null);
            result.setLatitude(state.getLatitude() != null ? state.getLatitude().doubleValue() : null);
            
            // 获取国家信息
            if (state.getCountryId() != null) {
                CountryEntity country = countryMapper.selectById(state.getCountryId());
                if (country != null) {
//                    result.setCountryName(country.getName());
                    result.fillCountryNameByLanguage(functionCountryName, country);
                    result.setCurrency(getCurrency(country.getCurrency()));
                }
            }
            
            // 构建完整地址
            StringBuilder fullAddress = new StringBuilder();
            if (result.getStateName() != null) {
                fullAddress.append(result.getStateName());
            }
            if (result.getCountryName() != null) {
                if (!fullAddress.isEmpty()) fullAddress.append(", ");
                fullAddress.append(result.getCountryName());
            }
            result.setFullAddress(fullAddress.toString());
            
            return result;
        }).collect(Collectors.toList());
    }
    
    /**
     * 搜索省份并返回该省份下的城市
     */
    private List<LocationSearchResultVO> searchStatesWithCities(String keyword) {
//        LambdaQueryWrapper<StateEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.like(StateEntity::getName, keyword)
//                   .orderByAsc(StateEntity::getName)
//                   .last("LIMIT 5"); // 限制省份数量
//
//        List<StateEntity> states = stateMapper.selectList(queryWrapper);

        Locale languageLocal = UserContextUtil.getLanguageLocal();
        List<StateEntity> states = stateService.searchStatesByLanguage(keyword, MAX_STATE_SEARCH_RESULTS, languageLocal);
        Function<CityEntity, String> functionCityName = LanguageLocalUtils.getFunctionCityName(languageLocal);
        Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(languageLocal);
        Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(languageLocal);
        List<LocationSearchResultVO> results = new ArrayList<>();
        
        for (StateEntity state : states) {
            // 获取该省份下的城市
            LambdaQueryWrapper<CityEntity> cityQueryWrapper = new LambdaQueryWrapper<>();
            cityQueryWrapper.eq(CityEntity::getStateId, state.getId())
                           .orderByAsc(CityEntity::getName)
                           .last("LIMIT 10"); // 每个省份最多返回10个城市
            
            List<CityEntity> cities = cityMapper.selectList(cityQueryWrapper);
            
            for (CityEntity city : cities) {
                LocationSearchResultVO result = new LocationSearchResultVO();
                result.setId(city.getId());
//                result.setName(city.getName());
                result.setMatchType(LocationMatchTypeEnum.CITY.getCode());
                result.setMatchTypeName(LocationMatchTypeEnum.CITY.getName());
                result.setCityId(city.getId());
//                result.setCityName(city.getName());
                result.fillCityNameByLanguage(functionCityName, city);
                result.setName(result.getCityName());
                result.setStateId(state.getId());
//                result.setStateName(state.getName());
                result.fillStateNameByLanguage(functionStateName, state);
                result.setCountryId(state.getCountryId());
                result.setLongitude(city.getLongitude() != null ? city.getLongitude().doubleValue() : null);
                result.setLatitude(city.getLatitude() != null ? city.getLatitude().doubleValue() : null);
                
                // 获取国家信息
                if (state.getCountryId() != null) {
                    CountryEntity country = countryMapper.selectById(state.getCountryId());
                    if (country != null) {
//                        result.setCountryName(country.getName());
                        result.fillCountryNameByLanguage(functionCountryName, country);
                        result.setCurrency(getCurrency(country.getCurrency()));
                    }
                }
                
                // 构建完整地址
                StringBuilder fullAddress = new StringBuilder();
                if (result.getCityName() != null) {
                    fullAddress.append(result.getCityName());
                }
                if (result.getStateName() != null) {
                    if (!fullAddress.isEmpty()) fullAddress.append(", ");
                    fullAddress.append(result.getStateName());
                }
                if (result.getCountryName() != null) {
                    if (!fullAddress.isEmpty()) fullAddress.append(", ");
                    fullAddress.append(result.getCountryName());
                }
                result.setFullAddress(fullAddress.toString());
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * 搜索国家
     */
    private List<LocationSearchResultVO> searchCountries(String keyword) {
//        LambdaQueryWrapper<CountryEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.like(CountryEntity::getName, keyword)
//                   .orderByAsc(CountryEntity::getName)
//                   .last("LIMIT " + MAX_SEARCH_RESULTS); // 限制结果数量
//
//        List<CountryEntity> countries = countryMapper.selectList(queryWrapper);

        Locale languageLocal = UserContextUtil.getLanguageLocal();
        List<CountryEntity> countries = countryService.searchCountryByLanguage(keyword, MAX_SEARCH_RESULTS, languageLocal);
        Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(languageLocal);

        return countries.stream().map(country -> {
            LocationSearchResultVO result = new LocationSearchResultVO();
            result.setId(country.getId());
//            result.setName(country.getName());
            result.setMatchType(LocationMatchTypeEnum.COUNTRY.getCode());
            result.setMatchTypeName(LocationMatchTypeEnum.COUNTRY.getName());
            result.setCountryId(country.getId());
//            result.setCountryName(country.getName());
            result.fillCountryNameByLanguage(functionCountryName, country);
            result.setCurrency(getCurrency(country.getCurrency()));
            result.setLongitude(country.getLongitude() != null ? country.getLongitude().doubleValue() : null);
            result.setLatitude(country.getLatitude() != null ? country.getLatitude().doubleValue() : null);
            
            // 构建完整地址
            result.setFullAddress(country.getName());
            
            return result;
        }).collect(Collectors.toList());
    }
    
    /**
     * 搜索国家并返回该国家下的省市数据
     */
    private List<LocationSearchResultVO> searchCountriesWithStatesAndCities(String keyword) {
//        LambdaQueryWrapper<CountryEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.like(CountryEntity::getName, keyword)
//                   .orderByAsc(CountryEntity::getName)
//                   .last("LIMIT 3"); // 限制国家数量
//
//        List<CountryEntity> countries = countryMapper.selectList(queryWrapper);

        Locale languageLocal = UserContextUtil.getLanguageLocal();
        List<CountryEntity> countries = countryService.searchCountryByLanguage(keyword, MAX_COUNTRY_SEARCH_RESULTS, languageLocal);
        List<LocationSearchResultVO> results = new ArrayList<>();
        Function<CityEntity, String> functionCityName = LanguageLocalUtils.getFunctionCityName(languageLocal);
        Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(languageLocal);
        Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(languageLocal);
        
        for (CountryEntity country : countries) {
            // 获取该国家下的省份
            LambdaQueryWrapper<StateEntity> stateQueryWrapper = new LambdaQueryWrapper<>();
            stateQueryWrapper.eq(StateEntity::getCountryId, country.getId())
                            .orderByAsc(StateEntity::getName)
                            .last("LIMIT " + MAX_STATE_SEARCH_RESULTS); // 每个国家最多返回5个省份
            
            List<StateEntity> states = stateMapper.selectList(stateQueryWrapper);
            
            for (StateEntity state : states) {
                // 获取该省份下的城市
                LambdaQueryWrapper<CityEntity> cityQueryWrapper = new LambdaQueryWrapper<>();
                cityQueryWrapper.eq(CityEntity::getStateId, state.getId())
                               .orderByAsc(CityEntity::getName)
                               .last("LIMIT 2"); // 每个省份最多返回2个城市
                
                List<CityEntity> cities = cityMapper.selectList(cityQueryWrapper);
                
                for (CityEntity city : cities) {
                    LocationSearchResultVO result = new LocationSearchResultVO();
                    result.setId(city.getId());
//                    result.setName(city.getName());
                    result.setMatchType(LocationMatchTypeEnum.CITY.getCode());
                    result.setMatchTypeName(LocationMatchTypeEnum.CITY.getName());
                    result.setCityId(city.getId());
//                    result.setCityName(city.getName());
                    result.fillCityNameByLanguage(functionCityName, city);
                    result.setName(result.getCityName());
                    result.setStateId(state.getId());
//                    result.setStateName(state.getName());
                    result.fillStateNameByLanguage(functionStateName, state);
                    result.setCountryId(country.getId());
//                    result.setCountryName(country.getName());
                    result.fillCountryNameByLanguage(functionCountryName, country);
                    result.setCurrency(getCurrency(country.getCurrency()));
                    result.setLongitude(city.getLongitude() != null ? city.getLongitude().doubleValue() : null);
                    result.setLatitude(city.getLatitude() != null ? city.getLatitude().doubleValue() : null);
                    
                    // 构建完整地址
                    StringBuilder fullAddress = new StringBuilder();
                    if (result.getCityName() != null) {
                        fullAddress.append(result.getCityName());
                    }
                    if (result.getStateName() != null) {
                        if (!fullAddress.isEmpty()) fullAddress.append(", ");
                        fullAddress.append(result.getStateName());
                    }
                    if (result.getCountryName() != null) {
                        if (!fullAddress.isEmpty()) fullAddress.append(", ");
                        fullAddress.append(result.getCountryName());
                    }
                    result.setFullAddress(fullAddress.toString());
                    
                    results.add(result);
                }
            }
        }
        
        return results;
    }
    
    /**
     * 确保最终结果包含城市级别的信息
     * 如果结果中没有城市，则关联出10个城市信息
     * 如果结果中没有省市，则关联出10条省市信息
     */
    private List<LocationSearchResultVO> ensureCityLevelResults(List<LocationSearchResultVO> results, String keyword) {
        if (results.isEmpty()) {
            log.debug("No results found, returning empty list");
            return results;
        }
        
        // 检查结果中是否包含城市级别的信息
        boolean hasCityLevel = results.stream()
                .anyMatch(result -> LocationMatchTypeEnum.CITY.getCode().equals(result.getMatchType()));
        
        if (hasCityLevel) {
            log.debug("Results already contain city level information, returning as is");
            return results;
        }
        
        // 检查结果中是否包含省份级别的信息
        boolean hasStateLevel = results.stream()
                .anyMatch(result -> LocationMatchTypeEnum.STATE.getCode().equals(result.getMatchType()));
        
        if (hasStateLevel) {
            // 如果只有省份信息，关联出10个城市信息
            log.debug("Results contain only state level information, adding cities from matched states");
            List<LocationSearchResultVO> cityResults = new ArrayList<>();
            
            // 获取匹配的省份ID
            List<Long> stateIds = results.stream()
                    .filter(result -> LocationMatchTypeEnum.STATE.getCode().equals(result.getMatchType()))
                    .map(LocationSearchResultVO::getStateId)
                    .distinct()
                    .collect(Collectors.toList());

            Locale languageLocal = UserContextUtil.getLanguageLocal();
            Function<CityEntity, String> functionCityName = LanguageLocalUtils.getFunctionCityName(languageLocal);
            Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(languageLocal);
            Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(languageLocal);

            // 为每个省份获取城市信息
            for (Long stateId : stateIds) {
                LambdaQueryWrapper<CityEntity> cityQueryWrapper = new LambdaQueryWrapper<>();
                cityQueryWrapper.eq(CityEntity::getStateId, stateId)
                               .orderByAsc(CityEntity::getName)
                               .last("LIMIT 10"); // 每个省份最多返回10个城市
                
                List<CityEntity> cities = cityMapper.selectList(cityQueryWrapper);
                
                for (CityEntity city : cities) {
                    LocationSearchResultVO cityResult = new LocationSearchResultVO();
                    cityResult.setId(city.getId());
//                    cityResult.setName(city.getName());
                    cityResult.setMatchType(LocationMatchTypeEnum.CITY.getCode());
                    cityResult.setMatchTypeName(LocationMatchTypeEnum.CITY.getName());
                    cityResult.setCityId(city.getId());
//                    cityResult.setCityName(city.getName());
                    cityResult.fillCityNameByLanguage(functionCityName, city);
                    cityResult.setName(cityResult.getName());
                    cityResult.setStateId(city.getStateId());
                    cityResult.setCountryId(city.getCountryId());
                    cityResult.setLongitude(city.getLongitude() != null ? city.getLongitude().doubleValue() : null);
                    cityResult.setLatitude(city.getLatitude() != null ? city.getLatitude().doubleValue() : null);
                    
                    // 获取省份和国家信息
                    if (city.getStateId() != null) {
                        StateEntity state = stateMapper.selectById(city.getStateId());
                        if (state != null) {
//                            cityResult.setStateName(state.getName());
                            cityResult.fillStateNameByLanguage(functionStateName, state);
                            if (city.getCountryId() != null) {
                                CountryEntity country = countryMapper.selectById(city.getCountryId());
                                if (country != null) {
//                                    cityResult.setCountryName(country.getName());
                                    cityResult.fillCountryNameByLanguage(functionCountryName, country);
                                    cityResult.setCurrency(getCurrency(country.getCurrency()));
                                }
                            }
                        }
                    }
                    
                    // 构建完整地址
                    StringBuilder fullAddress = new StringBuilder();
                    if (cityResult.getCityName() != null) {
                        fullAddress.append(cityResult.getCityName());
                    }
                    if (cityResult.getStateName() != null) {
                        if (!fullAddress.isEmpty()) fullAddress.append(", ");
                        fullAddress.append(cityResult.getStateName());
                    }
                    if (cityResult.getCountryName() != null) {
                        if (!fullAddress.isEmpty()) fullAddress.append(", ");
                        fullAddress.append(cityResult.getCountryName());
                    }
                    cityResult.setFullAddress(fullAddress.toString());
                    
                    cityResults.add(cityResult);
                }
            }
            
            // 限制总数为10个城市
            if (cityResults.size() > 10) {
                cityResults = cityResults.subList(0, 10);
            }
            
            log.debug("Added {} cities from matched states", cityResults.size());
            return cityResults;
        }
        
        // 检查结果中是否包含国家级别的信息
        boolean hasCountryLevel = results.stream()
                .anyMatch(result -> LocationMatchTypeEnum.COUNTRY.getCode().equals(result.getMatchType()));
        
        if (hasCountryLevel) {
            // 如果只有国家信息，关联出10条省市信息
            log.debug("Results contain only country level information, adding cities from matched countries");
            List<LocationSearchResultVO> cityResults = new ArrayList<>();
            
            // 获取匹配的国家ID
            List<Long> countryIds = results.stream()
                    .filter(result -> LocationMatchTypeEnum.COUNTRY.getCode().equals(result.getMatchType()))
                    .map(LocationSearchResultVO::getCountryId)
                    .distinct()
                    .collect(Collectors.toList());

            Locale languageLocal = UserContextUtil.getLanguageLocal();
            Function<CityEntity, String> functionCityName = LanguageLocalUtils.getFunctionCityName(languageLocal);
            Function<StateEntity, String> functionStateName = LanguageLocalUtils.getFunctionStateName(languageLocal);
            Function<CountryEntity, String> functionCountryName = LanguageLocalUtils.getFunctionCountryName(languageLocal);

            // 为每个国家获取省市信息
            for (Long countryId : countryIds) {
                // 获取该国家下的省份
                LambdaQueryWrapper<StateEntity> stateQueryWrapper = new LambdaQueryWrapper<>();
                stateQueryWrapper.eq(StateEntity::getCountryId, countryId)
                                .orderByAsc(StateEntity::getName)
                                .last("LIMIT 5"); // 每个国家最多返回5个省份
                
                List<StateEntity> states = stateMapper.selectList(stateQueryWrapper);
                
                for (StateEntity state : states) {
                    // 获取该省份下的城市
                    LambdaQueryWrapper<CityEntity> cityQueryWrapper = new LambdaQueryWrapper<>();
                    cityQueryWrapper.eq(CityEntity::getStateId, state.getId())
                                   .orderByAsc(CityEntity::getName)
                                   .last("LIMIT 2"); // 每个省份最多返回2个城市
                    
                    List<CityEntity> cities = cityMapper.selectList(cityQueryWrapper);
                    
                    for (CityEntity city : cities) {
                        LocationSearchResultVO cityResult = new LocationSearchResultVO();
                        cityResult.setId(city.getId());
//                        cityResult.setName(city.getName());
                        cityResult.setMatchType(LocationMatchTypeEnum.CITY.getCode());
                        cityResult.setMatchTypeName(LocationMatchTypeEnum.CITY.getName());
                        cityResult.setCityId(city.getId());
//                        cityResult.setCityName(city.getName());
                        cityResult.fillCityNameByLanguage(functionCityName, city);
                        cityResult.setName(cityResult.getName());
                        cityResult.setStateId(state.getId());
//                        cityResult.setStateName(state.getName());
                        cityResult.fillStateNameByLanguage(functionStateName, state);
                        cityResult.setCountryId(countryId);
                        cityResult.setLongitude(city.getLongitude() != null ? city.getLongitude().doubleValue() : null);
                        cityResult.setLatitude(city.getLatitude() != null ? city.getLatitude().doubleValue() : null);
                        
                        // 获取国家信息
                        CountryEntity country = countryMapper.selectById(countryId);
                        if (country != null) {
//                            cityResult.setCountryName(country.getName());
                            cityResult.fillCountryNameByLanguage(functionCountryName, country);
                            cityResult.setCurrency(getCurrency(country.getCurrency()));
                        }
                        
                        // 构建完整地址
                        StringBuilder fullAddress = new StringBuilder();
                        if (cityResult.getCityName() != null) {
                            fullAddress.append(cityResult.getCityName());
                        }
                        if (cityResult.getStateName() != null) {
                            if (!fullAddress.isEmpty()) fullAddress.append(", ");
                            fullAddress.append(cityResult.getStateName());
                        }
                        if (cityResult.getCountryName() != null) {
                            if (!fullAddress.isEmpty()) fullAddress.append(", ");
                            fullAddress.append(cityResult.getCountryName());
                        }
                        cityResult.setFullAddress(fullAddress.toString());
                        
                        cityResults.add(cityResult);
                    }
                }
            }
            
            // 限制总数为10个城市
            if (cityResults.size() > 10) {
                cityResults = cityResults.subList(0, 10);
            }
            
            log.debug("Added {} cities from matched countries", cityResults.size());
            return cityResults;
        }
        
        log.debug("No additional city level information needed");
        return results;
    }


    private Long getCurrency(String code){
        if (Objects.isNull(dictionaryMap.get(DictionaryEnum.REPORT.getName()))) {
            return 5l;
        } else if (Objects.isNull(dictionaryMap.get(DictionaryEnum.REPORT.getName()).get(code))){
            return dictionaryMap.get(DictionaryEnum.REPORT.getName()).get("USD").getId();
        }
        return dictionaryMap.get(DictionaryEnum.REPORT.getName()).get(code).getId();
    }

    @Override
    public Pager<LocationSearchResultVO> fuzzySearchLocationByGoogleMap(LocationSearchDTO searchDTO) {
        log.info("Starting Google Map fuzzy search for location, keyword: {}", searchDTO.getKeyword());
        
        try {
            // 参数验证
            if (StringUtils.isBlank(searchDTO.getKeyword())) {
                log.warn("Search keyword is blank, returning empty result");
                return Pager.buildEmpty();
            }
            
            // 获取用户语言
            Locale locale = UserContextUtil.getLanguageLocal();
            String languageCode = getGoogleMapLanguageCode(locale);
            log.debug("User language code: {}", languageCode);
            
            // 构建请求 URL
            String url = buildGoogleMapUrl(searchDTO.getKeyword(), languageCode);
            log.debug("Google Map API URL: {}", url);
            
            // 调用 Google Map API
            String responseJson = httpClient5Service.doGet(url, null);
            if (StringUtils.isBlank(responseJson)) {
                log.warn("Google Map API returned empty response");
                return Pager.buildEmpty();
            }
            
            log.debug("Google Map API response: {}", responseJson);
            
            // 解析响应
            GoogleMapAutocompleteResponseDTO response = JsonUtils.toObject(
                    responseJson, 
                    GoogleMapAutocompleteResponseDTO.class
            );
            
            // 检查响应状态
            if (response == null || !"OK".equals(response.getStatus())) {
                log.warn("Google Map API returned non-OK status: {}", 
                        response != null ? response.getStatus() : "null");
                return Pager.buildEmpty();
            }
            
            // 检查预测结果
            if (CollectionUtils.isEmpty(response.getPredictions())) {
                log.info("Google Map API returned no predictions");
                return Pager.buildEmpty();
            }
            
            // 并行调用 Place Details API 获取每个 placeId 的详细信息
            // 使用指定的线程池 locationTaskExecutor 而不是 ForkJoinPool.commonPool()
            List<CompletableFuture<LocationSearchResultVO>> futures =
                    response.getPredictions().stream()
                    .map(prediction -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return fetchPlaceDetailsAndBuildResult(prediction);
                        } catch (Exception e) {
                            log.error("Error fetching place details for placeId: {}", 
                                    prediction.getPlaceId(), e);
                            // 返回基本信息，不包含 countryId
                            LocationSearchResultVO result = new LocationSearchResultVO();
                            result.setPlaceId(prediction.getPlaceId());
                            result.setPlaceName(prediction.getDescription());
                            return result;
                        }
                    }, locationTaskExecutor.getThreadPoolExecutor()))
                    .toList();
            
            // 等待所有异步任务完成
            CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            ).join();
            
            // 收集结果
            List<LocationSearchResultVO> results = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // 构建分页结果（Google Map API 返回结果有限，无需真实分页）
            int totalCount = results.size();
            Pager<LocationSearchResultVO> pager = new Pager<>();
            pager.setPageIndex(1);
            pager.setPageSize(totalCount);
            pager.setTotalCount(totalCount);
            pager.setCurrentPageRecords(results);
            
            log.info("Google Map fuzzy search completed, total found: {}", totalCount);
            return pager;
            
        } catch (Exception e) {
            log.error("Error occurred during Google Map fuzzy search for keyword: {}", 
                    searchDTO.getKeyword(), e);
            return Pager.buildEmpty();
        }
    }
    
    /**
     * 获取 Place Details 并构建结果对象
     * 
     * @param prediction 预测结果
     * @return LocationSearchResultVO
     */
    private LocationSearchResultVO fetchPlaceDetailsAndBuildResult(PredictionDTO prediction) {
        LocationSearchResultVO result = new LocationSearchResultVO();
        result.setPlaceId(prediction.getPlaceId());
        result.setPlaceName(prediction.getDescription());
        result.setFullAddress(prediction.getDescription());
        
        try {
            // 构建 Place Details API URL (language=en, fields=address_components)
            String detailsUrl = buildPlaceDetailsUrl(prediction.getPlaceId());
            log.debug("Fetching place details from: {}", detailsUrl);
            
            // 调用 Place Details API
            String detailsJson = httpClient5Service.doGet(detailsUrl, null);
            if (StringUtils.isBlank(detailsJson)) {
                log.warn("Place Details API returned empty response for placeId: {}", 
                        prediction.getPlaceId());
                return result;
            }
            
            // 解析响应
            GoogleMapPlaceDetailsResponseDTO detailsResponse = JsonUtils.toObject(detailsJson, GoogleMapPlaceDetailsResponseDTO.class);
            
            // 检查响应状态
            if (detailsResponse == null || !"OK".equals(detailsResponse.getStatus())) {
                log.warn("Place Details API returned non-OK status: {} for placeId: {}", 
                        detailsResponse != null ? detailsResponse.getStatus() : "null",
                        prediction.getPlaceId());
                return result;
            }
            
            // 提取 address_components
            if (detailsResponse.getResult() != null && 
                    CollectionUtils.isNotEmpty(detailsResponse.getResult().getAddressComponents())) {
                
                // 查找 country 类型的地址组件
                String countryIso2 = extractCountryIso2(
                        detailsResponse.getResult().getAddressComponents());
                
                if (StringUtils.isNotBlank(countryIso2)) {
                    // 根据 iso2 查询 r_countries 表获取 countryId
                    Long countryId = getCountryIdByIso2(countryIso2);
                    if (countryId != null) {
                        result.setCountryId(countryId);
                        log.debug("Found countryId: {} for iso2: {} and placeId: {}", 
                                countryId, countryIso2, prediction.getPlaceId());
                    } else {
                        log.warn("Country not found in database for iso2: {} and placeId: {}", 
                                countryIso2, prediction.getPlaceId());
                    }
                } else {
                    log.warn("Country iso2 not found in address_components for placeId: {}", 
                            prediction.getPlaceId());
                }
            }
            
            // 提取经纬度信息
            if (detailsResponse.getResult() != null && 
                    detailsResponse.getResult().getGeometry() != null &&
                    detailsResponse.getResult().getGeometry().getLocation() != null) {
                
                Double lat = detailsResponse.getResult().getGeometry().getLocation().getLat();
                Double lng = detailsResponse.getResult().getGeometry().getLocation().getLng();
                
                if (lat != null && lng != null) {
                    result.setLatitude(lat);
                    result.setLongitude(lng);
                    log.debug("Extracted coordinates - lat: {}, lng: {} for placeId: {}", 
                            lat, lng, prediction.getPlaceId());
                } else {
                    log.warn("Latitude or longitude is null for placeId: {}", 
                            prediction.getPlaceId());
                }
            } else {
                log.warn("Geometry or location not found in response for placeId: {}", 
                        prediction.getPlaceId());
            }
            
        } catch (Exception e) {
            log.error("Error processing place details for placeId: {}", 
                    prediction.getPlaceId(), e);
        }
        
        return result;
    }
    
    /**
     * 从地址组件中提取国家的 ISO2 代码
     * 
     * @param addressComponents 地址组件列表
     * @return 国家 ISO2 代码（short_name），如果未找到返回 null
     */
    private String extractCountryIso2(List<com.item.dto.googlemap.AddressComponentDTO> addressComponents) {
        if (CollectionUtils.isEmpty(addressComponents)) {
            return null;
        }
        
        // 查找 types 包含 "country" 和 "political" 的地址组件
        return addressComponents.stream()
                .filter(component -> component.getTypes() != null && 
                        component.getTypes().contains("country") && 
                        component.getTypes().contains("political"))
                .map(com.item.dto.googlemap.AddressComponentDTO::getShortName)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据 ISO2 代码查询国家 ID
     * 
     * @param iso2 国家 ISO2 代码
     * @return 国家 ID，如果未找到返回 null
     */
    private Long getCountryIdByIso2(String iso2) {
        if (StringUtils.isBlank(iso2)) {
            return null;
        }
        
        try {
            LambdaQueryWrapper<CountryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CountryEntity::getIso2, iso2);
            CountryEntity country = countryMapper.selectOne(queryWrapper);
            return country != null ? country.getId() : null;
        } catch (Exception e) {
            log.error("Error querying country by iso2: {}", iso2, e);
            return null;
        }
    }
    
    /**
     * 构建 Place Details API URL
     * 
     * @param placeId Google Map Place ID
     * @return Place Details API URL
     */
    private String buildPlaceDetailsUrl(String placeId) {
        try {
            // URL 编码 placeId（虽然通常不需要，但为了安全起见）
            String encodedPlaceId = URLEncoder.encode(placeId, StandardCharsets.UTF_8);
            
            // 构建 URL: language=en, fields=address_components
            return String.format("%s?place_id=%s&fields=address_components,geometry&language=en&key=%s",
                    googleMapConfig.getPlaceDetailsUrl(),
                    encodedPlaceId,
                    googleMapConfig.getApiKey());
        } catch (Exception e) {
            log.error("Error building Place Details URL for placeId: {}", placeId, e);
            throw new RuntimeException("Failed to build Place Details URL", e);
        }
    }
    
    /**
     * 将 Locale 转换为 Google Map API 支持的语言代码
     * 
     * @param locale 用户语言环境
     * @return Google Map API 语言代码
     */
    private String getGoogleMapLanguageCode(Locale locale) {
        if (locale == null) {
            return "en"; // 默认英文
        }
        
        // 直接使用 Locale 的 language 部分
        // zh-CN -> zh, en-US -> en, ja-JP -> ja, es-ES -> es
        return locale.getLanguage();
    }
    
    /**
     * 构建 Google Map API 请求 URL
     * 
     * @param keyword 搜索关键词
     * @param languageCode 语言代码
     * @return 完整的 API URL
     */
    private String buildGoogleMapUrl(String keyword, String languageCode) {
        try {
            // URL 编码关键词
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            
            // 构建 URL
            return String.format("%s?input=%s&language=%s&key=%s",
                    googleMapConfig.getAutocompleteUrl(),
                    encodedKeyword,
                    languageCode,
                    googleMapConfig.getApiKey());
        } catch (Exception e) {
            log.error("Error building Google Map URL", e);
            throw new RuntimeException("Failed to build Google Map URL", e);
        }
    }

    @Override
    public Map<String, String> listIdNameMapByPlaceIds(List<String> placeIds) {
        if (CollectionUtils.isEmpty(placeIds)) {
            return new HashMap<>();
        }
        
        log.info("listIdNameMapByPlaceIds placeIds {}", placeIds);
        
        List<PlaceEntity> placeEntities = placeService.listByPlaceIds(placeIds);
        if (CollectionUtils.isEmpty(placeEntities)) {
            return new HashMap<>();
        }
        
        return placeEntities.stream()
                .collect(Collectors.toMap(
                        PlaceEntity::getPlaceId, 
                        PlaceEntity::getDescription, 
                        (d1, d2) -> d1  // 如果有重复的 placeId，保留第一个
                ));
    }

    @Override
    public LocationValDTO buildLocationStringByPlaceId(String placeId) {
        if (StringUtils.isBlank(placeId)) {
            log.warn("buildLocationStringByPlaceId: placeId is blank");
            return null;
        }
        
        log.info("buildLocationStringByPlaceId placeId: {}", placeId);
        
        try {
            // 从 r_places 表查询 placeId 对应的记录
            List<PlaceEntity> placeEntities = placeService.listByPlaceId(placeId);
            if (CollectionUtils.isEmpty(placeEntities)) {
                log.warn("buildLocationStringByPlaceId: no place found for placeId: {}", placeId);
                return null;
            }
            
            // 获取第一条记录（通常英语优先级返回）
            PlaceEntity placeEntity = placeEntities.stream().filter((item) -> "en".equalsIgnoreCase(item.getLanguage())).findFirst().orElse(null);
            if (placeEntity == null) {
                log.warn("buildLocationStringByPlaceId: no place found for placeId: {}", placeId);
                return null;
            }
            String addressComponentsJson = placeEntity.getAddressComponents();
            
            if (StringUtils.isBlank(addressComponentsJson)) {
                log.warn("buildLocationStringByPlaceId: address_components is blank for placeId: {}", placeId);
                return null;
            }
            
            // 解析 address_components JSON
            List<com.item.dto.googlemap.AddressComponentDTO> addressComponents = 
                    JsonUtils.toList(addressComponentsJson, com.item.dto.googlemap.AddressComponentDTO.class);
            
            if (CollectionUtils.isEmpty(addressComponents)) {
                log.warn("buildLocationStringByPlaceId: failed to parse address_components for placeId: {}", placeId);
                return null;
            }
            
            // 提取 cityName, stateName, countryName
            String cityName = extractLocationComponent(addressComponents, "locality");
            if (StringUtils.isBlank(cityName)) {
                cityName = extractLocationComponent(addressComponents, "sublocality");
            }
            String stateName = extractLocationComponent(addressComponents, "administrative_area_level_1");
            String countryName = extractLocationComponent(addressComponents, "country");

            LocationValDTO location = new LocationValDTO();
            location.setCityName(cityName);
            location.setStateName(stateName);
            location.setCountryName(countryName);

            log.info("buildLocationStringByPlaceId result: {} for placeId: {}", location, placeId);
            return location;
            
        } catch (Exception e) {
            log.error("buildLocationStringByPlaceId error for placeId: {}", placeId, e);
            return null;
        }
    }
    
    /**
     * 从地址组件列表中提取指定类型的位置信息
     * 
     * @param addressComponents 地址组件列表
     * @param targetType 目标类型（locality=城市, administrative_area_level_1=省/州, country=国家）
     * @return 位置名称（long_name），如果未找到返回空字符串
     */
    private String extractLocationComponent(List<com.item.dto.googlemap.AddressComponentDTO> addressComponents, 
                                           String targetType) {
        if (CollectionUtils.isEmpty(addressComponents) || StringUtils.isBlank(targetType)) {
            return "";
        }
        
        return addressComponents.stream()
                .filter(component -> component.getTypes() != null && 
                        component.getTypes().contains(targetType))
                .map(com.item.dto.googlemap.AddressComponentDTO::getLongName)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("");
    }
}