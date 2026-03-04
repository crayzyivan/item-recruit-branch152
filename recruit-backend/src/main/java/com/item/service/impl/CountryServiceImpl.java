package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.CountryEntity;
import com.item.mapper.CountryMapper;
import com.item.service.CountryService;
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
public class CountryServiceImpl extends ServiceImpl<CountryMapper, CountryEntity> implements CountryService {

    @Override
    public List<CountryEntity> searchCountryByLanguage(String keyword, int maxSize, Locale locale) {
        if (StringUtils.isBlank(keyword) || maxSize <= 0) {
            return List.of();
        }
        SFunction<CountryEntity, String> sFunctionCountryName = LanguageLocalUtils.getSFunctionCountryName(locale);
        LambdaQueryWrapper<CountryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(sFunctionCountryName, keyword)
                .orderByAsc(CountryEntity::getName)
                .last("LIMIT " + maxSize); // 限制国家数量

        return list(queryWrapper);
    }
}
