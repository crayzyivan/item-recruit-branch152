-- 添加面试链接字段到r_candidate_job表
ALTER TABLE r_candidate_job
    ADD COLUMN interview_url TEXT COMMENT '面试链接';