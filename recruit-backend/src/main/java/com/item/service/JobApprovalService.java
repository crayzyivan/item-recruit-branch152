package com.item.service;

import com.item.vo.JobApprovalRequestVO;
import com.item.vo.JobApprovalSettingsVO;
import com.item.vo.JobAuditHistoryVO;
import com.item.vo.PendingJobListVO;
import com.item.framework.http.Pager;
import java.util.List;

public interface JobApprovalService {
    Boolean submitJobForApproval(Long jobId);
    
    Boolean processJobApproval(JobApprovalRequestVO request);
    
    JobApprovalSettingsVO getApprovalSettings(String companyCode);
    
    Boolean createApprovalSettings(JobApprovalSettingsVO settings);
    
    Boolean updateApprovalSettings(JobApprovalSettingsVO settings);
    
    Pager<PendingJobListVO> getPendingApprovalJobs(String companyCode, int pageNo, int pageSize);
    
    Pager<PendingJobListVO> getAwaitingJobs(String companyCode, int pageNo, int pageSize);
    
    Boolean publishJob(Long jobId);
    
    List<JobAuditHistoryVO> getJobAuditHistory(Long jobId);
    
    void sendApprovalNotification(Long jobId, String action, String comment, String email);
}
