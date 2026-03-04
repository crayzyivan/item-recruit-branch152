package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.convert.DictionaryConverter;
import com.item.dto.DictionaryDTO;
import com.item.dto.cache.DictionaryCacheDTO;
import com.item.entity.DictionaryEntity;
import com.item.mapper.DictionaryMapper;
import com.item.service.DeclarativeCacheService;
import com.item.service.DictionaryService;
import com.item.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, DictionaryEntity> implements DictionaryService {

    private final DeclarativeCacheService declarativeCacheService;

    @Override
    public DictionaryDTO get(Long id) {
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("DictionaryServiceImpl get languageLocal = {}", languageLocal);
        DictionaryEntity entity = this.getById(id);
        return DictionaryConverter.INSTANCE.entityToDto(entity, languageLocal);
    }

    @Override
    public List<DictionaryDTO> listByType(String type) {
        List<DictionaryCacheDTO> dictionaryEntities = declarativeCacheService.listByTypeAllLanguage(type);
        if (CollectionUtils.isEmpty(dictionaryEntities)) {
            return List.of();
        }
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("DictionaryServiceImpl listByType languageLocal = {}", languageLocal);
        return DictionaryConverter.INSTANCE.cacheToDtos(dictionaryEntities, languageLocal);
    }

    @Override
    public List<DictionaryDTO> listByTypes(List<String> types) {
        LambdaQueryWrapper<DictionaryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DictionaryEntity::getType, types);
        wrapper.eq(DictionaryEntity::getStatus, 1);
        wrapper.orderByAsc(DictionaryEntity::getSort);
        List<DictionaryEntity> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("DictionaryServiceImpl listByTypes languageLocal = {}", languageLocal);
        return DictionaryConverter.INSTANCE.entityToDtos(list, languageLocal);
    }

    /**
     * 根据id批量查询
     * @param ids
     * @return
     */
    @Override
    public List<DictionaryDTO> listDtoByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }

        List<DictionaryEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("DictionaryServiceImpl listDtoByIds languageLocal = {}", languageLocal);
        return DictionaryConverter.INSTANCE.entityToDtos(list, languageLocal);
    }

    @Override
    public List<DictionaryDTO> getAll() {
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("DictionaryServiceImpl getAll languageLocal = {}", languageLocal);
        List<DictionaryEntity> list = this.list();
        return DictionaryConverter.INSTANCE.entityToDtos(list, languageLocal);
    }

    @Override
    public Map<Long, String> listDictMapping(List<String> types) {
        List<DictionaryDTO> dictionaryDTOS = listByTypes(types);
        if (CollectionUtils.isEmpty(dictionaryDTOS)) {
            return Map.of();
        }
        return dictionaryDTOS.stream().collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue, (value1, value2) -> value1));
    }
}