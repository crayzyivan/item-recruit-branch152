package com.item.controller;

import com.google.common.base.Strings;
import com.item.convert.JobCategoryConverter;
import com.item.convert.JobConvert;
import com.item.convert.JobConverter;
import com.item.convert.JobTypeConverter;
import com.item.convert.LocationConverter;
import com.item.dto.JobDto;
import com.item.dto.ai.GenerateJobRequestDTO;
import com.item.dto.ai.JobDescriptionDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.job.CandidateJobListReqDTO;
import com.item.dto.job.GenerateJobDescDTO;
import com.item.dto.job.JobCreateDTO;
import com.item.dto.job.JobCreateRequestVO;
import com.item.dto.job.JobHistoryListRequestDTO;
import com.item.dto.job.JobUpdateDTO;
import com.item.dto.job.JobUpdateRequestVO;
import com.item.dto.job.RandomJobRecommendRequestDTO;
import com.item.dto.job.RecommendCandidateCountRequestDTO;
import com.item.dto.job.RecruitJobListReqDTO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.JobStatus;
import com.item.framework.constant.RoleType;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.service.*;
import com.item.util.CommonUtils;
import com.item.util.UserContextUtil;
import com.item.vo.CompanyInfoVO;
import com.item.vo.CurrencyTypeVO;
import com.item.vo.InviteApplyRequestVO;
import com.item.vo.InviteApplyResponseVO;
import com.item.vo.InviteInterviewRequestVO;
import com.item.vo.InviteInterviewResponseVO;
import com.item.vo.BatchInviteInterviewRequestVO;
import com.item.vo.BatchInviteInterviewResponseVO;
import com.item.vo.JobCategoryVo;
import com.item.vo.JobDetailVO;
import com.item.vo.JobHistoryDetailVO;
import com.item.vo.JobListVO;
import com.item.vo.JobOptionVO;
import com.item.vo.JobShareDetailVO;
import com.item.vo.JobShareListVO;
import com.item.vo.JobStatusUpdateRequestVO;
import com.item.vo.JobTypeVo;
import com.item.vo.JobVO;
import com.item.vo.LocationTypeVO;
import com.item.vo.LocationVo;
import com.item.vo.RecommendCandidateCountVO;
import com.item.vo.RecommendCandidateVO;
import com.item.vo.SalaryTypeVO;
import com.item.vo.ai.AIJobDescriptionVO;
import com.item.vo.ai.AIJobResponseDataVO;
import com.item.vo.job.JobCreateResponseVO;
import com.item.vo.job.JobUpdateResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

