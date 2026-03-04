package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.JobDto;
import com.item.dto.StateDTO;
import com.item.dto.job.LocationValRecordDTO;
import com.item.entity.JobEntity;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.AiInterviewConfig;
import com.item.framework.http.Pager;
import com.item.service.JobService;
import com.item.service.LocationService;
import com.item.service.RDDomainService;
import com.item.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RDDomainServiceImpl implements RDDomainService {
    private final com.item.es.JobEsService jobEsService;
    private final JobService jobService;

    private final LocationService locationService;

    private final AiInterviewConfig aiInterviewConfig;

    @Override
    public void updateJobEsOld2NewLocation(Integer pageIndex, Integer pageSize, Long currentJobId) {
        JobDto jobDto = new JobDto();
        if(pageIndex == null || pageIndex < 1) {
            pageIndex = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 200;
        }
        Pager<JobEsEntity> jobEsEntityPager = jobEsService.searchJobs(null, pageIndex, pageSize, jobDto, -1);
        log.info("updateJobEsOld2NewLocation jobEsEntityPager {} {} {} {}", jobEsEntityPager.getPageIndex(), jobEsEntityPager.getPageSize(), jobEsEntityPager.getTotalCount(), jobEsEntityPager.getCurrentPageRecords());
        List<JobEsEntity> list = null;
        if (currentJobId != null) {
            list = jobEsEntityPager.getCurrentPageRecords().stream().filter(j -> j.getId().equals(currentJobId)).toList();
        } else {
            list = jobEsEntityPager.getCurrentPageRecords();
        }

        if(CollectionUtils.isEmpty(list)) {
            log.warn("list is empty {} {} {} {}", pageIndex, pageSize, currentJobId,jobEsEntityPager);
            return;
        }

        // city Map构建
        List<Long> cityIds = list.stream().map(JobEsEntity::getLocationId).map(Integer::longValue).distinct().toList();
        List<CityDTO> cityDTOS = locationService.listByCityIds(cityIds);
        Map<Long, CityDTO> cityIdMap = cityDTOS.stream().collect(Collectors.toMap(CityDTO::getId, Function.identity(), (v1, v2) -> v1));

        // state Map构建
        List<Long> stateIds = cityDTOS.stream().map(CityDTO::getStateId).distinct().toList();
        List<StateDTO> stateDTOS = locationService.listByStateIds(stateIds);
        Map<Long, StateDTO> stateIdMap = stateDTOS.stream().collect(Collectors.toMap(StateDTO::getId, Function.identity(), (v1, v2) -> v1));

        //county Map构建
        List<Long> countyIds = stateDTOS.stream().map(StateDTO::getCountryId).distinct().toList();
        List<CountryDTO> countryDTOS = locationService.listByCountryIds(countyIds);
        Map<Long, CountryDTO> countyIdMap = countryDTOS.stream().collect(Collectors.toMap(CountryDTO::getId, Function.identity(), (v1, v2) -> v1));

        for (JobEsEntity jobEsEntity : list) {
            if (jobEsEntity != null && CollectionUtils.isEmpty(jobEsEntity.getLocations())) {
                Integer locationId = jobEsEntity.getLocationId();
                CityDTO cityDTO = cityIdMap.get(locationId.longValue());
                if (cityDTO == null) {
                    log.warn("cityDTO is null jobEsEntity {} cityIdMap {}", jobEsEntity, cityIdMap);
                    continue;
                }
                StateDTO stateDTO = stateIdMap.get(cityDTO.getStateId());
                if (stateDTO == null) {
                    log.warn("stateDTO is null jobEsEntity {} stateIdMap {}", jobEsEntity, stateIdMap);
                    continue;
                }
                CountryDTO countryDTO = countyIdMap.get(stateDTO.getCountryId());
                if (countryDTO == null) {
                    log.warn("countryDTO is null jobEsEntity {} countyIdMap {}", jobEsEntity, countyIdMap);
                    continue;
                }
                JobEsEntity jobEsEntity1 = new JobEsEntity();
                jobEsEntity1.setId(jobEsEntity.getId());
                LocationValRecordDTO locationValRecordDTO = new LocationValRecordDTO();
                locationValRecordDTO.setCityId(cityDTO.getId());
                locationValRecordDTO.setStateId(stateDTO.getId());
                locationValRecordDTO.setCountryId(countryDTO.getId());
                locationValRecordDTO.setCityName(cityDTO.getName());
                locationValRecordDTO.setStateName(stateDTO.getName());
                locationValRecordDTO.setCountryName(countryDTO.getName());

                locationValRecordDTO.setLocationName(CommonUtils.getLocationName(countryDTO.getName(), stateDTO.getName(), cityDTO.getName()));
                jobEsEntity1.setLocations(Collections.singletonList(locationValRecordDTO));
                jobEsService.updateJobEs(jobEsEntity1);
            }
        }
    }

    @Transactional
    @Override
    public void updateJobInterviewLength(Integer pageIndex, Integer pageSize, Long currentJobId, Integer interviewLength) {
        if (pageIndex == null || pageIndex < 1) {
            pageIndex = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 200;
        }
        Page<JobEntity> page = new Page<>(pageIndex, pageSize);
        LambdaQueryWrapper<JobEntity> queryWrapper = new QueryWrapper<JobEntity>().lambda();
        queryWrapper.select(JobEntity::getId, JobEntity::getInterviewLength);
        queryWrapper.eq(JobEntity::getInterviewLength, 0).or().isNull(JobEntity::getInterviewLength);
        Page<JobEntity> pageJobs = jobService.page(page, queryWrapper);
        List<JobEntity> records = pageJobs.getRecords();
        if (currentJobId != null) {
            records = records.stream().filter(j -> j.getId().equals(currentJobId)).toList();
        }
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        List<Long> ids = records.stream().map(JobEntity::getId).toList();
        LambdaUpdateWrapper<JobEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(JobEntity::getInterviewLength, Objects.isNull(interviewLength) ? 10 : interviewLength)
                .in(JobEntity::getId, ids);
        jobService.update(updateWrapper);
        List<JobEsEntity> list = records.stream().map(job -> {
            JobEsEntity jobEsEntity = new JobEsEntity();
            jobEsEntity.setId(job.getId());
            jobEsEntity.setInterviewLength(Objects.isNull(interviewLength) ? 10 : interviewLength);
            return jobEsEntity;
        }).toList();
        for (JobEsEntity jobEsEntity : list) {
            jobEsService.updateJobEs(jobEsEntity);
        }
    }
}
