package com.item.schedule;

import com.item.convert.CandidateConverter;
import com.item.convert.CandidateJobConvert;
import com.item.dto.CandidateEducationDTO;
import com.item.dto.EmploymentHistoryDTO;
import com.item.dto.CandidateDTO;
import com.item.dto.naukri.NaukriApplicationDTO;
import com.item.dto.naukri.NaukriApplicationsResponseDTO;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.dto.job.LocationValRecordDTO;
import com.item.entity.CandidateEsEntity;
import com.item.es.ResumeEsService;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobStatus;
import com.item.service.CandidateJobService;
import com.item.service.CandidateService;
import com.item.service.JobService;
import com.item.service.NaukriApplicationsService;
import com.item.task.core.handler.annotation.ScheduleTask;
import org.springframework.scheduling.annotation.Scheduled;
import com.item.util.JsonUtils;
import com.item.util.FileParseUtils;
import com.item.util.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

 

@Slf4j
@Component
@RequiredArgsConstructor
public class NaukriApplicationsSyncTask {

    private final JobService jobService;
    private final CandidateService candidateService;
    private final CandidateJobService candidateJobService;
    private final NaukriApplicationsService naukriApplicationsService;
    private final RedissonClient redissonClient;
    private final JobEsService jobEsService;
    private final ResumeEsService resumeEsService;
    private final S3Utils s3Utils;

