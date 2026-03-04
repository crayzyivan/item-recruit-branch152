package com.item.convert;

import com.item.dto.DictionaryDTO;
import com.item.dto.cache.DictionaryCacheDTO;
import com.item.entity.DictionaryEntity;
import com.item.framework.constant.DictionaryEnum;
import com.item.util.LanguageLocalUtils;
import com.item.vo.DictionaryVO;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Mapper(componentModel = "spring")
public interface DictionaryConverter {
    DictionaryConverter INSTANCE = Mappers.getMapper(DictionaryConverter.class);

    DictionaryEntity dtoToEntity(DictionaryDTO dto);
    DictionaryDTO entityToDto(DictionaryEntity entity);
    DictionaryVO entityToVo(DictionaryEntity entity);
    DictionaryEntity voToEntity(DictionaryVO vo);
    List<DictionaryVO> dtolistToVolist(List<DictionaryDTO> dtos);
    List<DictionaryDTO> entityToDtos(List<DictionaryEntity> entities);

    List<DictionaryCacheDTO> convert2Caches(List<DictionaryEntity> list);
    DictionaryCacheDTO convert2Cache(DictionaryEntity entity);

    @IterableMapping(elementTargetType = DictionaryDTO.class, qualifiedByName = "cacheToDtoWithLanguage")
    List<DictionaryDTO> cacheToDtos(List<DictionaryCacheDTO> entities, @Context Locale locale);

    @Mapping(target = "value", source = ".", qualifiedByName = "getValueByLanguageCache")
    @Mapping(target = "remark", source = ".", qualifiedByName = "getRemarkByLanguageCache")
    @Named("cacheToDtoWithLanguage")
    DictionaryDTO entityToDto(DictionaryCacheDTO entity, @Context Locale locale);

    @Named("getValueByLanguageCache")
    default String getValueByLanguageCache(DictionaryCacheDTO entity, @Context Locale locale) {
        if (entity == null) {
            return null;
        }
        if (DictionaryEnum.REPORT.getName().equals(entity.getType())) {
            return entity.getValue();
        }
        Function<DictionaryCacheDTO, String> functionDictionaryValue = LanguageLocalUtils.getFunctionDictionaryCacheValue(locale);
        return functionDictionaryValue.apply(entity);
    }

    @Named("getRemarkByLanguageCache")
    default String getRemarkByLanguageCache(DictionaryCacheDTO entity, @Context Locale locale) {
        if (entity == null) {
            return null;
        }
        if (locale==null){
            return entity.getRemark();
        }
        if (!DictionaryEnum.REPORT.getName().equals(entity.getType())) {
            return entity.getRemark();
        }
        boolean unSupportLocale = LanguageLocalUtils.isUnSupportLocale(locale);
        if (unSupportLocale) {
            return entity.getRemark();
        }
        if (LanguageLocalUtils.isEnglishLanguage(locale)) {
            return entity.getRemark();
        }
        Function<DictionaryCacheDTO, String> functionDictionaryValue = LanguageLocalUtils.getFunctionDictionaryCacheValue(locale);
        return functionDictionaryValue.apply(entity);
    }


    @IterableMapping(elementTargetType = DictionaryDTO.class, qualifiedByName = "entityToDtoWithLanguage")
    List<DictionaryDTO> entityToDtos(List<DictionaryEntity> entities, @Context Locale locale);

    @Mapping(target = "value", source = ".", qualifiedByName = "getValueByLanguage")
    @Mapping(target = "remark", source = ".", qualifiedByName = "getRemarkByLanguage")
    @Named("entityToDtoWithLanguage")
    DictionaryDTO entityToDto(DictionaryEntity entity, @Context Locale locale);

    /**
     * 非货币类型使用的是value字段进行的国际化
     *
     * @param entity
     * @param locale
     * @return
     */
    @Named("getValueByLanguage")
    default String getValueByLanguage(DictionaryEntity entity, @Context Locale locale) {
        if (entity == null) {
            return null;
        }
        if (DictionaryEnum.REPORT.getName().equals(entity.getType())) {
            return entity.getValue();
        }
        Function<DictionaryEntity, String> functionDictionaryValue = LanguageLocalUtils.getFunctionDictionaryValue(locale);
        return functionDictionaryValue.apply(entity);
    }


    /**
     * 货币类型 使用的是remark字段进行的国际化
     *
     * @param entity
     * @param locale
     * @return
     */
    @Named("getRemarkByLanguage")
    default String getRemarkByLanguage(DictionaryEntity entity, @Context Locale locale) {
        if (entity == null) {
            return null;
        }
        if (locale==null){
            return entity.getRemark();
        }
        if (!DictionaryEnum.REPORT.getName().equals(entity.getType())) {
            return entity.getRemark();
        }
        boolean unSupportLocale = LanguageLocalUtils.isUnSupportLocale(locale);
        if (unSupportLocale) {
            return entity.getRemark();
        }
        if (LanguageLocalUtils.isEnglishLanguage(locale)) {
            return entity.getRemark();
        }
        Function<DictionaryEntity, String> functionDictionaryValue = LanguageLocalUtils.getFunctionDictionaryValue(locale);
        return functionDictionaryValue.apply(entity);
    }
}