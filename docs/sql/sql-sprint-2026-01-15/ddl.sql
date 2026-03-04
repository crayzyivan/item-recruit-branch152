ALTER TABLE recruit.r_ai_callback ADD interviewer_phone varchar(20) NULL COMMENT '面试电话';

ALTER TABLE recruit.r_candidate_job ADD preferred_interview_start_time varchar(30) DEFAULT NULL COMMENT '候选人期望面试时间段';
ALTER TABLE recruit.r_candidate_job ADD preferred_interview_end_time varchar(30) DEFAULT NULL COMMENT '候选人期望面试时间段';
ALTER TABLE recruit.r_candidate_job ADD interview_phone varchar(20) DEFAULT NULL COMMENT '候选人面试用电话';
ALTER TABLE recruit.r_candidate_job ADD interview_phone_status tinyint DEFAULT '0' COMMENT '是否已预约ai电话面试(0:未预约，1:已预约)';
ALTER TABLE recruit.r_candidate_job ADD interview_language varchar(5) DEFAULT NULL COMMENT '候选人面试用语言';
ALTER TABLE recruit.r_candidate_job ADD phone_recording_url varchar(200) NULL COMMENT '电话录音url';

ALTER TABLE recruit.r_ai_vetted_result ADD phone_recording_url varchar(200) NULL COMMENT '电话录音url';

CREATE TABLE `r_candidate_recall` (
                                      `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `candidate_job_id` bigint unsigned NOT NULL COMMENT '候选人职位关联ID',
                                      `preferred_interview_start_time` varchar(30) NOT NULL DEFAULT '' COMMENT '期望面试开始时间',
                                      `preferred_interview_end_time` varchar(30) NOT NULL DEFAULT '' COMMENT '期望面试结束时间',
                                      `interview_phone` varchar(20) NOT NULL DEFAULT '' COMMENT '面试联系电话',
                                      `interview_language` varchar(5) NOT NULL DEFAULT '' COMMENT '面试语言',
                                      `schedule_id` varchar(24) NOT NULL DEFAULT '' COMMENT '日程安排ID',
                                      `sms_status` varchar(2) DEFAULT '0' COMMENT '发送通知短信状态(0:失败,1:成功)',
                                      `recall_status` varchar(2) DEFAULT '0' COMMENT '是否回拨成功（0：未回拨，1：已回拨）',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除0未删除 1已删除',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人电话面试回拨表';


CREATE TABLE `r_languages` (
                               `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
                               `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ISO 639-1 双字母语言代码（核心字段）',
                               `name_en` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语言英文标准名称',
                               `name_zh` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语言中文标准名称',
                               `native_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '语言原生名称（如日语：日本語）',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_code` (`code`) COMMENT '语言代码唯一约束（ISO 639-1 代码不重复）'
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ISO 639-1 语言代码标准表';

INSERT INTO recruit.r_languages
(code, name_en, name_zh, native_name)
VALUES('en', 'English', '英语', 'English');
INSERT INTO recruit.r_languages
(code, name_en, name_zh, native_name)
VALUES('zh', 'Chinese', '中文', '中文');

ALTER TABLE recruit.r_ai_vetted_result ADD soft_skill_score tinyint NULL COMMENT '软技能得分';
ALTER TABLE recruit.r_ai_callback ADD interviewer_phone varchar(20) NULL COMMENT '面试电话';

ALTER TABLE recruit.r_candidate_job ADD preferred_interview_start_time varchar(30) DEFAULT NULL COMMENT '候选人期望面试时间段';
ALTER TABLE recruit.r_candidate_job ADD preferred_interview_end_time varchar(30) DEFAULT NULL COMMENT '候选人期望面试时间段';
ALTER TABLE recruit.r_candidate_job ADD interview_phone varchar(20) DEFAULT NULL COMMENT '候选人面试用电话';
ALTER TABLE recruit.r_candidate_job ADD interview_phone_status tinyint DEFAULT '0' COMMENT '是否已预约ai电话面试(0:未预约，1:已预约)';
ALTER TABLE recruit.r_candidate_job ADD interview_language varchar(5) DEFAULT NULL COMMENT '候选人面试用语言';
ALTER TABLE recruit.r_candidate_job ADD phone_recording_url varchar(200) NULL COMMENT '电话录音url';

ALTER TABLE recruit.r_ai_vetted_result ADD technical_skill_score tinyint NULL COMMENT '候选人技能分';

ALTER TABLE recruit.r_ai_vetted_result ADD overall_score tinyint NULL COMMENT '根据岗位权重计算总分';
ALTER TABLE recruit.r_ai_vetted_result ADD phone_recording_url varchar(200) NULL COMMENT '电话录音url';

CREATE TABLE `r_candidate_recall` (
                                      `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `candidate_job_id` bigint unsigned NOT NULL COMMENT '候选人职位关联ID',
                                      `preferred_interview_start_time` varchar(30) NOT NULL DEFAULT '' COMMENT '期望面试开始时间',
                                      `preferred_interview_end_time` varchar(30) NOT NULL DEFAULT '' COMMENT '期望面试结束时间',
                                      `interview_phone` varchar(20) NOT NULL DEFAULT '' COMMENT '面试联系电话',
                                      `interview_language` varchar(5) NOT NULL DEFAULT '' COMMENT '面试语言',
                                      `schedule_id` varchar(24) NOT NULL DEFAULT '' COMMENT '日程安排ID',
                                      `sms_status` varchar(2) DEFAULT '0' COMMENT '发送通知短信状态(0:失败,1:成功)',
                                      `recall_status` varchar(2) DEFAULT '0' COMMENT '是否回拨成功（0：未回拨，1：已回拨）',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除0未删除 1已删除',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人电话面试回拨表';


CREATE TABLE `r_languages` (
                               `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
                               `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ISO 639-1 双字母语言代码（核心字段）',
                               `name_en` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语言英文标准名称',
                               `name_zh` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语言中文标准名称',
                               `native_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '语言原生名称（如日语：日本語）',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_code` (`code`) COMMENT '语言代码唯一约束（ISO 639-1 代码不重复）'
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ISO 639-1 语言代码标准表';

INSERT INTO recruit.r_languages
(code, name_en, name_zh, native_name)
VALUES('en', 'English', '英语', 'English');
INSERT INTO recruit.r_languages
(code, name_en, name_zh, native_name)
VALUES('zh', 'Chinese', '中文', '中文');