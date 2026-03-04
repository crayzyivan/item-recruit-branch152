package com.item.controller;

import com.item.service.CandidateJobDomainService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 视频
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-11  13:54
 */
@Slf4j
@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    //private final InterviewResultSyncService interviewResultSyncService;
    private final CandidateJobDomainService candidateJobDomainService;

    /**
     * 视频跳转中转
     * @param applicationId
     * @param response
     */
    @GetMapping("/interview/{applicationId}")
    public void redirectToVideo(@PathVariable String applicationId, HttpServletResponse response){
        candidateJobDomainService.redirectToVideo(applicationId,response);
    }

}