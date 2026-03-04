package com.item.service.impl;

import com.item.dto.iam.IamUserContextDTO;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import com.item.service.BatchInviteInterviewService;
import com.item.service.InviteInterviewService;
import com.item.util.LanguageLocalUtils;
import com.item.util.UserContextUtil;
import com.item.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchInviteInterviewServiceImpl implements BatchInviteInterviewService {

    private final InviteInterviewService inviteInterviewService;
    private final ThreadPoolTaskExecutor batchInviteTaskExecutor;

    /**
     * 批量邀请面试
     * 使用CompletableFuture实现并行处理，提高批量处理效率
     * 采用独立事务处理每个邀请，确保单个失败不影响其他邀请
     */
    @Override
    public BatchInviteInterviewResponseVO batchInviteInterview(BatchInviteInterviewRequestVO request) {
        long startTime = System.currentTimeMillis();

        log.info("Batch invite interview started: totalCount={}", request.getInviteList().size());

        // 获取当前用户上下文（在主线程中获取，避免子线程中获取失败）
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();

        // 预处理：检测重复邮箱
        Set<String> seenEmails = new HashSet<>();
        Map<Integer, String> duplicateIndexMap = new HashMap<>();

        for (int i = 0; i < request.getInviteList().size(); i++) {
            InviteInterviewRequestVO inviteRequest = request.getInviteList().get(i);
            String candidateEmail = inviteRequest.getParsedResume() != null
                    ? inviteRequest.getParsedResume().getEmail()
                    : inviteRequest.getCandidateEmail();

            if (candidateEmail != null && seenEmails.contains(candidateEmail)) {
                duplicateIndexMap.put(i, "Duplicate candidate email");
            } else if (candidateEmail != null) {
                seenEmails.add(candidateEmail);
            }
        }

        log.info("Duplicate email check completed: duplicateCount={}", duplicateIndexMap.size());

        // 使用CompletableFuture并行处理所有邀请
        List<CompletableFuture<BatchInviteInterviewItemVO>> futures = 
                IntStream.range(0, request.getInviteList().size())
                        .mapToObj(index -> {
                            InviteInterviewRequestVO inviteRequest = request.getInviteList().get(index);

                            // 如果是重复邮箱，直接返回失败结果
                            if (duplicateIndexMap.containsKey(index)) {
                                String candidateEmail = inviteRequest.getParsedResume() != null
                                        ? inviteRequest.getParsedResume().getEmail()
                                        : inviteRequest.getCandidateEmail();

                                return CompletableFuture.completedFuture(
                                        BatchInviteInterviewItemVO.builder()
                                                .index(index)
                                                .jobId(inviteRequest.getJobId())
                                                .candidateEmail(candidateEmail)
                                                .candidateName(inviteRequest.getCandidateName())
                                                .status("FAILURE")
                                                .message(duplicateIndexMap.get(index))
                                                .build()
                                );
                            }

                            // 正常处理
                            return CompletableFuture.supplyAsync(
                                    () -> processInviteWithContext(inviteRequest, currentUser, index),
                                    batchInviteTaskExecutor
                            );
                        })
                        .toList();

        // 等待所有任务完成并收集结果
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // 阻塞等待所有任务完成
        allFutures.join();

        // 收集所有结果
        List<BatchInviteInterviewItemVO> allResults = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // 分类统计结果
        List<BatchInviteInterviewItemVO> successList = allResults.stream()
                .filter(item -> "SUCCESS".equals(item.getStatus()))
                .collect(Collectors.toList());

        List<BatchInviteInterviewItemVO> failureList = allResults.stream()
                .filter(item -> "FAILURE".equals(item.getStatus()))
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        log.info("Batch invite interview completed: totalCount={}, successCount={}, failureCount={}, processingTimeMs={}",
                allResults.size(), successList.size(), failureList.size(), processingTime);

        return BatchInviteInterviewResponseVO.builder()
                .totalCount(allResults.size())
                .successCount(successList.size())
                .failureCount(failureList.size())
                .successList(successList)
                .failureList(failureList)
                .processingTimeMs(processingTime)
                .build();
    }

    /**
     * 处理单个邀请（带用户上下文）
     * 每个邀请在独立事务中处理，确保数据一致性
     */
    private BatchInviteInterviewItemVO processInviteWithContext(
            InviteInterviewRequestVO request,
            IamUserContextDTO currentUser,
            int index) {

        String candidateEmail = request.getParsedResume() != null
                ? request.getParsedResume().getEmail()
                : request.getCandidateEmail();
        String candidateName = request.getCandidateName();

        try {
            // 设置用户上下文（如果需要）
            if (currentUser != null) {
                UserContextUtil.setCurrentUser(currentUser);
            }

            // 调用单个邀请处理方法（该方法已有@Transactional注解）
            InviteInterviewResponseVO response = inviteInterviewService.inviteInterview(request);

            return BatchInviteInterviewItemVO.builder()
                    .index(index)
                    .jobId(request.getJobId())
                    .candidateEmail(candidateEmail)
                    .candidateName(candidateName)
                    .applicationId(response.getApplicationId())
                    .candidateId(response.getCandidateId())
                    .status("SUCCESS")
                    .message(response.getMessage())
                    .build();

        } catch (BusinessException e) {
            log.warn("Batch invite interview item failed with business exception: jobId={}, email={}, error={}",
                    request.getJobId(), candidateEmail, e.getMessage());

            return BatchInviteInterviewItemVO.builder()
                    .index(index)
                    .jobId(request.getJobId())
                    .candidateEmail(candidateEmail)
                    .candidateName(candidateName)
                    .status("FAILURE")
                    .message(e.getMessage())
                    .errorCode(e.getCode())
                    .build();

        } catch (Exception e) {
            log.error("Batch invite interview item failed with unexpected error: jobId={}, email={}",
                    request.getJobId(), candidateEmail, e);

            String errorMessage = LanguageLocalUtils.getErrorMessage(
                    "Failed to invite interview: " + e.getMessage(),
                    "邀请面试失败: " + e.getMessage()
            );

            return BatchInviteInterviewItemVO.builder()
                    .index(index)
                    .jobId(request.getJobId())
                    .candidateEmail(candidateEmail)
                    .candidateName(candidateName)
                    .status("FAILURE")
                    .message(errorMessage)
                    .errorCode(GlobalStatusCode.FAIL)
                    .build();

        } finally {
            // 清理线程上下文
            UserContextUtil.clear();
        }
    }
}
