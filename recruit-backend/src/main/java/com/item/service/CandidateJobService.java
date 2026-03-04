package com.item.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.CandidateJobCountDTO;
import com.item.dto.report.CandidateSimpleDTO;
import com.item.entity.CandidateJobEntity;
import com.item.framework.http.Pager;
import com.item.vo.ApplicationUserInfoVO;
import com.item.vo.CandidateJobQueryVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 应聘者职位关系服务接口
 */
public interface CandidateJobService extends IService<CandidateJobEntity> {
    
    /**
     * 插入职位申请记录
     *
     * @param dto 职位申请实体
     * @return 是否插入成功
     */
    boolean applyJob(CandidateJobEntity dto);

    /**
     * 根据应聘者ID和职位ID获取申请记录
     *
     * @param candidateId 应聘者ID
     * @param jobId 职位ID
     * @return 申请记录列表
     */
    List<CandidateJobEntity> getByCandidateIdAndJobId(Long candidateId, Long jobId);


    /**
     *
     * @param jobIds
     * @return
     */
    List<CandidateJobEntity> listByJobIds(Set<Long> jobIds);

    /**
     * 获取创建和更新时间
     *
     * @param ids
     * @return
     */
    List<CandidateJobEntity> listTimeByJobIds(Set<Long> ids);

    /**
     * 获取应聘者对某职位的最新申请记录
     *
     * @param candidateId 应聘者ID
     * @param jobId 职位ID
     * @return 最新申请记录
     */
    CandidateJobEntity getLatestByCandidateIdAndJobId(Long candidateId, Long jobId);

    /**
     * 更新应聘状态
     *
     * @param id 主键ID
     * @param status 新状态
     * @param reason 原因说明（可选）
     * @return 是否更新成功
     */
    boolean updateApplyStatus(Long id, Integer status, String reason);

    /**
     * 该岗位投递了多少简历
     * @param jobId
     * @return
     */
    Long applicationCount(Long jobId);

    /**
     * 批量获取职位申请统计
     * @param jobIds 职位ID集合
     * @return 每个职位的申请数量统计Map，key为jobId，value为申请数量
     */
    Map<Long, Long> batchApplicationCount(Set<Long> jobIds);

    CandidateJobCountDTO getCandidateNumber(Long jobId);

    /**
     * 分页获取应聘者职位关系列表
     * @param queryVO
     * @return
     */
    Page<CandidateJobEntity> selectCandidateJobPageList(CandidateJobQueryVO queryVO);

    List<CandidateJobEntity> checkInfo(String candidateEmail, String interviewId, Long applicationId, String candidateName);

    Pager<CandidateSimpleDTO> getCandidateJobByCompanyCode(CandidateJobQueryVO query);

    /**
     * 查询没有ai简历评分的记录
     * @return
     */
    List<CandidateJobEntity> listByAssessmentScoreIsNull();

    /**
     * 查询没有发送面试链接的记录
     * @param cutoffTime 截止时间，只查询此时间之后创建的记录
     * @return
     */
    List<CandidateJobEntity> listNotSentInterviewMailCandidates(LocalDateTime cutoffTime);

    /**
     * 查询没有预约电话面试的投递记录
     * @return
     */
    List<CandidateJobEntity> listNotBookInterviewPhoneCandidates();

    Pager<CandidateSimpleDTO> getApplicationsCandidateByCompanyCode(CandidateJobQueryVO query);

    /**
     * 获取取消面试id
     * @return
     */
    List<Long> getCancelInterviewFreezeIds();

    /**
     * 获取过期的应聘者职位关系
     * @param cooldownCutoffTime
     * @return
     */
    List<CandidateJobEntity> findExpiredCandidateJobs(LocalDateTime cooldownCutoffTime);

    /**
     * Check if interview email has been sent for a job
     * @param jobId job ID
     * @return true if interview email has been sent, false otherwise
     */
    Boolean hasInterviewMailSent(Long jobId);

    /**
     * 根据申请ID获取用户信息（租户ID、用户ID、用户姓名）
     * @param applicationId 申请ID（r_candidate_job表的主键id）
     * @return 用户信息，包含租户ID、用户ID和用户姓名
     */
    ApplicationUserInfoVO getUserInfoByApplicationId(Long applicationId);

    /**
     * 检查指定职位是否有候选人申请记录
     * @param jobId 职位ID
     * @return 是否存在申请记录
     */
    boolean hasApplicationsByJobId(Long jobId);

    /**
     * 获取投递记录
     *
     * @param id
     * @return
     */
    CandidateJobEntity getCandidateJobById(Long id);

    /**
     *
     * @param id
     * @param questionInfo
     */
    void updateCandidateQuestionInfoById(Long id, String questionInfo);
}