/**
 * 职位相关接口
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    private final JobDomainService jobDomainService;
    private final JobConvert jobConvert;
    private final AIService aiService;
    private final CompanyDomainService companyDomainService;
    private final CandidateJobService candidateJobService;
    private final JobRecommendService jobRecommendService;
    private final com.item.service.InviteInterviewService inviteInterviewService;
    private final com.item.service.InviteApplyService inviteApplyService;
    private final BatchInviteInterviewService batchInviteInterviewService;

    /**
     * 应聘者列表接口  获取所有公司job
     *
     * @param candidateJobListReqDTO
     * @return
     */
    @PostMapping("/jobList")
    public Pager<JobListVO> getJobList(@RequestBody CandidateJobListReqDTO candidateJobListReqDTO) {
        JobDto jobDto = new JobDto();
        if (candidateJobListReqDTO.getLocationId() > 0) {
            jobDto.setLocationId(candidateJobListReqDTO.getLocationId());
        }
        if (CollectionUtils.isNotEmpty(candidateJobListReqDTO.getJobTypeIds())) {
            jobDto.setQJobTypeIds(candidateJobListReqDTO.getJobTypeIds());
        }
        if (candidateJobListReqDTO.getJobCategoryId() > 0) {
            jobDto.setJobCategoryId(candidateJobListReqDTO.getJobCategoryId());
            jobDto.setCategoryId(candidateJobListReqDTO.getJobCategoryId());
        }
        if (CollectionUtils.isNotEmpty(candidateJobListReqDTO.getSalaryTypes())) {
            jobDto.setQSalaryTypes(candidateJobListReqDTO.getSalaryTypes());
        }
        if (CollectionUtils.isNotEmpty(candidateJobListReqDTO.getTypeIds())) {
            jobDto.setQTypeIds(candidateJobListReqDTO.getTypeIds());
        }
        if (CollectionUtils.isNotEmpty(candidateJobListReqDTO.getCategoryIds())) {
            jobDto.setQCategoryIds(candidateJobListReqDTO.getCategoryIds());
        }
        if (CollectionUtils.isNotEmpty(candidateJobListReqDTO.getModeIds())) {
            jobDto.setQModeIds(candidateJobListReqDTO.getModeIds());
        }
        // ========== 新增：设置地理位置参数 ==========
        jobDto.setUserLatitude(candidateJobListReqDTO.getLatitude());
        jobDto.setUserLongitude(candidateJobListReqDTO.getLongitude());
//        jobDto.setJobStatus(JobStatus.ACTIVE.getCode());
        jobDto.setQJobStatus(Collections.singletonList(JobStatus.ACTIVE.getCode()));
        return jobDomainService.searchJobsFromEs(candidateJobListReqDTO.getPageIndex(), candidateJobListReqDTO.getPageSize(), candidateJobListReqDTO.getKeyword(), jobDto, candidateJobListReqDTO.getDatePosted());
    }

    /**
     * 应聘者根据分享连接 进入查看jobList
     *
     * @param companyCode
     * @param pageIndex
     * @param pageSize
     * @param locationId
     * @param jobTypeId
     * @param jobCategoryId
     * @param datePosted
     * @return
     */
    @GetMapping("/jobListByCompany/{companyCode}")
    public JobShareListVO getJobListByCompany(@PathVariable(value = "companyCode") String companyCode,
                                            @RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                            @RequestParam(value = "locationId", required = false, defaultValue = "0") int locationId,
                                            @RequestParam(value = "jobTypeId", required = false, defaultValue = "0") int jobTypeId,
                                            @RequestParam(value = "jobCategoryId", required = false, defaultValue = "0") int jobCategoryId,
                                            @RequestParam(value = "datePosted", required = false, defaultValue = "-1") int datePosted,
                                            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword) {
        return getJobListCompanyInfo(companyCode, pageIndex, pageSize, locationId, jobTypeId, jobCategoryId, datePosted, keyword);
    }

    /**
     * 招聘者列表
     *
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/recruiter/job-list")
    public Pager<JobVO> getRecruitJobList(@RequestBody RecruitJobListReqDTO req) {
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        JobDto jobDto = new JobDto();
        jobDto.setCompanyCode(currentUserNeedLogin.getCompanyCode());
        if (CollectionUtils.isNotEmpty(req.getJobStatus())) {
            jobDto.setQJobStatus(req.getJobStatus());
        }
        if (CollectionUtils.isNotEmpty(req.getSalaryTypes())) {
            jobDto.setQSalaryTypes(req.getSalaryTypes());
        }
        if (CollectionUtils.isNotEmpty(req.getTypeIds())) {
            jobDto.setQTypeIds(req.getTypeIds());
        }
        if (CollectionUtils.isNotEmpty(req.getCategoryIds())) {
            jobDto.setQCategoryIds(req.getCategoryIds());
        }
        if (CollectionUtils.isNotEmpty(req.getModeIds())) {
            jobDto.setQModeIds(req.getModeIds());
        }

        Pager<JobListVO> jobListVOPager = jobDomainService.searchJobsFromEs(req.getPageIndex(), req.getPageSize(), req.getKeyword(), jobDto, req.getDatePosted());
        return Pager.build(jobListVOPager, JobConverter.INSTANCE::convert2JobVO);
    }

    @GetMapping("/allJobTypes")
    public List<JobTypeVo> getAllJobTypes() {
        return JobTypeConverter.INSTANCE.convertToListVo(jobService.getAllJobTypes());
    }

    /**
     * 发布职位
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/publication")
    public JobCreateResponseVO createJob(@RequestBody @Validated JobCreateRequestVO vo) {
        JobCreateDTO dto = jobConvert.toJobCreateDTO(vo);
        return jobDomainService.publishJob(dto);
    }

    @GetMapping("/allJobCategories")
    public List<JobCategoryVo> getAllJobCategories() {
        return JobCategoryConverter.INSTANCE.convertToListVo(jobService.getAllJobCategories());

    }

    /**
     * 已废弃 应聘者 职位详情 根据id查询
     */
    @Deprecated
    @GetMapping("/detail/{jobId}")
    public JobDetailVO detailJob(@PathVariable Long jobId) {
        return jobDomainService.getJobDetailApplicationById(jobId);
    }

    /**
     * 应聘者 职位详情 根据id查询
     */
    @GetMapping("/candidate/detail/{jobId}")
    public JobShareDetailVO candidateDetailJob(@PathVariable Long jobId) {
        return getCandidateJobDetailCompanyInfo(jobId);
    }

    /**
     * 应聘者|招聘者 职位详情 根据分享连接查询  底层没有鉴权 没有获取当前登录人信息 裸接口
     * https://aaaa.com/rjob/companyName-title-jobUniq
     * jobCode 需要companyName-title-jobUniq字符串，并且进行 URL 编码 防止中文问题；
     */
    @GetMapping("/detail/info/{jobCode}")
    public JobShareDetailVO detailJobCode(@PathVariable String jobCode) {
        return getJobDetailCompanyInfo(jobCode);
    }

    /**
     * 招聘者 职位详情 根据id查询
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/detail-all/{jobId}")
    public JobDetailVO detailJobAll(@PathVariable Long jobId) {
        return jobDomainService.getJobDetailRecruiterById(jobId);
    }

    /**
     * 招聘者 职位详情 根据id查询 下拉中选择后调用获取详情
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/option/detail-all/{jobId}")
    public JobDetailVO optionDetailJobAll(@PathVariable Long jobId) {
        return jobDomainService.getJobDetailRecruiterById(jobId);
    }

    @GetMapping("/allLocations")
    public List<LocationVo> getAllLocations() {
        return LocationConverter.INSTANCE.convertToListVo(jobService.getAllLocations());
    }

    /**
     * locationType字典
     *
     * @return
     */
    @GetMapping("/all/location-type")
    public List<LocationTypeVO> getAllLocationType() {
        return jobDomainService.getAllLocationTypes();
    }

    /**
     * Get all salary types
     *
     * @return List of SalaryTypeVo
     */
    @GetMapping("/salary-types")
    public List<SalaryTypeVO> getAllSalaryTypes() {
        return SalaryTypeVO.convert2VO(jobDomainService.getAllSalaryTypes());
    }

    /**
     * Get all currency types
     *
     * @return List of CurrencyTypeVo
     */
    @GetMapping("/currency-types")
    public List<CurrencyTypeVO> getAllCurrencyTypes() {
        return CurrencyTypeVO.convert2VO(jobDomainService.getAllCurrencyTypes());
    }

    /**
     * Update job info by jobId
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PutMapping("/edition")
    public JobUpdateResponseVO updateJob(@RequestBody @Validated JobUpdateRequestVO vo) {
        JobUpdateDTO dto = jobConvert.toJobUpdateDTO(vo);
        return jobDomainService.updateJob(dto);
    }

    /**
     * Generate company share link by master account ID
     *
     *  招聘者登录token中获取 companyCode信息
     * @return generated share link
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/list-link")
    public String generateCompanyShareLink() {
        return jobDomainService.generateCompanyShareLink();
    }

    /**
     * Generate job info share link by job ID
     *
     * @param jobId job ID
     * @return generated job info share link
     */
    @GetMapping("/info-link/{jobId}")
    public String generateJobInfoShareLink(@PathVariable Long jobId) {
        return jobDomainService.generateJobInfoShareLink(jobId);
    }

    /**
     * Generate job info share link by job ID 无域名
     *
     * @param jobId job ID
     * @return generated job info share link
     */
    @GetMapping("/simple/info-link/{jobId}")
    public String generateJobInfoSimpleShareLink(@PathVariable Long jobId) {
        return jobDomainService.generateJobInfoSimpleShareLink(jobId);
    }

    /**
     * Update job status by job ID
     *
     * @param request job status update request
     * @return true if update successful, false otherwise
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PutMapping("/status")
    public Boolean updateJobStatus(@RequestBody @Validated JobStatusUpdateRequestVO request) {
        return jobDomainService.updateJobStatus(request.getJobId(), request.getJobStatus(), request.getComment());
    }

    /**
     * Delete job by job ID
     *
     * @param jobId job ID to delete
     * @return true if deletion successful, false otherwise
     */
    @Auth(roleType = RoleType.SUB_USER)
    @DeleteMapping("/{jobId}")
    public Boolean deleteJob(@PathVariable Long jobId) {
        return jobDomainService.deleteJob(jobId);
    }

    /**
     * 获取当前companyCode下的job列表
     *
     * 只有jobId和title数据 用于下拉选项显示
     *
     * @param query
     * @return
     */
