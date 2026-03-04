-- job-approval-001: Add columns to r_job
ALTER TABLE r_job
    ADD COLUMN submitted_for_approval_at TIMESTAMP NULL,
    ADD COLUMN approved_at TIMESTAMP NULL,
    ADD COLUMN approved_by BIGINT NULL,
    ADD COLUMN denied_at TIMESTAMP NULL,
    ADD COLUMN denied_by BIGINT NULL;

-- job-approval-002: Create r_job_approval_settings
CREATE TABLE r_job_approval_settings (
                                         id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                         company_code VARCHAR(64) NOT NULL,
                                         approval_required BOOLEAN NOT NULL DEFAULT FALSE,
                                         email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
                                         admin_email VARCHAR(255) NULL,
                                         deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                         create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         create_by_id BIGINT UNSIGNED NULL,
                                         update_by_id BIGINT UNSIGNED NULL,
                                         PRIMARY KEY (id),
                                         UNIQUE KEY uk_job_approval_settings_company_code (company_code)
);

CREATE INDEX idx_job_approval_settings_company_code
    ON r_job_approval_settings (company_code);

-- job-approval-003: Create r_job_approval_email_retry
CREATE TABLE r_job_approval_email_retry (
                                            id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                            job_id BIGINT UNSIGNED NOT NULL,
                                            action VARCHAR(50) NOT NULL,
                                            comment TEXT NULL,
                                            template_name VARCHAR(100) NOT NULL,
                                            variables TEXT NULL,
                                            admin_email VARCHAR(255) NOT NULL,
                                            retry_count INT NOT NULL DEFAULT 0,
                                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                            first_failure_time TIMESTAMP NOT NULL,
                                            last_retry_time TIMESTAMP NULL,
                                            completed_time TIMESTAMP NULL,
                                            deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                            create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            create_by_id BIGINT UNSIGNED NULL,
                                            update_by_id BIGINT UNSIGNED NULL,
                                            PRIMARY KEY (id)
);

CREATE INDEX idx_job_approval_email_retry_status
    ON r_job_approval_email_retry (status);

CREATE INDEX idx_job_approval_email_retry_job_id
    ON r_job_approval_email_retry (job_id);

CREATE INDEX idx_job_approval_email_retry_last_retry_time
    ON r_job_approval_email_retry (last_retry_time);

-- job-approval-004: Create r_job_audit_history
CREATE TABLE r_job_audit_history (
                                     id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                     job_id BIGINT UNSIGNED NOT NULL,
                                     old_status INT NULL,
                                     new_status INT NOT NULL,
                                     action VARCHAR(50) NOT NULL,
                                     comment TEXT NULL,
                                     created_by BIGINT UNSIGNED NOT NULL,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (id)
);

CREATE INDEX idx_job_id
    ON r_job_audit_history (job_id);
