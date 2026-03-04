package com.item.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.JobCategoryDto;
import com.item.dto.JobDto;
import com.item.dto.JobModeDto;
import com.item.dto.JobTypeDto;
import com.item.dto.LocationDto;
import com.item.dto.job.JobCreateBO;
import com.item.entity.JobEntity;
import com.item.framework.constant.TimeEnum;

import java.util.List;

/**
 * Job服务类
 * 演示MapStruct的使用
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */

public interface JobService extends IService<JobEntity> {

    /**
     * 分页查询职位列表
     *
     * @param pageNo
     * @param  pageSize
     * @return 职位DTO分页数据
     */
    IPage<JobDto> selectJobList(int pageNo, int pageSize, JobDto job, TimeEnum timeEnum);

    List<JobCategoryDto> getAllJobCategories();

    List<JobTypeDto> getAllJobTypes();

    List<LocationDto> getAllLocations();

    List<JobModeDto> getAllJobModes();

    JobEntity createJob(JobCreateBO dto);

    List<JobEntity> getJobsByIds(List<Long> ids);

    JobEntity getJobsByIds(Long id);

    List<JobEntity> listJobsByMasterAccountId(Long masterAccountId);

    long countJobsByMasterAccountId(Long masterAccountId);

    /**
     * Check if job with same hash and urlCode already exists
     * Uses hash and urlCode for performance optimization
     *
     * @param companyTitleHash the hash of company name and title
     * @param urlCode the url code for the job
     * @return true if duplicate exists, false otherwise
     */
    boolean existsByCompanyTitleHash(String companyTitleHash, String urlCode, String companyCode);

    boolean existsByCompanyTitleHash(String companyTitleHash, String urlCode, Long oneself, String companyCode);

    JobEntity getByCompanyTitleHash(String companyTitleHash, String urlCode, String companyCode);

    /**
     * 查询未生成面试url id的职位id
     *
     * @return 职位DTO列表
     */
    List<Long> selectNotCreateInterviewUrlId();

    //Pager<JobListVO> searchJobsFromEs(int pageNo, int pageSize, String keyword, JobDto job, Integer jobPosted);

    /**
     * 更新职位 url id
     * @param jobId
     * @param interviewUrlId
     */
    boolean updateInterviewUrlId(Long jobId, String interviewUrlId);

    IPage<JobEntity> listJobSimple(Integer pageIndex, Integer pageSize, String companyCode);

    /**
     * Update naukri job id for a job
     */
    boolean updateNaukriJobId(Long jobId, String naukriJobId);
}
