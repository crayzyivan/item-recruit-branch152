package com.item.service.impl;

import com.item.framework.net.HttpClient5Service;
import com.item.dto.naukri.NaukriApplicationDTO;
import com.item.dto.naukri.NaukriApplicationsResponseDTO;
import com.item.service.NaukriApplicationsService;
import com.item.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class NaukriApplicationsServiceImpl implements NaukriApplicationsService {
    private final HttpClient5Service httpClient5Service;

    @Value("${naukri.api.url:https://api.zwayam.com/amplify/v2}")
    private String apiUrl;

    @Value("${naukri.api.key}")
    private String apiKey;

    private Map<String, String> defaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("api_key", apiKey);
        return headers;
    }

    @Override
    public List<NaukriApplicationDTO> fetchApplicationsForJob(String naukriJobId, int page) {
        if (StringUtils.isBlank(naukriJobId)) {
            return Collections.emptyList();
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = LocalDate.now().atStartOfDay();// Make sure to fetch all applications since the beginning of the day
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fromDate = from.format(fmt);
        String toDate = now.format(fmt);
        String url = apiUrl + "/jobs/" + naukriJobId
                + "/applies?fromDate=" + URLEncoder.encode(fromDate, StandardCharsets.UTF_8)
                + "&toDate=" + URLEncoder.encode(toDate, StandardCharsets.UTF_8)
                + "&page=" + page;

        String resp = httpClient5Service.doGet(url, defaultHeaders());
        try {
            var response = JsonUtils.toObject(resp, NaukriApplicationsResponseDTO.class);
            if (response == null || response.getData().isEmpty()) {
                return Collections.emptyList();
            }
            return response.getData();
        } catch (Exception e) {
            log.warn("fetchApplicationsForJob parse error jobId={} page={} resp={}", naukriJobId, page, resp);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> getApplicationDetails(String applicationId) {
        if (StringUtils.isBlank(applicationId)) {
            return Collections.emptyMap();
        }
        String url = apiUrl + "/applies/" + applicationId;
        String resp = httpClient5Service.doGet(url, defaultHeaders());
        try {
            return JsonUtils.toObject(resp, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            log.warn("getApplicationDetails parse error appId={} resp={}", applicationId, resp);
            return Collections.emptyMap();
        }
    }

    @Override
    public String getApplicantResumeUrl(String applicationId) {
        if (StringUtils.isBlank(applicationId)) {
            return null;
        }

        try {
            // Fetch application details to obtain jobId and resume file id
            Map<String, Object> details = getApplicationDetails(applicationId);
            if (details == null || details.isEmpty()) {
                log.warn("getApplicantResumeUrl: empty details for appId={}", applicationId);
                return null;
            }

            String jobId = String.valueOf(details.get("jobId"));
            Object resumeFile = details.get("resumeFileId");
            String resumeFileId = resumeFile == null ? null : String.valueOf(resumeFile);

            if (StringUtils.isBlank(jobId) || StringUtils.isBlank(resumeFileId)) {
                log.warn("getApplicantResumeUrl: missing jobId/resumeFileId for appId={} details={}", applicationId, details);
                return null;
            }

            String url = apiUrl + "/jobs/" + jobId + 
                    "/applies/" + applicationId +
                    "/files/" + resumeFileId;

            String resp = httpClient5Service.doGet(url, defaultHeaders());
            Map<String, Object> map = JsonUtils.toObject(resp, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
            Object link = map == null ? null : map.get("url");
            return link == null ? null : String.valueOf(link);
        } catch (Exception e) {
            log.warn("getApplicantResumeUrl error appId={} ", applicationId, e);
            return null;
        }
    }

    @Override
    public boolean updateApplicationStage(String applicationId, String stage) {
        if (StringUtils.isAnyBlank(applicationId, stage)) {
            return false;
        }
        String url = apiUrl + "/applications/" + applicationId + "/stage";
        Map<String, Object> body = new HashMap<>();
        body.put("stage", stage);
        String resp = httpClient5Service.doPost(url, JsonUtils.toJson(body), defaultHeaders());
        log.info("updateApplicationStage appId={} stage={} resp={}", applicationId, stage, resp);
        return true;
    }
}


