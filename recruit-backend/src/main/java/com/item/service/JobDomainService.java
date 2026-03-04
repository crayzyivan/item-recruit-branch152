package com.item.service;

import com.item.dto.CurrencyTypeDTO;
import com.item.dto.JobDto;
import com.item.dto.JobOptionDTO;
import com.item.dto.SalaryTypeDTO;
import com.item.dto.job.JobCreateBO;
import com.item.dto.job.JobCreateDTO;
import com.item.dto.job.JobHistoryListRequestDTO;
import com.item.dto.job.JobUpdateDTO;
import com.item.dto.job.LocationValDTO;
import com.item.dto.job.RandomJobRecommendRequestDTO;
import com.item.framework.http.Pager;
import com.item.vo.JobDetailVO;
import com.item.vo.JobHistoryDetailVO;
import com.item.vo.JobListVO;
import com.item.vo.JobOptionVO;
import com.item.vo.LocationTypeVO;
import com.item.vo.ai.InterviewResultVO;
import com.item.vo.job.JobCreateResponseVO;
import com.item.vo.job.JobUpdateResponseVO;

import java.util.List;

public interface JobDomainService {
    JobCreateResponseVO publishJob(JobCreateDTO dto);

    JobDetailVO getJobDetailById(Long jobId, boolean recruiter);

    JobDetailVO getJobDetailUnlimitById(Long jobId);

    JobDetailVO getJobDetailApplicationById(Long jobId);

    JobDetailVO getJobDetailRecruiterById(Long jobId);


    JobDetailVO getJobDetailByIdStr(String jobCode);

    /**
     * Update job info by jobId
     */
    JobUpdateResponseVO updateJob(JobUpdateDTO dto);

    /**
     * Update job status by jobId
     * 
     * @param jobId the job ID
     * @param jobStatus the new job status
     * @return true if update successful, false otherwise
     */
    Boolean updateJobStatus(Long jobId, Integer jobStatus, String comment);

    /**
     * Delete job by jobId
     * 
     * @param jobId the job ID to delete
     * @return true if deletion successful, false otherwise
     */
    Boolean deleteJob(Long jobId);

    /**
     * Get all salary types as DTO list
     * 
     * @return List of SalaryTypeDto
     */
    List<SalaryTypeDTO> getAllSalaryTypes();
    
    /**
     * Get all currency types as DTO list
     * 
     * @return List of CurrencyTypeDto
     */
    List<CurrencyTypeDTO> getAllCurrencyTypes();

    /**
     * Generate company share link by master account ID
     *
     * @return generated share link
     */
    String generateCompanyShareLink();

    /**
     * Generate job info share link by job ID
     * 
     * @param jobId job ID
     * @return generated job info share link
     */
    String generateJobInfoShareLink(Long jobId);


    /**
     * Generate job info share link by job ID
     *
     * @param jobId job ID
     * @return generated job info share link
     */
    String generateJobInfoSimpleShareLink(Long jobId);

    Pager<JobListVO> searchJobsFromEs(int pageNo, int pageSize, String keyword, JobDto job, Integer jobPosted);

    /**
     * 生成面试id
     * @param jobCreateBO
     * @return
     */
    InterviewResultVO createAIInterview(JobCreateBO jobCreateBO);

    /**
     * 获取所有的r_job_mode数据 用于页面的locationType
     * @return
     */
    List<LocationTypeVO> getAllLocationTypes();

    /**
     * 获取当前company下的job
     *
     * @param option
     * @return
     */
    Pager<JobOptionVO> listJobOption(JobOptionDTO option);

    /**
     * 生成链接 有域名
     * jobs/idcode/companyname-warehouse-supervisor-evening-shift
     *
     * @param idCode
     * @param urlCode
     * @return
     */
    String generateInfoShareLink(String idCode, String urlCode);

    /**
     * 生成链接 无域名
     * jobs/idcode/companyname-warehouse-supervisor-evening-shift
     *
     * @param idCode
     * @param urlCode
     * @return
     */
    String generateInfoSimpleShareLink(String idCode, String urlCode);
    /**
     *
     * @param jobPublishRoutePrefix
     * @param companyCode
     * @return
     */
    String generateListShareLink(String jobPublishRoutePrefix, String companyCode);
    
    /**
     * 获取历史职位列表（支持去重和搜索）
     * 
     * @param requestDTO 请求参数
     * @return 历史职位列表
     */
    Pager<JobOptionVO> getHistoryJobList(JobHistoryListRequestDTO requestDTO);
    
    /**
     * 根据jobId获取历史职位详情
     * 
     * @param jobId 职位ID
     * @return 职位详情
     */
    JobHistoryDetailVO getHistoryJobDetail(Long jobId);

    /**
     * Check if job is duplicated based on title and locations
     * 
     * @param title job title for exact match
     * @param locations list of location criteria for nested matching
     * @param companyCode current user companyCode
     * @param excludeJobId exclude job id
     * @param confirmSave whether user has confirmed to save duplicate job
     * @return true if job is duplicated and user hasn't confirmed save, false otherwise
     */
    boolean checkJobDuplication(String title, List<LocationValDTO> locations, String companyCode, Long excludeJobId, boolean confirmSave);

    /**
     * Get random job recommendations
     * 
     * @param request random job recommendation request parameters
     * @return list of random job recommendations
     */
    List<JobListVO> getRandomJobRecommendations(RandomJobRecommendRequestDTO request);
}
