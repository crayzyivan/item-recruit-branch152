package com.item.convert;

import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.CountryIsoDTO;
import com.item.dto.LanguageDTO;
import com.item.dto.LocationDto;
import com.item.dto.StateDTO;
import com.item.dto.cache.CityCacheDTO;
import com.item.dto.cache.CountryCacheDTO;
import com.item.dto.cache.StateCacheDTO;
import com.item.entity.CityEntity;
import com.item.entity.CountryEntity;
import com.item.entity.LanguageEntity;
import com.item.entity.LocationEntity;
import com.item.entity.StateEntity;
import com.item.vo.CityVO;
import com.item.vo.CountryVO;
import com.item.vo.LanguageVO;
import com.item.vo.LocationVo;
import com.item.vo.StateVO;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.function.Function;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface LocationConverter {
    LocationConverter INSTANCE = Mappers.getMapper(LocationConverter.class);

    LocationVo convertToVo(LocationDto dto);
    List<LocationVo> convertToListVo(List<LocationDto> dtoList);
    List<LocationDto> convertToListDto(List<LocationEntity> entityList);

    CountryVO toCountryVO(CountryDTO countryDTO);
    StateVO toStateVO(StateDTO stateDTO);
    CityVO toCityVO(CityDTO cityDTO);
    @Mapping(source = "iso2", target = "isoCode")
    CountryDTO toCountryDTO(CountryEntity countryEntity);
    StateDTO toStateDTO(StateEntity stateEntity);
    CityDTO toCityDTO(CityEntity cityEntity);

    List<CountryVO> toCountryVOList(List<CountryDTO> list);
    List<StateVO> toStateVOList(List<StateDTO> list);
    List<CityVO> toCityVOList(List<CityDTO> list);
    List<LanguageVO> toLanguageVOList(List<LanguageDTO> list);


    List<CountryDTO> toCountryDTOList(List<CountryEntity> list);
    List<StateDTO> toStateDTOList(List<StateEntity> list);
    List<CityDTO> toCityDTOList(List<CityEntity> list);

    List<CountryIsoDTO> toCountryIsoDTOList(List<CountryEntity> list);

    List<CountryCacheDTO> convert2Caches(List<CountryEntity> countryEntityList);
    @Mapping(source = "iso2", target = "isoCode")
    CountryCacheDTO convert2Cache(CountryEntity countryEntity);

    List<StateCacheDTO> convert2StateCaches(List<StateEntity> stateEntityList);
    StateCacheDTO convert2StateCache(StateEntity stateEntity);

    List<CityCacheDTO> convert2CityCaches(List<CityEntity> cityEntityList);
    CityCacheDTO convert2CityCache(CityEntity cityEntity);

    @IterableMapping(elementTargetType = CountryDTO.class, qualifiedByName = "cacheToDtoWithLanguage")
    List<CountryDTO> toCountryDTOs(List<CountryCacheDTO> list, @Context Function<CountryCacheDTO, String> funCountryCacheName);

    @Mapping(target = "name", source = ".", qualifiedByName = "getNameByLanguage")
    @Named("cacheToDtoWithLanguage")
    CountryDTO toCountryDTO(CountryCacheDTO countryEntity, @Context Function<CountryCacheDTO, String> funCountryCacheName);

    @IterableMapping(elementTargetType = StateDTO.class, qualifiedByName = "cacheStateToDtoWithLanguage")
    List<StateDTO> toStateDTOs(List<StateCacheDTO> list, @Context Function<StateCacheDTO, String> funStateCacheName);
    @Mapping(target = "name", source = ".", qualifiedByName = "getStateNameByLanguage")
    @Named("cacheStateToDtoWithLanguage")
    StateDTO to2StateDTO(StateCacheDTO stateEntity, @Context Function<StateCacheDTO, String> funStateCacheName);

    @IterableMapping(elementTargetType = CityDTO.class, qualifiedByName = "cacheCityToDtoWithLanguage")
    List<CityDTO> toCityDTOs(List<CityCacheDTO> list, @Context Function<CityCacheDTO, String> funCityCacheName);
    @Mapping(target = "name", source = ".", qualifiedByName = "getCityNameByLanguage")
    @Named("cacheCityToDtoWithLanguage")
    CityDTO to2CityDTO(CityCacheDTO cityEntity, @Context Function<CityCacheDTO, String> funCityCacheName);


    @Named("getNameByLanguage")
    default String getNameByLanguage(CountryCacheDTO entity, @Context Function<CountryCacheDTO, String> funCountryCacheName) {
        if (entity == null) {
            return null;
        }
        return funCountryCacheName.apply(entity);
    }

    @Named("getStateNameByLanguage")
    default String getStateNameByLanguage(StateCacheDTO entity, @Context Function<StateCacheDTO, String> funStateCacheName) {
        if (entity == null) {
            return null;
        }
        return funStateCacheName.apply(entity);
    }

    @Named("getCityNameByLanguage")
    default String getCityNameByLanguage(CityCacheDTO entity, @Context Function<CityCacheDTO, String> funCityCacheName) {
        if (entity == null) {
            return null;
        }
        return funCityCacheName.apply(entity);
    }



    @IterableMapping(elementTargetType = CountryDTO.class, qualifiedByName = "entityToDtoWithLanguage")
    List<CountryDTO> entityToCountryDTOs(List<CountryEntity> list, @Context Function<CountryEntity, String> funCountryName);

    @Mapping(target = "name", source = ".", qualifiedByName = "getEntityNameByLanguage")
    @Named("entityToDtoWithLanguage")
    CountryDTO entityToCountryDTO(CountryEntity countryEntity, @Context Function<CountryEntity, String> funCountryName);

    @IterableMapping(elementTargetType = StateDTO.class, qualifiedByName = "entityStateToDtoWithLanguage")
    List<StateDTO> entityToStateDTOs(List<StateEntity> list, @Context Function<StateEntity, String> funStateName);
    @Mapping(target = "name", source = ".", qualifiedByName = "getStateEntityNameByLanguage")
    @Named("entityStateToDtoWithLanguage")
    StateDTO entityToStateDTO(StateEntity stateEntity, @Context Function<StateEntity, String> funStateName);

    @IterableMapping(elementTargetType = CityDTO.class, qualifiedByName = "entityCityToDtoWithLanguage")
    List<CityDTO> entityToCityDTOs(List<CityEntity> list, @Context Function<CityEntity, String> funCityName);
    @Mapping(target = "name", source = ".", qualifiedByName = "getCityEntityNameByLanguage")
    @Named("entityCityToDtoWithLanguage")
    CityDTO entityToCityDTO(CityEntity cityEntity, @Context Function<CityEntity, String> funCityName);


    @Named("getEntityNameByLanguage")
    default String getEntityNameByLanguage(CountryEntity entity, @Context Function<CountryEntity, String> funCountryName) {
        if (entity == null) {
            return null;
        }
        return funCountryName.apply(entity);
    }

    @Named("getStateEntityNameByLanguage")
    default String getStateEntityNameByLanguage(StateEntity entity, @Context Function<StateEntity, String> funStateName) {
        if (entity == null) {
            return null;
        }
        return funStateName.apply(entity);
    }

    @Named("getCityEntityNameByLanguage")
    default String getCityEntityNameByLanguage(CityEntity entity, @Context Function<CityEntity, String> funCityName) {
        if (entity == null) {
            return null;
        }
        return funCityName.apply(entity);
    }

    @IterableMapping(elementTargetType = LanguageDTO.class, qualifiedByName = "entityLanguageToDtoWithLanguage")
    List<LanguageDTO> toLanguageDTOList(List<LanguageEntity> list, @Context boolean chinese);

    @Mapping(target = "name", source = ".", qualifiedByName = "getLanguageNameByLanguage")
    @Named("entityLanguageToDtoWithLanguage")
    LanguageDTO entityToLanguageDTO(LanguageEntity languageEntity, @Context boolean chinese);

    @Named("getLanguageNameByLanguage")
    default String getLanguageNameByLanguage(LanguageEntity entity, @Context boolean isChinese) {
        if (entity == null) {
            return null;
        }
        if (isChinese) {
            return entity.getNameZh();
        }
        return entity.getNameEn();
    }
}