//    @Auth(roleType = RoleType.SUB_USER)
//    @PostMapping("/option")
//    public Pager<JobOptionVO> jobOption(@RequestBody JobOptionDTO query) {
//        return jobDomainService.listJobOption(query);
//    }


    /**
     * 已经废弃 Ai生成job 信息
     *
     * @param jobTitle
     * @param extraAi
     * @return
     */
    @Deprecated
    @GetMapping("/generate-job")
    public AIJobResponseDataVO generateJob(@RequestParam("jobTitle") String jobTitle, @RequestParam(value = "extraAi", required = false, defaultValue = "") String extraAi){
        return jobConvert.toAIJobResponseDataVO(aiService.generateJob(GenerateJobRequestDTO.builder().jobTitle(jobTitle).extraInfo(extraAi).build()));

    }

    /**
     * Ai生成job description信息
     *
     * @param generateJobDesc
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/generation/job-description")
    public AIJobResponseDataVO generateJobDesc(@RequestBody @Validated GenerateJobDescDTO generateJobDesc){
        return jobConvert.toAIJobResponseDataVO(aiService.generateJob(jobConvert.toGenerateJobRequestDTO(generateJobDesc)));
    }

    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/pdf-generate-job-description")
    public AIJobDescriptionVO generateJobDescription(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(GlobalStatusCode.PARAM_MISSING,"The resume file cannot be empty.");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR,"The resume file must not exceed 10MB.");
        }
        JobDescriptionDTO jobDescriptionDTO = aiService.pdfGenerateJobDescription(file);
        return jobConvert.toAIJobDescriptionVO(jobDescriptionDTO);
    }

    private JobShareDetailVO getJobDetailCompanyInfo(String jobCode) {
        JobDetailVO jobDetail = jobDomainService.getJobDetailByIdStr(jobCode);
        if (jobDetail == null) {
            return new JobShareDetailVO();
        }
        String companyCode = jobDetail.getCompanyCode();
        if(StringUtils.isBlank(companyCode)) {
            JobShareDetailVO jobShareDetailVO = new JobShareDetailVO();
            jobShareDetailVO.setJob(jobDetail);
            return jobShareDetailVO;
        }
        CompanyInfoVO companyInfoByCode = companyDomainService.getCompanyInfoByCode(companyCode);
        JobShareDetailVO jobShareDetailVO = new JobShareDetailVO();
        jobShareDetailVO.setJob(jobDetail);
        jobShareDetailVO.setCompany(companyInfoByCode);
        return jobShareDetailVO;
    }

    private JobShareDetailVO getCandidateJobDetailCompanyInfo(Long jobId) {
        JobDetailVO jobDetail = jobDomainService.getJobDetailApplicationById(jobId);
        if (jobDetail == null) {
            return new JobShareDetailVO();
        }
        String companyCode = jobDetail.getCompanyCode();
        if(StringUtils.isBlank(companyCode)) {
            JobShareDetailVO jobShareDetailVO = new JobShareDetailVO();
            jobShareDetailVO.setJob(jobDetail);
            return jobShareDetailVO;
        }
        CompanyInfoVO companyInfoByCode = companyDomainService.getCompanyInfoByCode(companyCode);
        JobShareDetailVO jobShareDetailVO = new JobShareDetailVO();
        jobShareDetailVO.setJob(jobDetail);
        jobShareDetailVO.setCompany(companyInfoByCode);
        return jobShareDetailVO;
    }

    private JobShareListVO getJobListCompanyInfo(String companyCode,
                                                 int pageIndex,
                                                 int pageSize,
                                                 int locationId,
                                                 int jobTypeId,
                                                 int jobCategoryId,
                                                 int datePosted,
                                                 String keyword){
        JobShareListVO jobShareListVO = new JobShareListVO();
        if (Strings.isNullOrEmpty(companyCode)) {
            return jobShareListVO;
        }
        String companyCodeReal = CommonUtils.splitterStrGetLast(companyCode);
        JobDto jobDto = new JobDto();
        jobDto.setCompanyCode(companyCodeReal);
        jobDto.setQJobStatus(Collections.singletonList(JobStatus.ACTIVE.getCode()));
        Pager<JobListVO> jobListVOPager = jobDomainService.searchJobsFromEs(pageIndex, pageSize, keyword, jobDto, datePosted);
        Pager<JobVO> pager = Pager.build(jobListVOPager, JobConverter.INSTANCE::convert2JobVO);
        jobShareListVO.setJobPager(pager);
        CompanyInfoVO companyInfoByCode = companyDomainService.getCompanyInfoByCode(companyCodeReal);
        jobShareListVO.setCompany(companyInfoByCode);
        return jobShareListVO;
    }

    /**
     * 获取历史职位列表（支持搜索 分页） 必须登录态
     * 
     * @param requestDTO 请求参数
     * @return 历史职位列表
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/history/list")
    public Pager<JobOptionVO> getHistoryJobList(@RequestBody @Validated JobHistoryListRequestDTO requestDTO) {
        return jobDomainService.getHistoryJobList(requestDTO);
    }

    /**
     * 根据jobId获取历史职位详情  接口路劲参数是 通过/job/history/list接口返回的jobId  必须登录态
     * 
     * @param jobId 职位ID
     * @return 职位详情
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/history/detail/{jobId}")
    public JobHistoryDetailVO getHistoryJobDetail(@PathVariable Long jobId) {
        return jobDomainService.getHistoryJobDetail(jobId);
    }

    /**
     * 随机job
     * Get random job recommendations
     * Returns a list of random job recommendations based on the provided filters
     * 
     * @param request random job recommendation request parameters
     * @return list of random job recommendations
     */
    @PostMapping("/random-recommendations")
    public List<JobListVO> getRandomJobRecommendations(@RequestBody @Validated RandomJobRecommendRequestDTO request) {
        return jobDomainService.getRandomJobRecommendations(request);
    }

    /**
     * Check if interview email has been sent for a job
     * 
     * @param jobId job ID
     * @return true if interview email has been sent, false otherwise
     */
    @GetMapping("/has-interview-sent/{jobId}")
    public Boolean hasInterviewMailSent(@PathVariable Long jobId) {
        return candidateJobService.hasInterviewMailSent(jobId);
    }

    /**
     * 根据职位推荐候选人
     * 
     * @param jobId 职位ID
     * @return 推荐的候选人列表
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/recommend-candidates/{jobId}")
    public List<RecommendCandidateVO> recommendCandidates(@PathVariable Long jobId) {
        return jobRecommendService.recommendCandidates(jobId);
    }

    /**
     * 获取工作岗位推荐候选人的人数
     *
     * @param dto 岗位id列表
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/recommend-candidate/count")
    public List<RecommendCandidateCountVO> recommendCandidateCount(@RequestBody RecommendCandidateCountRequestDTO dto) {
        return jobRecommendService.recommendCandidateCount(dto.getJobIds());
    }

    /**
     * 邀请面试接口
     * HR邀请外部候选人面试，支持IAM账号创建、职位申请创建和简历匹配流程
     * 注意：简历解析由前端调用"/resume-parsing"接口完成，解析结果通过parsedResume参数回传
     *
     * @param request 邀请面试请求，包含jobId、candidateEmail、candidateName和可选的parsedResume
     * @return 邀请面试响应，包含申请ID和候选人ID
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/invite-interview")
    public InviteInterviewResponseVO inviteInterview(@RequestBody @Validated InviteInterviewRequestVO request) {
        
        log.info("inviteInterview request: jobId={}, candidateEmail={}, candidateName={}, hasParsedResume={}", 
                request.getJobId(), request.getCandidateEmail(), request.getCandidateName(), 
                request.getParsedResume() != null);
        
        // 调用Service处理业务逻辑
        return inviteInterviewService.inviteInterview(request);
    }

    /**
     * 批量邀请面试接口
     * HR批量邀请多个外部候选人面试，使用并行处理提高效率
     * 每个邀请独立处理，单个失败不影响其他邀请
     * 注意：简历解析由前端调用"/resume-parsing"接口完成，解析结果通过parsedResume参数回传
     *
     * @param request 批量邀请面试请求，包含多个候选人的邀请信息
     * @return 批量邀请面试响应，包含成功和失败的详细统计
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/batch-invite-interview")
    public BatchInviteInterviewResponseVO batchInviteInterview(@RequestBody @Validated BatchInviteInterviewRequestVO request) {
        
        log.info("batchInviteInterview request: inviteCount={}", request.getInviteList().size());
        
        // 调用Service处理批量业务逻辑
        return batchInviteInterviewService.batchInviteInterview(request);
    }

    /**
     * 邀请投递接口
     * HR邀请外部候选人投递职位，支持IAM账号创建和邀请投递邮件发送
     * 候选人使用邮件中的登录信息登录系统后自行投递职位
     *
     * @param request 邀请投递请求，包含jobId、candidateEmail、candidateName
     * @return 邀请投递响应，包含候选人ID、IAM账号和消息
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/invite-apply")
    public InviteApplyResponseVO inviteApply(@RequestBody @Validated InviteApplyRequestVO request) {
        
        log.info("inviteApply request: jobId={}, candidateEmail={}, candidateName={}",
                request.getJobId(), request.getCandidateEmail(), request.getCandidateName());
        
        // 调用Service处理业务逻辑
        return inviteApplyService.inviteApply(request);
    }
}