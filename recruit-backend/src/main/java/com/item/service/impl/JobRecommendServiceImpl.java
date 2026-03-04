package com.item.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.item.entity.CandidateEsEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.RecommendationCandidateJobEntity;
import com.item.es.JobEsService;
import com.item.es.ResumeEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.framework.utils.MDCThreadPoolExecutor;
import com.item.service.CandidateJobService;
import com.item.service.JobRecommendService;
import com.item.service.LocationService;
import com.item.service.RecommendationCandidateJobService;
import com.item.vo.RecommendCandidateCountVO;
import com.item.vo.RecommendCandidateVO;
import com.item.vo.ai.JobMatchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 职位推荐候选人服务实现类
 *
 * @author system
 * @since 2025-10-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobRecommendServiceImpl implements JobRecommendService {

    private final CandidateJobService candidateJobService;
    private final JobEsService jobEsService;
    private final ResumeEsService resumeEsService;
    private final RecommendationCandidateJobService recommendationCandidateJobService;
    private final LocationService locationService;
    private final RecruitCommonNacosConfig recruitCommonNacosConfig;
    private static final ThreadPoolExecutor RECOMMEND_CANDIDATE_COUNT_POOL_EXECUTOR = new MDCThreadPoolExecutor(20, 30, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(500), new ThreadFactoryBuilder()
            .setNameFormat("recommendCandidateCount-%d")
            .build());

    @Override
    public List<RecommendCandidateVO> recommendCandidates(Long jobId) {
        log.info("Starting candidate recommendation for jobId: {}", jobId);
        try {
            // Step 1: 查询已投递候选人ID列表
            Set<Long> appliedCandidateIds = getAppliedCandidateIds(jobId);
            List<RecommendationCandidateJobEntity> recommendationCandidateJobList = recommendationCandidateJobService.listByJobIds(Collections.singletonList(jobId));
            if (CollectionUtils.isNotEmpty(recommendationCandidateJobList)){
                Set<Long> candidateIds=recommendationCandidateJobList.stream().map(RecommendationCandidateJobEntity::getCandidateId).collect(Collectors.toSet());
                appliedCandidateIds.addAll(candidateIds);
            }

            // Step 2: 获取职位信息
            JobEsEntity jobEntity = jobEsService.getJobById(jobId);
            if (jobEntity == null) {
                return Collections.emptyList();
            }
            
            // Step 3: 根据jobTitle匹配度查询100条候选人记录并去重
            Set<String> excludeCompanyCodeInRecommendCandidates = recruitCommonNacosConfig.getExcludeCompanyCodeInRecommendCandidates();
            List<RecommendCandidateVO> titleMatchedCandidates = searchCandidatesByTitle(jobEntity.getTitle(), appliedCandidateIds, excludeCompanyCodeInRecommendCandidates,100);
            log.info("Found {} candidates matched by title for jobId: {}, excludeCompanyCodeInRecommendCandidates {}", titleMatchedCandidates.size(), jobId, excludeCompanyCodeInRecommendCandidates);

            // Step 4: 判断记录数是否<=10条
            if (titleMatchedCandidates.size() <= 10) {
                return titleMatchedCandidates;
            }

            Set<Long> candidateIds=titleMatchedCandidates.stream().map(RecommendCandidateVO::getCandidateId).collect(Collectors.toSet());

            // Step 5: 如果>10条，根据skill匹配employmentHistories获取10条记录
            List<RecommendCandidateVO> skillMatchedCandidates = searchCandidatesBySkills(jobEntity.getSkills(), candidateIds, 10);
            if (CollectionUtils.isEmpty(skillMatchedCandidates) || skillMatchedCandidates.size()<10){
                skillMatchedCandidates= titleMatchedCandidates.stream()
                        .limit(10)
                        .collect(Collectors.toList());
            }
            log.info("Found {} candidates matched by skills for jobId: {}", skillMatchedCandidates.size(), jobId);
            return skillMatchedCandidates;
        } catch (Exception e) {
            log.error("Error occurred while recommending candidates for jobId: {}", jobId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendCandidateCountVO> recommendCandidateCount(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return Collections.emptyList();
        }
        Map<Long, Set<Long>> appliedCandidateIds = getAppliedCandidateIds(jobIds);
        Map<Long, Set<Long>> recommendationCandidate = getRecommendationCandidate(jobIds);
        List<JobEsEntity> jobByIds = jobEsService.getJobByIds(jobIds);
        Map<Long, JobEsEntity> jobEsEntityMap = jobByIds.stream().collect(Collectors.toMap(JobEsEntity::getId, Function.identity(), (v1, v2) -> v2));
        List<RecommendCandidateCountVO> resultList = new CopyOnWriteArrayList<>();
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        for (Long jobId : jobIds) {
            JobEsEntity jobEsEntity = jobEsEntityMap.get(jobId);
            Set<Long> candidateIds = appliedCandidateIds.getOrDefault(jobId, new HashSet<>());
            Set<Long> recommendationCandidateIds = recommendationCandidate.getOrDefault(jobId, new HashSet<>());
            candidateIds.addAll(recommendationCandidateIds);
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                int recommendationCandidateCount = getRecommendationCandidateCount(jobEsEntity, candidateIds);
                resultList.add(RecommendCandidateCountVO.builder().jobId(jobId).recommendCandidateCount(recommendationCandidateCount).build());
            }, RECOMMEND_CANDIDATE_COUNT_POOL_EXECUTOR);
            completableFutures.add(completableFuture);
        }
        try {
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(recruitCommonNacosConfig.getRecommendCandidateCoundTimeOut(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for recommendation candidate count", e);
        } catch (ExecutionException e) {
            log.warn("Error occurred while waiting for recommendation candidate count", e);
        } catch (TimeoutException e) {
            log.warn("Timeout while waiting for recommendation candidate count", e);
        } catch (Exception e) {
            log.error("Error occurred while waiting for recommendation candidate count", e);
        }
        return resultList;
    }

    public int getRecommendationCandidateCount(JobEsEntity jobEsEntity, Set<Long> excludeCandidateIds) {
        if (jobEsEntity == null) {
            return 0;
        }
        // Step 3: 根据jobTitle匹配度查询100条候选人记录并去重
        Set<String> excludeCompanyCodeInRecommendCandidates = recruitCommonNacosConfig.getExcludeCompanyCodeInRecommendCandidates();
        List<RecommendCandidateVO> titleMatchedCandidates = searchCandidatesByTitle(jobEsEntity.getTitle(), excludeCandidateIds, excludeCompanyCodeInRecommendCandidates,100);

        // Step 4: 判断记录数是否<=10条
        if (titleMatchedCandidates.size() <= 10) {
            log.info("titleMatchedCandidates is <= 10 {}", titleMatchedCandidates.size());
            return titleMatchedCandidates.size();
        }

        Set<Long> candidateFirstIds = titleMatchedCandidates.stream().map(RecommendCandidateVO::getCandidateId).collect(Collectors.toSet());

        // Step 5: 如果>10条，根据skill匹配employmentHistories获取10条记录
        List<RecommendCandidateVO> skillMatchedCandidates = searchCandidatesBySkills(jobEsEntity.getSkills(), candidateFirstIds, 10);
        if (CollectionUtils.isEmpty(skillMatchedCandidates) || skillMatchedCandidates.size() < 10) {
            log.info("skillMatchedCandidates is empty or <= 10 {}", titleMatchedCandidates.size());
            return Math.min(titleMatchedCandidates.size(), 10);
        }
        log.info("skillMatchedCandidates is not empty or > 10 {}", skillMatchedCandidates.size());
        return Math.min(skillMatchedCandidates.size(), 10);
    }

    /**
     * 获取已投递候选人ID列表
     */
    private Set<Long> getAppliedCandidateIds(Long jobId) {
        List<CandidateJobEntity> appliedCandidates = candidateJobService.listByJobIds(Set.of(jobId));
        if (CollectionUtils.isEmpty(appliedCandidates)){return new  HashSet<>();}
        return appliedCandidates.stream()
                .map(CandidateJobEntity::getCandidateId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取推荐候选人ID列表
     * 按照jobId分组，返回Map<Long, Set<Long>>，key为jobId，value为该职位对应的推荐候选人ID集合
     */
    private Map<Long, Set<Long>> getRecommendationCandidate(Collection<Long> jobIds){
        List<Long> jobIdList = jobIds.stream().distinct().toList();
        List<RecommendationCandidateJobEntity> recommendationCandidateJobList = recommendationCandidateJobService.listByJobIds(jobIdList);
        if (CollectionUtils.isEmpty(recommendationCandidateJobList)) {
            return Map.of();
        }
        // 按照jobId分组，每个jobId对应一个Set<Long>存放candidateId
        return recommendationCandidateJobList.stream()
                .collect(Collectors.groupingBy(
                        RecommendationCandidateJobEntity::getJobId,
                        Collectors.mapping(
                                RecommendationCandidateJobEntity::getCandidateId,
                                Collectors.toSet()
                        )
                ));
    }

    /**
     * 获取已投递候选人ID列表
     * 按照jobId分组，返回Map<Long, Set<Long>>，key为jobId，value为该职位对应的候选人ID集合
     */
    private Map<Long, Set<Long>> getAppliedCandidateIds(Collection<Long> jobIds) {
        Set<Long> jobIdSet = new HashSet<>(jobIds);
        List<CandidateJobEntity> appliedCandidates = candidateJobService.listByJobIds(jobIdSet);
        if (CollectionUtils.isEmpty(appliedCandidates)) {
            return Map.of();
        }
        // 按照jobId分组，每个jobId对应一个Set<Long>存放candidateId
        return appliedCandidates.stream()
                .collect(Collectors.groupingBy(
                        CandidateJobEntity::getJobId,
                        Collectors.mapping(
                                CandidateJobEntity::getCandidateId,
                                Collectors.toSet()
                        )
                ));
    }


    /**
     * 根据职位标题搜索候选人
     */
    private List<RecommendCandidateVO> searchCandidatesByTitle(String jobTitle, Set<Long> excludeCandidateIds, Set<String> excludeCompanyCode, int limit) {
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // 使用新的ES搜索方法，查询JobMatchResultVO
        List<JobMatchResultVO> jobMatchResults = resumeEsService.searchRecommendCandidatesByTitle(jobTitle, excludeCandidateIds, excludeCompanyCode, limit);
        List<Long> cityIds=jobMatchResults.stream().map(JobMatchResultVO::getCandidateCityId).toList();
        List<Long> stateIds=jobMatchResults.stream().map(JobMatchResultVO::getCandidateStateId).toList();
        List<Long> countryIds=jobMatchResults.stream().map(JobMatchResultVO::getCandidateCountryId).toList();
        Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(cityIds);
        Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(stateIds);
        Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);
        // 转换为RecommendCandidateVO
        return jobMatchResults.stream()
                .map(c->convertJobMatchResultToRecommendVO(c,cityMap,stateMap,countryMap))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据技能搜索候选人
     */
    private List<RecommendCandidateVO> searchCandidatesBySkills(List<String> skills, Set<Long> excludeCandidateIds, int limit) {
        if (CollectionUtils.isEmpty(skills)) {
            return Collections.emptyList();
        }
        
        log.info("Searching candidates by skills: {}", skills);
        
        // 使用新的ES搜索方法
        List<CandidateEsEntity> candidates = resumeEsService.searchRecommendCandidatesByEmploymentHistory(skills, excludeCandidateIds, limit);
        List<Long> cityIds=candidates.stream().map(CandidateEsEntity::getCityId).toList();
        List<Long> stateIds=candidates.stream().map(CandidateEsEntity::getStateId).toList();
        List<Long> countryIds=candidates.stream().map(CandidateEsEntity::getCountryId).toList();
        Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(cityIds);
        Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(stateIds);
        Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);

        // 转换为RecommendCandidateVO
        return candidates.stream()
                .map(c->convertCandidateEsToRecommendVO(c,cityMap,stateMap,countryMap))
                .collect(Collectors.toList());
    }
    
    /**
     * 将JobMatchResultVO转换为RecommendCandidateVO
     */
    private RecommendCandidateVO convertJobMatchResultToRecommendVO(JobMatchResultVO jobMatchResult,Map<Long, String> cityMap,
                                                                    Map<Long, String> stateMap,Map<Long, String> countryMap) {
        RecommendCandidateVO recommendVO = new RecommendCandidateVO();
        recommendVO.setCandidateId(jobMatchResult.getCandidateId());
        recommendVO.setCandidateName(jobMatchResult.getCandidateName());
        recommendVO.setCandidateEmail(jobMatchResult.getCandidateEmail());
        recommendVO.setCityName(cityMap.get(jobMatchResult.getCandidateCityId()));
        recommendVO.setStateName(stateMap.get(jobMatchResult.getCandidateStateId()));
        recommendVO.setCountryName(countryMap.get(jobMatchResult.getCandidateCountryId()));
        return recommendVO;
    }
    
    /**
     * 将CandidateEsEntity转换为RecommendCandidateVO
     */
    private RecommendCandidateVO convertCandidateEsToRecommendVO(CandidateEsEntity candidateEs,Map<Long, String> cityMap,
                                                                 Map<Long, String> stateMap,Map<Long, String> countryMap) {
        RecommendCandidateVO recommendVO = new RecommendCandidateVO();
        recommendVO.setCandidateId(candidateEs.getId());
        recommendVO.setCandidateName(candidateEs.getCandidateName());
        recommendVO.setCandidateEmail(candidateEs.getCandidateEmail());
        recommendVO.setCityName(cityMap.get(candidateEs.getCityId()));
        recommendVO.setStateName(stateMap.get(candidateEs.getStateId()));
        recommendVO.setCountryName(countryMap.get(candidateEs.getCountryId()));
        return recommendVO;
    }
}
