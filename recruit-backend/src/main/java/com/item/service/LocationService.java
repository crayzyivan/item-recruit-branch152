package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.CountryIsoDTO;
import com.item.dto.FullLocationDTO;
import com.item.dto.LanguageDTO;
import com.item.dto.LocationDto;
import com.item.dto.LocationSearchDTO;
import com.item.dto.StateDTO;
import com.item.dto.job.LocationValDTO;
import com.item.entity.LocationEntity;
import com.item.framework.http.Pager;
import com.item.vo.LocationSearchResultVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
public interface LocationService extends IService<LocationEntity> {
    /**
     * 未使用的接口
     * @return
     */
    @Deprecated
    List<LocationDto> findAll();

    /**
     * 获取所有国家列表
     * @return
     */
    List<CountryDTO> getAllCountries();

    /**
     * 获取所有语言列表
     * @return
     */
    List<LanguageDTO> getAllLanguages();

    /**
     * 根据国家id查询 所有省列表
     * @param countryId 国家id
     * @return
     */
    List<StateDTO> getStatesByCountryId(Long countryId);
    /**
     * 根据省id 获取所有市列表
     * @param stateId   省id
     * @return
     */
    List<CityDTO> getCitiesByStateId(Long stateId);
    /**
     * 根据id获取国家、省、市
     * @param countryId 国家id
     * @param stateId   省id
     * @param cityId    市id
     * @return
     */
    Object getLocationDetail(Long countryId, Long stateId, Long cityId);

    /**
     * 根据市id获取国家信息
     * @param cityId 市id
     * @return 国家信息
     */
    CountryDTO getCountryByCityId(Long cityId);

    StateDTO getStateByCityId(Long cityId);

    /**
     * 根据国家id列表批量获取国家信息
     * @param countryIds
     * @return
     */
    List<CountryDTO> listByCountryIds(List<Long> countryIds);

    /**
     * 根据省id列表批量获取省信息
     * @param stateIds
     * @return
     */
    List<StateDTO> listByStateIds(List<Long> stateIds);

    /**
     * 根据市id列表批量获取市信息
     * @param cityIds
     * @return
     */
    List<CityDTO> listByCityIds(List<Long> cityIds);

    /**
     * 根据cityId获取location全称(city,state,country)
     * @param cityId
     * @return
     */
    FullLocationDTO getFullLocation(Long cityId);

    /**
     * 查询全部国家
     * @return
     */
    List<CountryIsoDTO> selectAllCountryIso();

    /**
     * 地理位置模糊搜索
     * 按优先级搜索：城市 > 省份 > 国家
     * 支持分页查询和类型筛选
     * 
     * @param searchDTO 搜索参数，包含关键词、分页信息、类型筛选等
     * @return 分页的搜索结果，包含匹配的地理位置信息
     */
    Pager<LocationSearchResultVO> fuzzySearchLocation(LocationSearchDTO searchDTO);

    /**
     * 通过 Google Map API 进行地理位置模糊搜索
     * 
     * @param searchDTO 搜索参数，包含关键词、分页信息等
     * @return 分页的搜索结果
     */
    Pager<LocationSearchResultVO> fuzzySearchLocationByGoogleMap(LocationSearchDTO searchDTO);


    CountryDTO getCountryById(Long id);

    /**
     * 根据国家id列表批量获取国家映射
     * @param countryIds
     * @return
     */
    Map<Long, String> listIdNameMapByCountryIds(List<Long> countryIds);

    /**
     * 根据省id列表批量获取省映射
     * @param stateIds
     * @return
     */
    Map<Long, String> listIdNameMapByStateIds(List<Long> stateIds);

    /**
     * 根据市id列表批量获取市映射
     *
     * @param cityIds
     * @return
     */
    Map<Long, String> listIdNameMapByCityIds(List<Long> cityIds);

    /**
     * 根据国家id列表批量获取国家信息
     * @param countryIds
     * @return
     */
    List<CountryDTO> listEnCountryByCountryIds(List<Long> countryIds);

    /**
     * 根据省id列表批量获取省信息
     * @param stateIds
     * @return
     */
    List<StateDTO> listEnStateByStateIds(List<Long> stateIds);

    /**
     * 根据市id列表批量获取市信息
     * @param cityIds
     * @return
     */
    List<CityDTO> listEnCityByCityIds(List<Long> cityIds);

    /**
     *
     * @param placeIds
     * @return
     */
    Map<String, String> listIdNameMapByPlaceIds(List<String> placeIds);

    /**
     * 根据 placeId 构建位置字符串（cityName, stateName, countryName）
     * 从 r_places 表获取 address_components 并解析
     * 
     * @param placeId Google Map Place ID
     * @return 格式化的位置字符串，如 "Beijing, Beijing Shi, China"，如果未找到返回空字符串
     */
    LocationValDTO buildLocationStringByPlaceId(String placeId);

}
