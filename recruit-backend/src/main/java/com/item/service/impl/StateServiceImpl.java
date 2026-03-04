package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.StateEntity;
import com.item.mapper.StateMapper;
import com.item.service.StateService;
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
public class StateServiceImpl extends ServiceImpl<StateMapper, StateEntity> implements StateService {

    @Override
    public List<StateEntity> searchStatesByLanguage(String keyword, int maxSize, Locale locale) {
        if (StringUtils.isBlank(keyword) || maxSize <= 0) {
            return List.of();
        }
        SFunction<StateEntity, String> sFunctionStateName = LanguageLocalUtils.getSFunctionStateName(locale);
        LambdaQueryWrapper<StateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(sFunctionStateName, keyword)
                .orderByAsc(StateEntity::getName)
                .last("LIMIT " + maxSize);

        return list(queryWrapper);
    }
}
