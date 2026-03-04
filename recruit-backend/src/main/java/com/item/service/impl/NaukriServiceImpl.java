package com.item.service.impl;

import com.item.dto.job.JobCreateBO;
import com.item.dto.job.JobUpdateBO;
import com.item.framework.net.HttpClient5Service;
import com.item.service.JobService;
import com.item.service.NaukriService;
import com.item.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Naukri integration using Zwayam Amplify.
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class NaukriServiceImpl implements NaukriService {
    private final HttpClient5Service httpClient5Service; 
    private final JobService jobService;

    @Value("${naukri.enabled}")
    private boolean naukriEnabled;

    @Value("${naukri.api.url}")
    private String apiUrl;

    // Temporary API key placeholder per request; to be replaced later by ops
    @Value("${naukri.api.key}")
    private String apiKey;

    private static final Set<String> NAUKRI_SUPPORTED_COUNTRIES = Set.of(
            "India", "india", "IN", "in",
            "Saudi Arabia", "saudi arabia", "SA", "sa"
    );

    @Override
    public boolean shouldPostToNaukri(JobCreateBO jobCreateBO) {
        if (!naukriEnabled || jobCreateBO == null || CollectionUtils.isEmpty(jobCreateBO.getLocations())) {
            return false;
        }
        // If any location country matches India or Saudi Arabia
        return jobCreateBO.getLocations().stream().anyMatch(l -> {
            String countryName = StringUtils.defaultIfBlank(l.getCountryName(), "");
            String countryId = l.getCountryId() == null ? "" : String.valueOf(l.getCountryId());
            return NAUKRI_SUPPORTED_COUNTRIES.contains(countryName) || NAUKRI_SUPPORTED_COUNTRIES.contains(countryId);
        });
    }

    @Override
    @Async
    public void asyncPostJob(JobCreateBO jobCreateBO, String companyName, Long jobId) {
        try {
            if (!shouldPostToNaukri(jobCreateBO)) {
                log.info("Naukri post skipped: naukriEnabled={}, locations={}.", naukriEnabled, jobCreateBO.getLocations());
                return;
            }
            Map<String, Object> payload = new HashMap<>();

            // Required: title
            payload.put("title", StringUtils.defaultIfBlank(jobCreateBO.getTitle(), "Untitled Job"));

            // Required: jobType (must not be null)
            // Default to "hot"
            payload.put("jobType", "hot"); // default: hot

            // Required: description (include duties and requirements with line breaks per item)
            payload.put("description", buildDescription(jobCreateBO));

           

            // Required: salaryCurrency
            // Default to INR if other currency than INR or USD Naukri only supports INR and USD based on API documentation
            String currency = StringUtils.defaultIfBlank(jobCreateBO.getCurrencyName(), "INR");
            currency = ("USD".equalsIgnoreCase(currency) || "INR".equalsIgnoreCase(currency)) ? currency.toUpperCase() : "INR";
            payload.put("salaryCurrency", currency); // default/coerced: INR or USD only

            // If currency is USD, then required: minSalary and maxSalary. US minimum salary is 5000
            // If currency is INR, then required: minSalary and maxSalary. INR minimum salary is 50000
            if(currency.equalsIgnoreCase("USD"))
            {
                // Required: minSalary (>= 5000)
                int minSalary = jobCreateBO.getMinSalary() != null && jobCreateBO.getMinSalary() >= 5000 ? jobCreateBO.getMinSalary() : 5000;
                payload.put("minSalary", minSalary);
                // Required: maxSalary (>= 5000)
                int maxSalary = jobCreateBO.getMaxSalary() != null && jobCreateBO.getMaxSalary() >= 5000 ? jobCreateBO.getMaxSalary() : 5000;
                payload.put("maxSalary", maxSalary);                
            }else{
                int minSalary = jobCreateBO.getMinSalary() != null && jobCreateBO.getMinSalary() >= 50000 ? jobCreateBO.getMinSalary() : 50000;
                payload.put("minSalary", minSalary);
                // Required: maxSalary (>= 5000)
                int maxSalary = jobCreateBO.getMaxSalary() != null && jobCreateBO.getMaxSalary() >= 50000 ? jobCreateBO.getMaxSalary() : 50000;
                payload.put("maxSalary", maxSalary);  
            }

            // Required: industry (must not be null)
            // Default to IT Services & Consulting
            // Naukri has its own industry list, so we need to map the industry to Naukri industry
            //Default to IT Services & Consulting instead
            payload.put("industry", "IT Services & Consulting"); // default: Other

            // Optional: workMode
            // Optional: workMode mapping (convert app values to Naukri values)
            String workMode = StringUtils.defaultIfBlank(jobCreateBO.getModeName(), "");
            switch (workMode) {
                case "On-Site":
                    workMode = "In office"; // Map "On-site" to "Onsite" for Naukri
                    break;
                case "Remote":
                    workMode = "Remote";
                    break;
                case "Hybrid":
                    workMode = "Hybrid";
                    break;
                default:
                    // For "Other" or any unknown value, set as empty or handle as needed
                    workMode = "";
                    break;
            }
            payload.put("workMode", workMode);

            // Required: employmentType (map app values to Naukri values)
            String employmentType = StringUtils.defaultIfBlank(jobCreateBO.getTypeName(), "");
            switch (employmentType) {
                case "Full Time":
                    employmentType = "Full Time, Permanent";
                    break;
                case "Contract":
                    employmentType = "Full Time, Temporary/Contractual";
                    break;
                case "Part Time":
                    employmentType = "Part Time, Permanent";
                    break;
                case "Temporary":
                    employmentType = "Part Time, Temporary/Contractual";
                    break;
                case "Other":
                    employmentType = "Full Time, Freelance/Homebased";
                    break;
                default:
                    employmentType = "Full Time, Permanent"; // default: Full Time, Permanent
                    break;
            }
            payload.put("employmentType", employmentType);

            // Required: orgName
            payload.put("orgName", StringUtils.defaultIfBlank(companyName, "Unis Company Inc."));

            // Optional: website (not in JobCreateBO, so ignore or set default)
            // payload.put("website", "https://example.com"); // default if needed

            // Optional: minWorkExperience, maxWorkExperience (not in JobCreateBO, so ignore or set default)
            // payload.put("minWorkExperience", 0);
            // payload.put("maxWorkExperience", 0);
           
            // Required: keySkills
            // Naukri only supports 250 characters for all the key skills combined.
            // So we need to limit the key skills to 250 characters.            
            payload.put("keySkills", limitKeySkillsLength(jobCreateBO.getSkills()));

            // Required: locations
            // Naukri only supports India and Saudi Arabia.
            // So we need to filter the locations to only include India and Saudi Arabia.
            // If the country is not India, then we need to remove the state and city. Naukri only supports India.
            if (CollectionUtils.isNotEmpty(jobCreateBO.getLocations())) {
                payload.put("locations", jobCreateBO.getLocations().stream().map(l -> {
                    Map<String, String> loc = new HashMap<>();
                    if (StringUtils.isNotBlank(l.getCityName())) loc.put("city", l.getCityName());
                    if (StringUtils.isNotBlank(l.getStateName())) loc.put("state", l.getStateName());
                    if (StringUtils.isNotBlank(l.getCountryName())) loc.put("country", l.getCountryName());
                    return scrubStateCityIfNotIndia(loc);
                }).toList());
            } else {
                // Default: at least one location required
                payload.put("locations", List.of(Map.of("country", "India")));
            }

            // Optional: educationQualifications, questions (not in JobCreateBO, so ignore)

            // Required: distributeTo (must not be null)
            payload.put("distributeTo", List.of("naukri")); // default: ["naukri"]

            // Optional: showSalary (not in JobCreateBO, so default to true)
            payload.put("showSalary", true);

            // Optional: notifyEmail (not in JobCreateBO, so ignore or set default)
            // payload.put("notifyEmail", "hr@example.com");

            // Required: referenceCode (must not be null)
            payload.put("referenceCode", String.valueOf(jobId != null ? jobId : System.currentTimeMillis())); // default: jobId or timestamp
                   
            Map<String, String> headers = defaultHeaders();
            log.info("Create Job payload: {}", JsonUtils.toJson(jobCreateBO));
            log.info("Naukri post payload: {}", JsonUtils.toJson(payload));
            String response = httpClient5Service.doPost(apiUrl + "/jobs", JsonUtils.toJson(payload), headers);
             log.info("Naukri post response jobId={} resp={}", jobId, response);

            // Try to extract naukri job id from response
             String naukriJobId = null;
             try {
                if (StringUtils.isNotBlank(response)) {
                    var dto = JsonUtils.toObject(response, com.item.dto.naukri.NaukriJobCreateResponseDTO.class);
                    naukriJobId = dto == null ? null : dto.getId();
                 }
             } catch (Exception ignore) {
                log.warn("Failed parsing Naukri response jobId={} resp={}", jobId, response);
             }

             if (jobId != null && StringUtils.isNotBlank(naukriJobId)) {
                try {
                   
                     // persist to DB as well
                        jobService.updateNaukriJobId(jobId, naukriJobId);
                 } catch (Exception e) {
                     log.warn("Failed updating ES with naukriJobId jobId={} naukriJobId={}", jobId, naukriJobId, e);
                 }
             }
             log.info("Naukri post invoked for jobId={} company={} title={}", jobId, payload.get("orgName"), jobCreateBO.getTitle());
        } catch (Exception e) {
            log.warn("Naukri post failed jobId={} error=", jobId, e);
        }
    }

    @Override
    @Async
    public void asyncUpdateJob(JobUpdateBO jobUpdateBO) {
        try {
            if (jobUpdateBO == null || jobUpdateBO.getJobId() == null) {
                return;
            }
            Map<String, Object> payload = new HashMap<>();               
            payload.put("description", jobUpdateBO.getJobDetail());
            payload.put("locations", jobUpdateBO.getLocations());
            payload.put("employmentType", jobUpdateBO.getTypeName());
            payload.put("mode", jobUpdateBO.getModeName());
            payload.put("salaryMin", jobUpdateBO.getMinSalary());
            payload.put("salaryMax", jobUpdateBO.getMaxSalary());
            payload.put("currency", jobUpdateBO.getCurrencyName());

            Map<String, String> headers = defaultHeaders();
            httpClient5Service.doPostAsync(apiUrl + "/jobs/update", JsonUtils.toJson(payload), headers);
            log.info("Naukri update invoked for jobId={}", jobUpdateBO.getJobId());
        } catch (Exception e) {
            log.warn("Naukri update failed jobId={} error=", jobUpdateBO == null ? null : jobUpdateBO.getJobId(), e);
        }
    }

    @Override
    @Async
    public void asyncUnpublishJob(String jobId) {
        try {
            if (jobId == null) {
                return;
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("jobBoards", List.of("naukri"));
            Map<String, String> headers = defaultHeaders();
            String url = apiUrl + "/jobs/" + jobId + "/unpublish";
            httpClient5Service.doPostAsync(url, JsonUtils.toJson(payload), headers);
            log.info("Naukri UnpublishJob invoked for NaukrijobId={} payload={}", jobId, payload);
        } catch (Exception e) {
            log.warn("Naukri UnpublishJob failed jobId={} error=", jobId, e);
        }
    }

    private Map<String, String> defaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("api_key",apiKey);
        return headers;
    }

    /**
     * Remove state and city if the country is not India.
     */
    private Map<String, String> scrubStateCityIfNotIndia(Map<String, String> location) {
        String country = location.get("country");
        if (StringUtils.isBlank(country)) {
            return location;
        }
        String normalized = country.trim().toLowerCase();
        if (!("india".equals(normalized) || "in".equals(normalized))) {
            location.remove("state");
            location.remove("city");
        }
        return location;
    }

    
    /**
     * Combine all skills separated by new lines, then cap the total length to 250 characters.
     * Returns a single-element list containing the combined string (or empty list if no content).
     */
    private List<String> limitKeySkillsLength(List<String> skills) {
        if (skills == null) {
            return null;
        }
        if (skills.isEmpty()) {
            return List.of();
        }
        String combined = String.join("\n", skills.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .toList());
        if (combined.length() > 250) {
            combined = combined.substring(0, 250);
        }
        if (StringUtils.isBlank(combined)) {
            return List.of();
        }
        return List.of(combined);
    }

    /**
     * Build description with main duties and requirements appended, each item on a new line.
     */
    private String buildDescription(JobCreateBO jobCreateBO) {
        String base = StringUtils.defaultIfBlank(jobCreateBO.getJobDetail(), "No description provided");
        StringBuilder sb = new StringBuilder(base);

        appendSection(sb, "\n\nMain Duties:", jobCreateBO.getMainDuty());
        appendSection(sb, "\n\nPreferred Requirements:", jobCreateBO.getPreferredJobRequirement());
        appendSection(sb, "\n\nMinimum Requirements:", jobCreateBO.getMinimumJobRequirement());

        return sb.toString();
    }

    private void appendSection(StringBuilder sb, String header, List<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append(header);
        for (String item : items) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            sb.append("\n- ").append(item.trim());
        }
    }

    @Override
    @Async
    public void asyncRefreshJob(String jobId) {
        try {
            if (jobId == null) {
                return;
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("jobBoards", List.of("naukri"));
            Map<String, String> headers = defaultHeaders();
            String url = apiUrl + "/jobs/" + jobId + "/refresh";
            httpClient5Service.doPostAsync(url, JsonUtils.toJson(payload), headers);
            log.info("Naukri refresh invoked for jobId={} payload={}", jobId, payload);
        } catch (Exception e) {
            log.warn("Naukri refresh failed jobId={} error=", jobId, e);
        }
    }
}


