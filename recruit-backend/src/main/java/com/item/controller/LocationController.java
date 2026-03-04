package com.item.controller;

import com.item.convert.LocationConverter;
import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.LanguageDTO;
import com.item.dto.LocationSearchDTO;
import com.item.dto.StateDTO;
import com.item.framework.http.Pager;
import com.item.service.LocationService;
import com.item.vo.CityVO;
import com.item.vo.CountryVO;
import com.item.vo.LanguageVO;
import com.item.vo.LocationSearchResultVO;
import com.item.vo.StateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final LocationConverter locationConverter;

    /**
     * 查询所有语言列表
     * @return
     */
    @GetMapping("/languages")
    public List<LanguageVO> getAllLanguages() {
        List<LanguageDTO> list = locationService.getAllLanguages();
        return LocationConverter.INSTANCE.toLanguageVOList(list);
    }

    /**
     * 查询所有国家列表
     * @return
     */
    @GetMapping("/countries")
    public List<CountryVO> getAllCountries() {
        List<CountryDTO> list = locationService.getAllCountries();
        return LocationConverter.INSTANCE.toCountryVOList(list);
    }

    /**
     * 根据国家id查询 所有省列表
     * @param countryId 国家id
     * @return
     */
    @GetMapping("/states")
    public List<StateVO> getStatesByCountryId(@RequestParam Long countryId) {
        List<StateDTO> list = locationService.getStatesByCountryId(countryId);
        return locationConverter.toStateVOList(list);
    }

    /**
     * 根据省id 获取所有市列表
     * @param stateId   省id
     * @return
     */
    @GetMapping("/cities")
    public List<CityVO> getCitiesByStateId(@RequestParam Long stateId) {
        List<CityDTO> list = locationService.getCitiesByStateId(stateId);
        return locationConverter.toCityVOList(list);
    }

    /**
     * 根据id获取国家、省、市
     * @param countryId 国家id
     * @param stateId   省id
     * @param cityId    市id
     * @return
     */
    @GetMapping("/detail")
    public Object getLocationDetail(@RequestParam(required = false) Long countryId,
                                            @RequestParam(required = false) Long stateId,
                                            @RequestParam(required = false) Long cityId) {
        Object dto = locationService.getLocationDetail(countryId, stateId, cityId);
        if (dto instanceof CountryDTO) {
            return locationConverter.toCountryVO((CountryDTO) dto);
        } else if (dto instanceof StateDTO) {
            return locationConverter.toStateVO((StateDTO) dto);
        } else if (dto instanceof CityDTO) {
            return locationConverter.toCityVO((CityDTO) dto);
        }
        return null;
    }

    /**
     * 地理位置模糊搜索
     * 按优先级搜索：城市 > 省份 > 国家
     * 支持分页查询和类型筛选
     * 
     * @param keyword 搜索关键词
     * @param page 页码，从0开始，默认为0
     * @param size 每页大小，默认为10
     * @param cityOnly 是否只搜索城市，默认为false
     * @param stateOnly 是否只搜索省份，默认为false
     * @param countryOnly 是否只搜索国家，默认为false
     * @return 分页的搜索结果
     */
    @GetMapping("/search")
    public Pager<LocationSearchResultVO> fuzzySearchLocation(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "false") Boolean cityOnly,
            @RequestParam(defaultValue = "false") Boolean stateOnly,
            @RequestParam(defaultValue = "false") Boolean countryOnly) {
        
        // 构建搜索参数
        LocationSearchDTO searchDTO = new LocationSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setPage(page-1);
        searchDTO.setSize(size);
        searchDTO.setCityOnly(cityOnly);
        searchDTO.setStateOnly(stateOnly);
        searchDTO.setCountryOnly(countryOnly);

        // 调用 Google Map 服务进行地理位置搜索
        return locationService.fuzzySearchLocationByGoogleMap(searchDTO);
    }
} 