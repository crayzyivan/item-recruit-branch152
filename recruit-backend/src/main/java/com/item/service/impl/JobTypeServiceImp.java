package com.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.dto.JobTypeDto;
import com.item.entity.JobTypeEntity;
import com.item.mapper.JobTypeMapper;
import com.item.service.DeclarativeCacheService;
import com.item.service.JobTypeService;
import com.item.util.LanguageLocalUtils;
import com.item.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobTypeServiceImp extends ServiceImpl<JobTypeMapper, JobTypeEntity> implements JobTypeService {

    private final DeclarativeCacheService declarativeCacheService;

    @Override
    public List<JobTypeDto> getAllJobTypes() {
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("JobTypeServiceImp getAllJobTypes languageLocal = {}", languageLocal);
        List<JobTypeEntity> allJobTypeAllLanguage = declarativeCacheService.getAllJobTypeAllLanguage();
        Function<JobTypeEntity, String> functionJobTypeName = LanguageLocalUtils.getFunctionJobTypeName(languageLocal);
        return allJobTypeAllLanguage.stream().map(c -> {
            JobTypeDto dto = new JobTypeDto();
            dto.setId(c.getId());
            dto.setName(functionJobTypeName.apply(c));
            return dto;
        }).toList();
    }

    @Override
    public Map<Integer, String> getAllJobTypeMapping() {
        List<JobTypeDto> all = getAllJobTypes();
        return all.stream().collect(Collectors.toMap(JobTypeDto::getId, JobTypeDto::getName, (name1, name2) -> name1));
    }
}
