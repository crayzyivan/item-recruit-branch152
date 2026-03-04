package com.item.service.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.convert.ReportDailyConverter;
import com.item.dto.report.ReportDailyDTO;
import com.item.dto.report.TopReportDTO;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.entity.ReportDailyEntity;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.ReportType;
import com.item.mapper.ReportDailyMapper;
import com.item.service.CandidateJobService;
import com.item.service.JobService;
import com.item.service.ReportService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportDailyMapper, ReportDailyEntity> implements ReportService {
    private final JobService jobService;
    private final CandidateJobService candidateJobService;

    @Override
    public List<ReportDailyDTO> getDailyReport(String companyCode, ReportType type) {
        QueryWrapper<ReportDailyEntity> queryWrapper = new QueryWrapper<>();
        LocalDate date = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = LocalDate.now();
        if (type == null) {
            type = ReportType.LAST7DAYS;
        }
        int dataCount = 0;
        if (type == ReportType.LAST7DAYS) {
            startDate = date.minusDays(8);
            dataCount = 7;
        } else if (type == ReportType.LAST30DAYS) {
            startDate = date.minusDays(31);
            dataCount = 30;
        } else if (type == ReportType.LAST3MONTHS) {
            startDate = date.minusDays(91);
            dataCount = 90;
        }
        queryWrapper
                .lambda()
                .orderByAsc(ReportDailyEntity::getCreateTime)
                .eq(ReportDailyEntity::getCompanyCode, companyCode)
                .ge(ReportDailyEntity::getCreateTime,startDate)
                .lt(ReportDailyEntity::getCreateTime,endDate);

        List<ReportDailyDTO> results =
                ReportDailyConverter.INSTANCE.convertEntityListToDTOList( this.list(queryWrapper));
        if (results.stream().count() == dataCount) {
            return results;
        }

        Map<LocalDate,ReportDailyDTO> zeroCountMap = new HashMap<>();

        LocalDate finalStartDate = startDate;
        IntStream
                .rangeClosed(0, dataCount - 1)
                .forEach(i -> {
                    ReportDailyDTO dto = new ReportDailyDTO();
                    dto.setCreateTime(finalStartDate.plusDays(i));
                    dto.setApplyCount(0L);
                    dto.setDeniedCount(0L);
                    dto.setOnHoldCount(0L);
                    dto.setAcceptCount(0L);
                    zeroCountMap.put(dto.getCreateTime(), dto);
        });
        results.forEach(result -> zeroCountMap.remove(result.getCreateTime()));

        results.addAll(zeroCountMap.values());

        return results.stream().sorted(Comparator.comparing(ReportDailyDTO::getCreateTime)).collect(Collectors.toList());
    }



    @Override
    public TopReportDTO getTopReport(String companyCode) {
        TopReportDTO topReportDTO = new TopReportDTO();
        var openJobCountFuture = CompletableFuture.runAsync(() -> {
            LambdaQueryWrapper<JobEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(JobEntity::getCompanyCode, companyCode)
                    .eq(JobEntity::getJobStatus, 1);
            topReportDTO.setOpenJobTotalCount(jobService.count(queryWrapper));
        });

        var applicationTotalCountFuture = CompletableFuture.runAsync(() -> {
            LambdaQueryWrapper<CandidateJobEntity> candidateQueryWrapper = new LambdaQueryWrapper<>();
            candidateQueryWrapper.eq(CandidateJobEntity::getCompanyCode, companyCode);
            topReportDTO.setApplicationTotalCount(candidateJobService.count(candidateQueryWrapper));
        });

        var pendingCountFuture = CompletableFuture.runAsync(() -> topReportDTO.setPendingReviewCount(
                candidateJobService.count(new LambdaQueryWrapper<CandidateJobEntity>()
                                            .eq(CandidateJobEntity::getCompanyCode, companyCode)
                                            .eq(CandidateJobEntity::getApplyStatus, JobApplyStatus.REVIEW.getCode()))));

        var deniedCountFuture = CompletableFuture.runAsync(() -> topReportDTO.setDeniedTotalCount(candidateJobService.count(
                new LambdaQueryWrapper<CandidateJobEntity>()
                .eq(CandidateJobEntity::getCompanyCode, companyCode)
                .eq(CandidateJobEntity::getApplyStatus, JobApplyStatus.DENIED.getCode()))));

        var readyCountFuture = CompletableFuture.runAsync(() -> topReportDTO.setReadyTotalCount(candidateJobService.count(
                new LambdaQueryWrapper<CandidateJobEntity>()
                .eq(CandidateJobEntity::getCompanyCode, companyCode)
                .eq(CandidateJobEntity::getApplyStatus, JobApplyStatus.READY.getCode()))));

        CompletableFuture.allOf(openJobCountFuture,
                applicationTotalCountFuture,
                pendingCountFuture,
                deniedCountFuture,
                readyCountFuture).join();


        return topReportDTO;
    }
}
