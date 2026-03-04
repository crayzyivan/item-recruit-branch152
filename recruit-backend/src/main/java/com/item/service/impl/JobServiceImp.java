package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.item.convert.JobConvert;
import com.item.convert.JobConverter;
import com.item.dto.JobCategoryDto;
import com.item.dto.JobDto;
import com.item.dto.JobModeDto;
import com.item.dto.JobTypeDto;
import com.item.dto.LocationDto;
import com.item.dto.job.JobCreateBO;
import com.item.entity.JobEntity;
import com.item.framework.constant.JobStatus;
import com.item.framework.constant.TimeEnum;
import com.item.mapper.JobMapper;
import com.item.service.JobCategoryService;
import com.item.service.JobModeService;
import com.item.service.JobScreenService;
import com.item.service.JobService;
import com.item.service.JobTypeService;
import com.item.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class JobServiceImp extends ServiceImpl<JobMapper, JobEntity> implements JobService {
    private final JobScreenService jobScreenService;
    private final JobCategoryService jobCategoryService;
    private final JobTypeService jobTypeService;
    private final JobModeService jobModeService;
    private final LocationService locationService;

    private final JobConvert jobConvert;


    @Override
    public IPage<JobDto> selectJobList(int pageNo, int pageSize, JobDto job, TimeEnum timeEnum) {
        Page<JobEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<JobEntity> queryWrapper = new QueryWrapper<JobEntity>().lambda();

        if (job != null) {
            if(Objects.nonNull(job.getCategoryId()) && job.getCategoryId() > 0) {
                queryWrapper.eq(JobEntity::getCategoryId, job.getCategoryId());
            }
            if(Objects.nonNull(job.getTypeId()) && job.getTypeId() > 0) {
                queryWrapper.eq(JobEntity::getTypeId, job.getTypeId());
            }
            if(Objects.nonNull(job.getLocationId()) && job.getLocationId() > 0) {
                queryWrapper.eq(JobEntity::getLocationId, job.getLocationId());
            }
            if (!Strings.isNullOrEmpty(job.getCompanyCode())) {
                queryWrapper.eq(JobEntity::getCompanyCode, job.getCompanyCode());
            }
            queryWrapper.eq(StringUtils.isNotBlank(job.getCompanyCode()), JobEntity::getCompanyCode, job.getCompanyCode());
            queryWrapper.in(CollectionUtils.isNotEmpty(job.getQJobStatus()), JobEntity::getJobStatus, job.getQJobStatus());
        }

        if (timeEnum != null) {
            switch (timeEnum) {
                case LASTHOUR -> queryWrapper.ge(JobEntity::getCreateTime, LocalDateTime.now().minusHours(1));
                case LASTDAY -> queryWrapper.ge(JobEntity::getCreateTime, LocalDateTime.now().minusDays(1));
                case LASTWEEK -> queryWrapper.ge(JobEntity::getCreateTime, LocalDateTime.now().minusWeeks(1));
                case LAST14DAYS -> queryWrapper.ge(JobEntity::getCreateTime, LocalDateTime.now().minusDays(14));
                case LAST30DAYS -> queryWrapper.ge(JobEntity::getCreateTime, LocalDateTime.now().minusDays(30));
            }
        }
        List<JobTypeDto> types = jobTypeService.getAllJobTypes();
        List<JobModeDto> modes = jobModeService.getAll();
        Map<Integer, JobTypeDto> typeMap = types.stream().collect(Collectors.toMap(JobTypeDto::getId, Function.identity()));
        Map<Integer, JobModeDto> modeMap = modes.stream().collect(Collectors.toMap(JobModeDto::getId, Function.identity()));
        queryWrapper.orderByDesc(JobEntity::getCreateTime);
        this.page(page, queryWrapper);
        List<JobDto> jobDtoList = new ArrayList<>();
        page.getRecords().forEach(j -> {
            JobDto dto = JobConverter.INSTANCE.convertEntityToDto(j);
            // TODO: set Job logo url
            //dto.setLogoUrl();
//            dto.setApplicationCount(jobScreenService.getApplicationCount(j.getId()));
            dto.setTypeName(Optional.ofNullable(typeMap.get(j.getTypeId())).map(JobTypeDto::getName).orElse(null));
            dto.setModeName(Optional.ofNullable(modeMap.get(j.getModeId())).map(JobModeDto::getName).orElse(null));
            jobDtoList.add(dto);
        });
        IPage<JobDto> result = new Page<>(pageNo, pageSize,page.getTotal());
        result.setRecords(jobDtoList);

        return result;
    }

    @Override
    public List<JobCategoryDto> getAllJobCategories() {
        return this.jobCategoryService.getAllJobCategories();
    }

    @Override
    public List<JobTypeDto> getAllJobTypes() {
        return this.jobTypeService.getAllJobTypes();
    }

    @Override
    public List<LocationDto> getAllLocations() {
        return this.locationService.findAll();
    }

    @Override
    public List<JobModeDto> getAllJobModes() {
        return this.jobModeService.getAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobEntity createJob(JobCreateBO dto) {
        JobEntity jobEntity = jobConvert.toJob(dto);
        save(jobEntity);
        return jobEntity;
    }

    @Override
    public List<JobEntity> getJobsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<JobEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.in(JobEntity::getId, ids);
        return this.list(wrapper);
    }

    @Override
    public JobEntity getJobsByIds(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        return this.getById(id);
    }

    @Override
    public List<JobEntity> listJobsByMasterAccountId(Long masterAccountId) {
        if (masterAccountId == null || masterAccountId <= 0) {
            return List.of();
        }
        LambdaQueryWrapper<JobEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(JobEntity::getMasterAccountId, masterAccountId);
        return this.list(wrapper);
    }


    @Override
    public long countJobsByMasterAccountId(Long masterAccountId) {
        if (masterAccountId == null || masterAccountId <= 0) {
            return 0;
        }
        LambdaQueryWrapper<JobEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(JobEntity::getMasterAccountId, masterAccountId);
        return this.count(wrapper);
    }

    @Override
    public boolean existsByCompanyTitleHash(String companyTitleHash, String urlCode, String companyCode) {
        JobEntity job = getByCompanyTitleHash(companyTitleHash, urlCode, companyCode);

        return job != null;
    }

    @Override
    public boolean existsByCompanyTitleHash(String companyTitleHash, String urlCode, Long oneself, String companyCode) {
        JobEntity job = getByCompanyTitleHash(companyTitleHash, urlCode, companyCode);
        if (job == null) {
            return false;
        }
        return !job.getId().equals(oneself);
    }

    @Override
    public JobEntity getByCompanyTitleHash(String companyTitleHash, String urlCode, String companyCode) {
        if (StringUtils.isBlank(companyTitleHash) || StringUtils.isBlank(urlCode)) {
            return null;
        }

        // Check by hash and urlCode for performance optimization
        LambdaQueryWrapper<JobEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(JobEntity::getCompanyTitleHash, companyTitleHash)
                .eq(JobEntity::getCompanyCode, companyCode)
                .eq(JobEntity::getUrlCode, urlCode)
                .last("LIMIT 1");
        List<JobEntity> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.stream().findFirst().orElse(null);
    }

    /**
     * 查询未生成面试url id的职位id
     *
     * @return 职位DTO列表
     */
    @Override
    public List<Long> selectNotCreateInterviewUrlId() {
        LambdaQueryWrapper<JobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(JobEntity::getId);  // 只查询 id 字段
        //queryWrapper.eq(JobEntity::getJobStatus, JobStatus.ACTIVE.getCode());
        queryWrapper.and(w -> w.isNull(JobEntity::getInterviewUrlId).or().eq(JobEntity::getInterviewUrlId, ""));
        List<JobEntity> jobs = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(jobs)){
            return jobs.stream().map(JobEntity::getId).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 更新职位 url id
     * @param jobId
     * @param interviewUrlId
     */
    @Override
    public boolean updateInterviewUrlId(Long jobId, String interviewUrlId) {
        LambdaUpdateWrapper<JobEntity> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper
                .set(JobEntity::getInterviewUrlId, interviewUrlId)
                .eq(JobEntity::getId, jobId);
        return this.update(updateWrapper);
    }

    @Override
    public IPage<JobEntity> listJobSimple(Integer pageIndex, Integer pageSize, String companyCode) {
        Page<JobEntity> page = new Page<>();
        if(StringUtils.isBlank(companyCode)){
            page.setCurrent(1);
            page.setSize(10);
            page.setTotal(0);
            page.setRecords(List.of());
            return page;
        }


        // // 分页查询当前companyCode下的所有job数据，只需要id和title字段值
        if (pageIndex != null && pageSize != null) {
            page.setCurrent(pageIndex);
            page.setSize(pageSize);
        } else {
            //如果参数有一个为空就不走分页查询
            page.setCurrent(1);
            page.setSize(Integer.MAX_VALUE);
        }

        LambdaQueryWrapper<JobEntity> queryWrapper = Wrappers.lambdaQuery();
        
        // 只查询 id 和 title 字段
        queryWrapper.select(JobEntity::getId, JobEntity::getTitle);
        
        // 查询指定 companyCode 下的职位
        queryWrapper.eq(JobEntity::getCompanyCode, companyCode);
        
        // 按创建时间降序排列
        queryWrapper.orderByDesc(JobEntity::getId);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public boolean updateNaukriJobId(Long jobId, String naukriJobId) {
       LambdaUpdateWrapper<JobEntity> updateWrapper = new LambdaUpdateWrapper<>();
       updateWrapper
               .set(JobEntity::getNaukriJobId, naukriJobId)
               .eq(JobEntity::getId, jobId);
       return this.update(updateWrapper);
    }
}
