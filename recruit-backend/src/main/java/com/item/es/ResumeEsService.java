package com.item.es;

import com.item.dto.report.CandidateSimpleDTO;
import com.item.entity.CandidateEsEntity;
import com.item.framework.http.Pager;
import com.item.vo.CandidateJobQueryVO;
import com.item.vo.CandidateJobVO;
import com.item.vo.ai.JobMatchResultVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ResumeEsService {
    /**
     * 保存文本内容到ES
     */
    void saveResumeToEs(CandidateEsEntity candidateEsEntity);

    void saveResumeAiResultToEs(JobMatchResultVO jobMatchResultVO);

    CandidateEsEntity searchResumeToEs(Long candidateId);

    /**
     * 根据应聘者和岗位关联Id查询匹配结果
     * 根据应聘者和岗位关联Id查询匹配结果
     *
     * @param id
     * @return
     */
    JobMatchResultVO getMatchResultById(String id);

    Pager<CandidateSimpleDTO> getCandidateJobByCompanyCode(CandidateJobQueryVO queryVO);

    /**
     * 更新JobMatchResultVOES字段值
     *
     * @param documentId
     * @param fieldsValues
     */
    void updateMatchEsFieldValue(String documentId, Map<String, Object> fieldsValues);

    void updateCandidate(CandidateEsEntity candidateEsEntity);

    Pager<CandidateSimpleDTO> getApplicationsCandidateByCompanyCode(CandidateJobQueryVO queryVO);

    void batchUpdateByJobId(JobMatchResultVO jobMatchResultVO);

    void batchUpdateByCandidateId(JobMatchResultVO jobMatchResultVO);

    /**
     * 查询应聘者职位列表
     *
     * @param queryVO
     * @return
     */
    Pager<CandidateJobVO> queryCandidateJobVoList(CandidateJobQueryVO queryVO);

    /**
     * Get talent database candidates with permission check - RP-324
     * Check if current user is in allCandidatesUser whitelist
     * If yes, query all candidates; otherwise return empty result
     *
     * @param queryVO query parameters including pageIndex, pageSize, candidateName
     * @return paginated candidate list
     */
    Pager<CandidateSimpleDTO> getAllCandidatesByQuery(CandidateJobQueryVO queryVO);

    /**
     * Search candidates by job title matching using JobMatchResultVO
     * Used for job recommendation functionality - RP-328
     * 
     * @param jobTitle job title to search for
     * @param excludeCandidateIds candidate IDs to exclude from results
     * @param limit maximum number of results to return
     * @return list of matching JobMatchResultVO records
     */
    List<JobMatchResultVO> searchRecommendCandidatesByTitle(String jobTitle, Set<Long> excludeCandidateIds, Set<String> excludeCompanyCode, int limit);

    /**
     * Search candidates by employment history skills matching
     * Used for job recommendation functionality - RP-328
     * 
     * @param skills list of skills to search for in employment histories
     * @param includeCandidateIds candidate IDs to include from results
     * @param limit maximum number of results to return
     * @return list of matching candidates
     */
    List<CandidateEsEntity> searchRecommendCandidatesByEmploymentHistory(List<String> skills, Set<Long> includeCandidateIds, int limit);

    /**
     * 候选人投递历史
     * @param candidateId
     * @param companyCode
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Pager<JobMatchResultVO> getCandidateJobRecords(Long candidateId,String companyCode,int pageIndex, int pageSize);
}