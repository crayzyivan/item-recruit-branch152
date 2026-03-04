package com.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.dto.JobModeDto;
import com.item.entity.JobModeEntity;
import com.item.mapper.JobModeMapper;
import com.item.service.DeclarativeCacheService;
import com.item.service.JobModeService;
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
public class JobModeServiceImp extends ServiceImpl<JobModeMapper, JobModeEntity> implements JobModeService {

    private final DeclarativeCacheService declarativeCacheService;

    @Override
    public List<JobModeDto> getAll() {
        Locale languageLocal = UserContextUtil.getLanguageLocal();
        log.info("JobModeServiceImp getAll languageLocal = {}", languageLocal);
        List<JobModeEntity> allModeAllLanguage = declarativeCacheService.getAllModeAllLanguage();
        Function<JobModeEntity, String> functionJobModeName = LanguageLocalUtils.getFunctionJobModeName(languageLocal);
        return allModeAllLanguage.stream().map(c -> {
            JobModeDto dto = new JobModeDto();
            dto.setId(c.getId());
            dto.setName(functionJobModeName.apply(c));
            return dto;
        }).toList();
    }

    @Override
    public Map<Integer, String> getAllJobModeMapping() {
        List<JobModeDto> all = getAll();
        return all.stream().collect(Collectors.toMap(JobModeDto::getId, JobModeDto::getName, (name1, name2) -> name1));
    }
}
