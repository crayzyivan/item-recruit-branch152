package com.item.controller;

import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.service.JobApprovalService;
import com.item.vo.JobApprovalRequestVO;
import com.item.vo.JobApprovalSettingsVO;
import com.item.vo.JobAuditHistoryVO;
import com.item.vo.PendingJobListVO;
import com.item.framework.http.Pager;
import com.item.util.UserContextUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job/approval")
@RequiredArgsConstructor
public class JobApprovalController {
    private final JobApprovalService jobApprovalService;
    
    @PostMapping("/submit/{jobId}")
    @Auth(roleType = RoleType.SUB_USER)
    public Boolean submitJobForApproval(@PathVariable Long jobId) {
        return jobApprovalService.submitJobForApproval(jobId);
    }
    
    @PostMapping("/process")
    @Auth(roleType = RoleType.MASTER_USER)
    public Boolean processJobApproval(@RequestBody @Validated JobApprovalRequestVO request) {
        return jobApprovalService.processJobApproval(request);
    }
    
    @GetMapping("/settings")
    @Auth(roleType = RoleType.SUB_USER)
    public JobApprovalSettingsVO getApprovalSettings() {
        return jobApprovalService.getApprovalSettings(UserContextUtil.getCurrentUserCompanyCode());
    }
    
    @PostMapping("/settings")
    @Auth(roleType = RoleType.MASTER_USER)
    public Boolean createApprovalSettings(@RequestBody @Validated JobApprovalSettingsVO settings) {
        String companyCode = UserContextUtil.getCurrentUserCompanyCode();
        settings.setCompanyCode(companyCode);
        return jobApprovalService.createApprovalSettings(settings);
    }
    
    @PutMapping("/settings")
    @Auth(roleType = RoleType.MASTER_USER)
    public Boolean updateApprovalSettings(@RequestBody @Validated JobApprovalSettingsVO settings) {
        String companyCode = UserContextUtil.getCurrentUserCompanyCode();
        settings.setCompanyCode(companyCode);
        return jobApprovalService.updateApprovalSettings(settings);
    }
    
    @GetMapping("/pending")
    @Auth(roleType = RoleType.MASTER_USER)
    public Pager<PendingJobListVO> getPendingApprovalJobs(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return jobApprovalService.getPendingApprovalJobs(
            UserContextUtil.getCurrentUserCompanyCode(), pageNo, pageSize);
    }
    
    @GetMapping("/awaiting")
    @Auth(roleType = RoleType.SUB_USER)
    public Pager<PendingJobListVO> getAwaitingJobs(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return jobApprovalService.getAwaitingJobs(
            UserContextUtil.getCurrentUserCompanyCode(), pageNo, pageSize);
    }
    
    @PostMapping("/publish/{jobId}")
    @Auth(roleType = RoleType.MASTER_USER)
    public Boolean publishJob(@PathVariable Long jobId) {
        return jobApprovalService.publishJob(jobId);
    }
    
    @GetMapping("/history/{jobId}")
    @Auth(roleType = RoleType.SUB_USER)
    public List<JobAuditHistoryVO> getJobAuditHistory(@PathVariable Long jobId) {
        return jobApprovalService.getJobAuditHistory(jobId);
    }
}