   @ScheduleTask("naukriApplicationsSyncTask")   
    public void sync() {
        RLock lock = redissonClient.getLock("lock:naukri:apps-sync");
        boolean locked = false;
        try {
        
            log.info("NaukriApplicationsSyncTask: start");

            locked = lock.tryLock(0, 40, TimeUnit.SECONDS);
            if (!locked) {
                log.info("NaukriApplicationsSyncTask: another instance is running, skip this cycle");
                return;
            }
            // 1) Get jobs that have posted to Naukri (naukriJobId not null)
            log.info("Naukri sync: fetching jobs");
            List<JobEntity> jobs = jobService.lambdaQuery()
                    .isNotNull(JobEntity::getNaukriJobId)
                    .ne(JobEntity::getNaukriJobId, "")
                    .eq(JobEntity::getJobStatus, JobStatus.ACTIVE.getCode())                  
                    .select(JobEntity::getId, JobEntity::getNaukriJobId, JobEntity::getCompanyCode, JobEntity::getCustomerId)
                    .list();

            if (CollectionUtils.isEmpty(jobs)) {
                log.info("Naukri sync: no jobs with naukriJobId");
                return;
            }

           
            Map<String, Long> naukriToJobId = jobs.stream().collect(Collectors.toMap(JobEntity::getNaukriJobId, JobEntity::getId));

            for (JobEntity job : jobs) {
                String naukriJobId = job.getNaukriJobId();

                
                int page = 0;
                while (true) {
                    List<NaukriApplicationDTO> apps = naukriApplicationsService.fetchApplicationsForJob(naukriJobId, page);
                    if (CollectionUtils.isEmpty(apps)) {
                        break;
                    }
                   
                    for (NaukriApplicationDTO naukriApplicationDTO : apps) {
                  
                        if (naukriApplicationDTO == null || StringUtils.isBlank(naukriApplicationDTO.getId())) {
                            continue;
                        }
                    // 2) Ensure candidate exists
                    String email = naukriApplicationDTO.getApplyData().getEmail();
                 
                    CandidateEntity candidate = candidateService.getByEmail(email);
                    
                    if (candidate == null && StringUtils.isNotBlank(email)) {
                        CandidateDTO create = new CandidateDTO();
                        create.setCandidateEmail(email);
                        create.setFirstName(naukriApplicationDTO.getApplyData().getFirstName());
                        create.setLastName(naukriApplicationDTO.getApplyData().getLastName());
                        create.setCandidateName((StringUtils.defaultString(naukriApplicationDTO.getApplyData().getFirstName(), "") + " " + StringUtils.defaultString(naukriApplicationDTO.getApplyData().getLastName(), "")).trim());
                        create.setPhoneNumber(naukriApplicationDTO.getApplyData().getMobile());
                        // a strong password can be set or a random token-like string
                        create.setPassword(java.util.UUID.randomUUID().toString());
                        create.setGender(naukriApplicationDTO.getApplyData().getGender());
                        // populate location from job for newly created candidate
                        applyJobLocationToCandidate(create, job.getId());
                        candidateService.register(create);
                        candidate = candidateService.getByEmail(email);                      
                    }

                    if (candidate == null) {
                        log.warn("Naukri sync: candidate missing and cannot be created appId={} email={}", naukriApplicationDTO.getId(), email);
                        continue;
                    }

                    // 3) Create application if missing
                    Long localJobId = naukriToJobId.get(naukriJobId);
                    List<CandidateJobEntity> existing = candidateJobService.getByCandidateIdAndJobId(candidate.getId(), localJobId);                   
                    if (CollectionUtils.isEmpty(existing)) {
                        CandidateJobEntity cje = new CandidateJobEntity();
                        cje.setCandidateId(candidate.getId());
                        cje.setJobId(localJobId);
                        cje.setCustomerId(job.getCustomerId());
                        cje.setCompanyCode(job.getCompanyCode());
                        // set job location fields from ES for downstream AI location fallback
                        try {
                            JobEsEntity jobEs = jobEsService.getJobById(localJobId);
                            if (jobEs != null && jobEs.getLocations() != null && !jobEs.getLocations().isEmpty()) {
                                LocationValRecordDTO loc = jobEs.getLocations().getFirst();
                                cje.setJobCountryId(loc.getCountryId());
                                cje.setJobStateId(loc.getStateId());
                                cje.setJobCityId(loc.getCityId());
                                cje.setJobCountryName(loc.getCountryName());
                                cje.setJobStateName(loc.getStateName());
                                cje.setJobCityName(loc.getCityName());
                            }
                        } catch (Exception ignore) {}
                        cje.setPosted(1);
                        cje.setApplyStatus(JobApplyStatus.SUBMITTED.getCode());
                        cje.setNaukriApplicationId(naukriApplicationDTO.getId());
                        candidateJobService.applyJob(cje);
                    } else {
                        CandidateJobEntity latest = existing.getFirst();
                        if (StringUtils.isBlank(latest.getNaukriApplicationId())) {
                            latest.setNaukriApplicationId(naukriApplicationDTO.getId());                           
                            candidateJobService.updateById(latest);
                        }
                        // backfill job location if missing
                        if (latest.getJobCityId() == null && latest.getJobCountryId() == null && latest.getJobStateId() == null) {
                            try {
                                JobEsEntity jobEs = jobEsService.getJobById(localJobId);
                                if (jobEs != null && jobEs.getLocations() != null && !jobEs.getLocations().isEmpty()) {
                                    LocationValRecordDTO loc = jobEs.getLocations().getFirst();
                                    latest.setJobCountryId(loc.getCountryId());
                                    latest.setJobStateId(loc.getStateId());
                                    latest.setJobCityId(loc.getCityId());
                                    latest.setJobCountryName(loc.getCountryName());
                                    latest.setJobStateName(loc.getStateName());
                                    latest.setJobCityName(loc.getCityName());
                                    candidateJobService.updateById(latest);
                                }
                            } catch (Exception ignore) {}
                        }
                    }

                    // 4) Fetch and attach resume URL if present
                    try {
                        String externalUrl = naukriApplicationsService.getApplicantResumeUrl(naukriApplicationDTO.getId());
                        if (StringUtils.isNotBlank(externalUrl)) {
                            // store on candidate for now; later improve to attach to candidate-job if needed
                            CandidateDTO update = CandidateConverter.INSTANCE.convertEntityToDto(candidate);
                            if (update.getEducationList() == null) {
                                update.setEducationList(new java.util.ArrayList<>());
                            }
                            if (update.getEmploymentList() == null) {
                                update.setEmploymentList(new java.util.ArrayList<>());
                            }
                            // map education (ug, pg, phd) with null checks
                            try {
                                var edu = naukriApplicationDTO.getApplyData().getEducation();
                                java.util.function.Consumer<com.item.dto.naukri.NaukriApplicationDTO.Ug> addEdu = ug -> {
                                    if (ug == null) return;
                                    CandidateEducationDTO ced = new CandidateEducationDTO();
                                    if (org.apache.commons.lang3.StringUtils.isNotBlank(ug.getUniversity())) {
                                        ced.setInstitutionName(ug.getUniversity());
                                    }
                                    if (org.apache.commons.lang3.StringUtils.isNotBlank(ug.getSpecialization())) {
                                        ced.setMajor(ug.getSpecialization());
                                    }
                                    ced.setGraduated(org.apache.commons.lang3.StringUtils.isNotBlank(ug.getYearOfPassing()) ? 1 : 0);
                                    update.getEducationList().add(ced);
                                };
                                if (edu != null) {
                                    addEdu.accept(edu.getUg());
                                    addEdu.accept(edu.getPg());
                                    addEdu.accept(edu.getPhd());
                                }
                            } catch (Exception ignore) {}

                            // map work experiences (current, previous, other)
                            try {
                                var we = naukriApplicationDTO.getApplyData().getWorkExperiences();
                                if (we != null) {
                                    java.util.function.Consumer<NaukriApplicationDTO.CompanyInfo> addEmp = ci -> {
                                        if (ci == null) return;
                                        EmploymentHistoryDTO eh = new EmploymentHistoryDTO();
                                        eh.setCompanyName(ci.getOrganizationName());
                                        eh.setJobTitle(ci.getDesignation());
                                        eh.setKeyResponsibilities(ci.getJobProfile());
                                        try { if (org.apache.commons.lang3.StringUtils.isNotBlank(ci.getStartDate())) eh.setStartDate(java.time.LocalDate.parse(ci.getStartDate())); } catch (Exception ignored) {}
                                        try { if (org.apache.commons.lang3.StringUtils.isNotBlank(ci.getEndDate())) eh.setEndDate(java.time.LocalDate.parse(ci.getEndDate())); } catch (Exception ignored) {}
                                        update.getEmploymentList().add(eh);
                                    };
                                    addEmp.accept(we.getCurrentCompany());
                                    addEmp.accept(we.getPreviousCompany());
                                    if (we.getOtherCompany() != null) {
                                        for (NaukriApplicationDTO.CompanyInfo oc : we.getOtherCompany()) {
                                            addEmp.accept(oc);
                                        }
                                    }
                                }
                            } catch (Exception ignore) {}
                            update.setId(candidate.getId());

                            // 1) download resume and upload to S3
                            String s3Key = null;
                            byte[] resumeBytes = null;
                            try (java.io.InputStream in = new java.net.URL(externalUrl).openStream()) {
                                resumeBytes = in.readAllBytes();
                                String fileName = "naukri_resume_" + naukriApplicationDTO.getId() + ".pdf";
                                s3Key = s3Utils.uploadFromByteArray(resumeBytes, fileName, "application/pdf");
                            } catch (Exception ioEx) {
                                log.warn("Naukri sync: resume download/upload failed appId={} url={}",
                                        naukriApplicationDTO.getId(), externalUrl, ioEx);
                            }

                            // prefer S3 key if available, otherwise fall back to external
                            String resumeUrlToPersist = StringUtils.isNotBlank(s3Key) ? s3Key : externalUrl;
                            update.setResumeUrl(resumeUrlToPersist);
                            // ensure candidate has location populated from job
                            applyJobLocationToCandidate(update, job.getId());
                            candidateService.updateCandidate(update);

                            // mark uploaded flag if we have a resume URL
                            try {
                                CandidateEntity flagUpdate = new CandidateEntity();
                                flagUpdate.setId(candidate.getId());
                                flagUpdate.setUploadStatus(1);
                                candidateService.updateById(flagUpdate);
                            } catch (Exception ignore) {}

                            // extract and upsert resume content into ES so AI has data
                            try {
                                String resumeText = null;
                                if (resumeBytes != null) {
                                    resumeText = FileParseUtils.parsePdf(new java.io.ByteArrayInputStream(resumeBytes));
                                } else {
                                    resumeText = fetchResumeText(resumeUrlToPersist);
                                }
                                if (org.apache.commons.lang3.StringUtils.isNotBlank(resumeText)) {
                                    CandidateEsEntity esUpdate = CandidateEsEntity.builder()
                                            .id(candidate.getId())
                                            .resumeUrl(resumeUrlToPersist)
                                            .resumeContent(resumeText)
                                            .build();
                                    resumeEsService.updateCandidate(esUpdate);
                                } else {
                                    ensureCandidateEsDoc(candidate, resumeUrlToPersist, null);
                                }
                            } catch (Exception ex) {
                                log.warn("Naukri sync: resume text extraction failed appId={} url={}",
                                        naukriApplicationDTO.getId(), resumeUrlToPersist, ex);
                                // still ensure doc exists in ES (without content)
                                ensureCandidateEsDoc(candidate, resumeUrlToPersist, null);
                            }

                            // update JobMatchResultVO basic candidate fields in ES
                            try {
                                CandidateEntity refreshed = candidateService.getByEmail(email);
                                com.item.vo.ai.JobMatchResultVO jobMatchResultVO = new com.item.vo.ai.JobMatchResultVO();
                                CandidateJobConvert.INSTANCE.candidateEntityToJobMatchResultVO(refreshed, jobMatchResultVO);
                                resumeEsService.batchUpdateByCandidateId(jobMatchResultVO);
                            } catch (Exception ex) {
                                log.warn("Naukri sync: batchUpdateByCandidateId failed candidateId={}", candidate.getId(), ex);
                            }

                            // AI screening is handled by the scheduler; no immediate trigger here
                        } else {
                            // No resume URL returned; ensure ES has a minimal doc and update match VO
                            ensureCandidateEsDoc(candidate, null, null);
                            try {
                                CandidateEntity refreshed = candidateService.getByEmail(email);
                                com.item.vo.ai.JobMatchResultVO jobMatchResultVO = new com.item.vo.ai.JobMatchResultVO();
                                CandidateJobConvert.INSTANCE.candidateEntityToJobMatchResultVO(refreshed, jobMatchResultVO);
                                resumeEsService.batchUpdateByCandidateId(jobMatchResultVO);
                            } catch (Exception ex) {
                                log.warn("Naukri sync: batchUpdateByCandidateId (no-url) failed candidateId={}", candidate.getId(), ex);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Naukri sync: resume url fetch failed appId={}", naukriApplicationDTO.getId(), e);
                    }

                    // 5) Outbound status sync is handled by domain events (not here)
                    }
                    page++;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("NaukriApplicationsSyncTask interrupted", e);
        } catch (Exception e) {
            log.warn("NaukriApplicationsSyncTask error", e);
        } finally {
            if (locked) {
                try { lock.unlock(); } catch (Exception ignore) {}
            }
        }

    }

    /**
     * Apply job location (from ES) to candidate dto if candidate has no location set
     */
    private void applyJobLocationToCandidate(CandidateDTO candidateDTO, Long jobId)
    {
        if (candidateDTO == null || jobId == null) {
            return;
        }
        boolean hasAnyLocation = candidateDTO.getCountryId() != null
                || candidateDTO.getStateId() != null
                || candidateDTO.getCityId() != null
                || org.apache.commons.lang3.StringUtils.isNotBlank(candidateDTO.getCountryName())
                || org.apache.commons.lang3.StringUtils.isNotBlank(candidateDTO.getStateName())
                || org.apache.commons.lang3.StringUtils.isNotBlank(candidateDTO.getCityName());
        if (hasAnyLocation) {
            return;
        }

        try {
            JobEsEntity jobEs = jobEsService.getJobById(jobId);
            if (jobEs == null || jobEs.getLocations() == null || jobEs.getLocations().isEmpty()) {
                return;
            }
            LocationValRecordDTO loc = jobEs.getLocations().getFirst();
            candidateDTO.setCountryId(loc.getCountryId());
            candidateDTO.setStateId(loc.getStateId());
            candidateDTO.setCityId(loc.getCityId());
            candidateDTO.setCountryName(loc.getCountryName());
            candidateDTO.setStateName(loc.getStateName());
            candidateDTO.setCityName(loc.getCityName());
        } catch (Exception e) {
            log.warn("applyJobLocationToCandidate failed jobId={}", jobId, e);
        }
    }

    /**
     * Ensure candidate ES doc exists; create with minimal fields if missing
     */
    private void ensureCandidateEsDoc(CandidateEntity candidate, String resumeUrl, String resumeText)
    {
        try {
            if (candidate == null) {
                return;
            }
            com.item.entity.CandidateEsEntity existing = resumeEsService.searchResumeToEs(candidate.getId());
            if (existing == null) {
                CandidateEsEntity create = CandidateEsEntity.builder()
                        .id(candidate.getId())
                        .candidateName(candidate.getCandidateName())
                        .candidateEmail(candidate.getCandidateEmail())
                        .phoneNumber(candidate.getPhoneNumber())
                        .countryId(candidate.getCountryId())
                        .stateId(candidate.getStateId())
                        .cityId(candidate.getCityId())
                        .countryName(candidate.getCountryName())
                        .stateName(candidate.getStateName())
                        .cityName(candidate.getCityName())
                        .resumeUrl(resumeUrl)
                        .resumeContent(resumeText)
                        .build();
                resumeEsService.saveResumeToEs(create);
            }
        } catch (Exception e) {
            log.warn("ensureCandidateEsDoc failed candidateId={}", candidate != null ? candidate.getId() : null, e);
        }
    }

    /**
     * Download resume from remote URL and parse text (PDF expected)
     */
    private String fetchResumeText(String fileUrl)
    {
        try (java.io.InputStream in = new java.net.URL(fileUrl).openStream()) {
            return FileParseUtils.parsePdf(in);
        } catch (Exception e) {
            log.warn("fetchResumeText failed url={}", fileUrl, e);
            return null;
        }
    }
}
