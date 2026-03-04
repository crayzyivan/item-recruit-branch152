package com.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.dto.JobCategoryDto;
import com.item.entity.JobCategoryEntity;
import com.item.mapper.JobCategoryMapper;
import com.item.service.DeclarativeCacheService;
import com.item.service.JobCategoryService;
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
public class JobCategoryServiceImp extends ServiceImpl<JobCategoryMapper, JobCategoryEntity> implements JobCategoryService {

    private final DeclarativeCacheService declarativeCacheService;

    @Override
    public List<JobCategoryDto> getAllJobCategories() {
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("JobCategoryServiceImp getAllJobCategories languageLocal = {}", languageLocal);
        List<JobCategoryEntity> allJobCategoriesAllLanguage = declarativeCacheService.getAllJobCategoriesAllLanguage();
        Function<JobCategoryEntity, String> functionJobCategoryName = LanguageLocalUtils.getFunctionJobCategoryName(languageLocal);
        return allJobCategoriesAllLanguage.stream().map(c -> {
            JobCategoryDto dto = new JobCategoryDto();
            dto.setId(c.getId());
            dto.setName(functionJobCategoryName.apply(c));
            return dto;
        }).toList();
    }

    @Override
    public Map<Integer, String> getAllJobCategoryMapping() {
        List<JobCategoryDto> allJobCategories = getAllJobCategories();
        return allJobCategories.stream().collect(Collectors.toMap(JobCategoryDto::getId, JobCategoryDto::getName, (name1, name2) -> name1));
    }

}
