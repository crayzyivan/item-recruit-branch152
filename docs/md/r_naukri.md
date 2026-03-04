# Naukri Integration (Jobs + Applications)

## Overview
Item Recruit integrates with Naukri via Zwayam Amplify for:
- Job lifecycle: publish, update, refresh, unpublish
- Applications ingest: periodic sync, candidate creation, application creation

Config:
- naukri.enabled
- naukri.api.url
- naukri.api.key

## Jobs Integration
- Service: com.item.service.impl.NaukriServiceImpl
- Publish: JobDomainServiceImpl.publishJob(...)
- Update: JobDomainServiceImpl.updateJob(...)
- Unpublish (on CLOSED): JobDomainServiceImpl.updateJobStatus(...)
- Refresh: POST /jobs/{jobId}/refresh with { "jobBoards": ["naukri"] }
- Headers: api_key, Content-Type: application/json
- Persistence: r_job.naukri_job_id (DB), JobEsEntity.naukriJobId (ES)

## Applications Integration
- Service: com.item.service.impl.NaukriApplicationsServiceImpl
- Scheduler: com.item.schedule.NaukriApplicationsSyncTask
- DTOs: NaukriApplicationsResponseDTO, NaukriApplicationDTO (@JsonProperty for PascalCase)

### Sync (every 2 hours)
- Cron: 0 0 */2 * * *
- Lock: Redisson key lock:naukri:apps-sync
- Steps:
  1) List local jobs with naukri_job_id
  2) GET jobs/{naukriJobId}/applies?fromDate=now-2h&toDate=now
  3) For each application:
     - Create candidate if missing (random strong password)
     - Create r_candidate_job if missing; set naukri_application_id
     - GET resume URL and store on candidate

### Status propagation
- On our status change, call NaukriApplicationsService.updateApplicationStage(applicationId, stage)
- Suggested hook: CandidateJobService.updateApplyStatus(...) when naukri_application_id present

## Methods

### com.item.service.impl.NaukriServiceImpl (Jobs)
- `boolean shouldPostToNaukri(JobCreateBO jobCreateBO)`
  - Checks feature toggle and location (India or Saudi Arabia) to decide posting.
- `void asyncPostJob(JobCreateBO jobCreateBO, String companyName, Long jobId)`
  - Creates a job on Naukri; parses response into NaukriJobCreateResponseDTO and persists naukri_job_id.
  - POST {apiUrl}/jobs, headers: api_key, Content-Type: application/json.
- `void asyncUpdateJob(JobUpdateBO jobUpdateBO)`
  - Updates an existing job on Naukri with edited fields.
  - POST {apiUrl}/jobs/update.
- `void asyncUnpublishJob(String jobId)`
  - Unpublishes a job from job boards on Naukri (jobBoards: ["naukri"]).
  - POST {apiUrl}/jobs/{jobId}/unpublish.
- `void asyncRefreshJob(String jobId)`
  - Refreshes bumping visibility for the job on Naukri (jobBoards: ["naukri"]).
  - POST {apiUrl}/jobs/{jobId}/refresh.

### JobDomainServiceImpl integration points
- `Long publishJob(JobCreateDTO dto)`
  - After persisting locally, conditionally calls `naukriService.asyncPostJob(...)` when eligible.
- `Boolean updateJob(JobUpdateDTO dto)`
  - After local update, calls `naukriService.asyncUpdateJob(...)`.
- `Boolean updateJobStatus(Long jobId, Integer jobStatus)`
  - If status becomes CLOSED, calls `naukriService.asyncUnpublishJob(naukriJobId)`.

### com.item.service.impl.NaukriApplicationsServiceImpl (Applications)
- `List<NaukriApplicationDTO> fetchApplicationsForJob(String naukriJobId)`
  - Fetches applications for a job within a time window (now-2h → now).
  - GET {apiUrl}/jobs/{jobId}/applies?fromDate=...&toDate=...&page=0.
- `Map<String, Object> getApplicationDetails(String applicationId)`
  - Fetches detailed info for one application.
  - GET {apiUrl}/applications/{applicationId}.
- `String getApplicantResumeUrl(String applicationId)`
  - Returns a temporary resume URL if available.
  - GET {apiUrl}/applications/{applicationId}/resume/url.
- `boolean updateApplicationStage(String applicationId, String stage)`
  - Updates stage/status of an application in Naukri.
  - POST {apiUrl}/applications/{applicationId}/stage with body { stage }.

### com.item.schedule.NaukriApplicationsSyncTask
- `void sync()` (scheduled; cron: 0 0 */2 * * *; Redisson-locked)
  - For each local job with naukri_job_id:
    - Calls `fetchApplicationsForJob` to pull recent applies.
    - Ensures candidate exists locally; creates if missing with strong random password.
    - Ensures r_candidate_job exists; sets naukri_application_id.
    - Retrieves resume URL and stores on candidate.

### DTOs
- `NaukriJobCreateResponseDTO`
  - `{ id: "..." }` from job creation response.
- `NaukriApplicationsResponseDTO`
  - `{ data: [...], totalCount, currentPage, pageSize }` wrapper.
- `NaukriApplicationDTO`
  - Application record including `applyData` (PascalCase mapped via @JsonProperty), `scores`, `createdDate`, etc.

## Notes
- Log URLs/responses for troubleshooting
- URL-encode date parameters
- Ensure DTOs reflect API casing via @JsonProperty
