package com.item.es;

import com.item.dto.JobDto;
import com.item.dto.job.LocationValDTO;
import com.item.es.entity.JobEsEntity;
import com.item.framework.http.Pager;
import com.item.vo.JobOptionVO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface JobEsService {
    void saveJobToEs(JobEsEntity jobEsEntity);
    Pager<JobEsEntity> searchJobs(String keyword, int pageNum, int pageSize, JobDto jobDto, int datePosted);
    JobEsEntity getJobById(Long jobId);
    JobEsEntity getJobById(Long jobId, String[] selectField);
    List<JobEsEntity> getJobByIds(Collection<Long> jobIds);

    /**
     * 通过jobid获取智能判定规则
     * 其中规则属性可能为null 为null默认未开启智能判定
     *
     * @param jobIds
     * @return
     */
    List<JobEsEntity> listJobIntelligenceScoreRuleByIds(Collection<Long> jobIds);
    /**
     * Update job info in ES by jobId
     */
    void updateJobEs(JobEsEntity jobEsEntity);

    /**
     * Delete job from ES by jobId
     * 
     * @param jobId the job ID to delete
     * @return true if deletion successful, false otherwise
     */
    boolean deleteJobById(Long jobId);

    /**
     * 搜索历史职位列表（支持去重）
     * 根据jobTitle去重，按id倒序，只返回id和title字段
     *
     * @param keyword 搜索关键字
     * @param pageIndex 页码
     * @param pageSize 页大小
     * @param companyCode 公司代码
     * @return 去重后的职位列表
     */
    Pager<JobOptionVO> searchHistoryJobsWithDeduplication(String keyword, int pageIndex, int pageSize, String companyCode);

    /**
     * 搜索feed xml job
     * @param jobStatus
     * @param companyCode
     * @param size
     * @return
     */
    List<JobEsEntity> searchFeedXmlJobs(Integer jobStatus, String companyCode, int size,boolean distinct);

    /**
     * Search jobs by title exact match and locations nested match
     * 
     * @param title job title for exact match (case insensitive)
     * @param locations list of location criteria for nested matching （any one） ; You can configure whether this condition takes effect
     * @param size maximum number of results to return
     * @param companyCode current user companyCode
     * @param excludeJobId exclude job id
     * @return list of matching job entities
     */
    List<JobEsEntity> searchJobsByTitleAndLocations(String title, List<LocationValDTO> locations, String companyCode, Long excludeJobId, int size);

    /**
     * Search random jobs with optional filtering
     * 
     * @param size maximum number of random jobs to return
     * @param companyCode optional company code filter
     * @param seed optional random seed for reproducible results
     * @param categoryIds optional job category IDs filter
     * @param locations optional locations filter
     * @param excludeJobIds optional job IDs to exclude from results
     * @return list of random job entities
     */
    List<JobEsEntity> searchRandomJobs(Integer size, String companyCode, Long seed, List<Long> categoryIds, List<LocationValDTO> locations, Set<Long> excludeJobIds);
}