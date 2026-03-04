package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.CityEntity;
import com.item.mapper.CityMapper;
import com.item.service.CityService;
import com.item.util.LanguageLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * @author : lh
 */
@Slf4j
@Component
public class CityServiceImpl extends ServiceImpl<CityMapper, CityEntity> implements CityService {

    @Override
    public List<CityEntity> searchCitiesByLanguage(String keyword, int maxSize, Locale locale) {
        if (StringUtils.isBlank(keyword) || maxSize <= 0) {
            return List.of();
        }
        SFunction<CityEntity, String> functionCityName = LanguageLocalUtils.getSFunctionCityName(locale);
        LambdaQueryWrapper<CityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(functionCityName, keyword)
                .orderByAsc(CityEntity::getName)
                .last("LIMIT " + maxSize);

        return list(queryWrapper);
    }
}
