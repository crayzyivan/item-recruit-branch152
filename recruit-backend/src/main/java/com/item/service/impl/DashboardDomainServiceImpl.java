package com.item.service.impl;

import com.item.dto.report.CandidateSimpleDTO;
import com.item.entity.CandidateJobEntity;
import com.item.es.ResumeEsService;
import com.item.framework.http.Pager;
import com.item.service.CandidateJobService;
import com.item.service.DashboardDomainService;
import com.item.service.JobStatusRecordService;
import com.item.service.LocationService;
import com.item.util.JsonUtils;
import com.item.vo.CandidateJobQueryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardDomainServiceImpl implements DashboardDomainService {
    private final ResumeEsService resumeEsService;
    private final JobStatusRecordService jobStatusRecordService;
    private final CandidateJobService candidateJobService;
    private final LocationService locationService;

    @Override
    public Pager<CandidateSimpleDTO> getCandidateJobByCompanyCode(CandidateJobQueryVO queryVO) {
        Pager<CandidateSimpleDTO> candidateJobByCompanyCode = resumeEsService.getCandidateJobByCompanyCode(queryVO);
        Set<Long> ids = candidateJobByCompanyCode.getCurrentPageRecords().stream().map(CandidateSimpleDTO::getId).collect(Collectors.toSet());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("jobStatusRecordService.listByCandidateJobIds(ids)");
        List<CandidateJobEntity> candidateJobEntities = candidateJobService.listTimeByJobIds(ids);
        stopWatch.stop();
        stopWatch.start("get apply time");
        Map<Long, CandidateJobEntity> applyMap = candidateJobEntities.stream().collect(Collectors.toMap(CandidateJobEntity::getId, Function.identity(), (v1, v2) -> v1));
        Map<String, String> placeNameMap = getPlaceName(candidateJobEntities);
        candidateJobByCompanyCode.getCurrentPageRecords().forEach(record -> {
            CandidateJobEntity candidateJobEntity = applyMap.get(record.getId());
            if (candidateJobEntity != null) {
                record.setApplyTime(candidateJobEntity.getCreateTime());
                record.setUpdateTime(candidateJobEntity.getUpdateTime());
                String jobLocation = placeNameMap.getOrDefault(candidateJobEntity.getPlaceId(), null);
                if (jobLocation != null) {
                    record.setJobLocation(jobLocation);
                }
            }
        });
        stopWatch.stop();
        log.debug("record cost : {}", stopWatch.prettyPrint());
        return candidateJobByCompanyCode;
    }
    
    public Map<String, String> getPlaceName(List<CandidateJobEntity> candidateJobEntities) {
        if (CollectionUtils.isEmpty(candidateJobEntities)) {
            return new HashMap<>();
        }
        
        List<String> placeIds = candidateJobEntities.stream()
                .map(CandidateJobEntity::getPlaceId)
                .filter(placeId -> placeId != null && !placeId.isEmpty())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(placeIds)) {
            return new HashMap<>();
        }
        
        return locationService.listIdNameMapByPlaceIds(placeIds);
    }
}
