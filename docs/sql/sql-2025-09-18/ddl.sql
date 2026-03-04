-- 1) r_job: add naukri_job_id and index
ALTER TABLE r_job
    ADD COLUMN `naukri_job_id` VARCHAR(128) DEFAULT NULL COMMENT 'Naukri Job ID';

CREATE INDEX `idx_naukri_job_id` ON `r_job` (`naukri_job_id`);

-- 2) r_candidate_job: add naukri_application_id and index
ALTER TABLE r_candidate_job
    ADD COLUMN `naukri_application_id` VARCHAR(128) DEFAULT NULL COMMENT 'Naukri application id';

CREATE INDEX `idx_naukri_application_id` ON `r_candidate_job` (`naukri_application_id`);


-- 3). 添加邀请重新申请字段
ALTER TABLE r_candidate_job
    ADD COLUMN reapply_invited TINYINT NOT NULL DEFAULT 0
    COMMENT '是否被邀请重新申请(0:未邀请,1:已邀请)';

CREATE INDEX idx_candidate_job_reapply_invited
    ON r_candidate_job (candidate_id, job_id, reapply_invited);
