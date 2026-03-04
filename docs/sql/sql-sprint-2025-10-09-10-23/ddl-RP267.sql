-- RP-267: C端用户新邮箱字段兼容
-- 在 r_candidate 表中新增 candidate_permanent_email 字段来存储用户不可变的邮箱地址
-- 时间范围: 2025-10-09 到 2025-10-23
-- 作者: hua.liu
-- 日期: 2025-10-11

-- 步骤1: 添加新字段 candidate_permanent_email (初始为可空)
ALTER TABLE r_candidate
ADD candidate_permanent_email varchar(128) COMMENT '用户不能改变的email';

-- 步骤2: 数据迁移 - 将现有的 candidate_email 值复制到新字段
UPDATE r_candidate
SET candidate_permanent_email = candidate_email 
WHERE r_candidate.candidate_permanent_email IS NULL;

-- 步骤3: 修改字段为 NOT NULL 约束
ALTER TABLE r_candidate
MODIFY COLUMN candidate_permanent_email varchar(128) NOT NULL COMMENT '用户不能改变的email';

-- 步骤4: 创建索引以提高查询性能
CREATE INDEX idx_candidate_permanent_email
ON r_candidate (candidate_permanent_email)
COMMENT '不变邮箱索引';

-- 说明:
-- 1. candidate_permanent_email 字段用于存储用户注册时的原始邮箱，该邮箱不会被用户修改
-- 2. MKT注册和第三方登录注册的用户都会将邮箱存储到此字段
-- 3. recruit系统的userInfo接口将使用此字段返回应聘者的邮箱信息
-- 4. 此字段与现有的 candidate_email 字段并存，candidate_email 可能会被用户修改
-- 5. 执行顺序很重要：先添加可空字段 -> 数据填充 -> 设置NOT NULL约束 -> 创建索引


-- 音频面试
ALTER TABLE `r_points_operation_log`
ADD COLUMN `freeze_interview_type` TINYINT NOT NULL DEFAULT 0 COMMENT 'freeze interview type(0:video,1:audio)',
ADD COLUMN `actual_interview_type` TINYINT NULL COMMENT 'actual interview type(0:video,1:audio)';
ALTER TABLE `r_job`
ADD COLUMN `interview_type` TINYINT NOT NULL DEFAULT 0 COMMENT 'interview type(0:video,1:audio)' AFTER `interview_length`;
ALTER TABLE `r_ai_vetted_result`
ADD COLUMN `interview_type` TINYINT NOT NULL DEFAULT 0 COMMENT 'interview type(0:video,1:audio)' AFTER `candidate_job_id`;

-- 根据job推荐应聘者发送邀请邮件
CREATE TABLE `r_recommendation_candidate_job` (
                                                  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                                  `job_id` bigint unsigned NOT NULL COMMENT '岗位id',
                                                  `candidate_id` bigint unsigned NOT NULL COMMENT '应聘者id',
                                                  `recommend_by` bigint unsigned DEFAULT NULL COMMENT '推荐人的id',
                                                  `candidate_email` varchar(256) DEFAULT NULL COMMENT '推荐时发送的邮箱信息',
                                                  `recommend_time` datetime NOT NULL COMMENT '推荐时间',
                                                  `recommend_reasons` varchar(512) DEFAULT NULL COMMENT '推荐理由',
                                                  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除 0未删除 1已删除',
                                                  `create_time` datetime NOT NULL COMMENT '创建时间',
                                                  `update_time` datetime NOT NULL COMMENT '更新时间',
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发送job推荐应聘者邮件记录表';
-- 添加索引
CREATE INDEX idx_candidate_id_job_id ON r_recommendation_candidate_job (candidate_id, job_id);
CREATE INDEX idx_job_id_candidate_id ON r_recommendation_candidate_job (job_id, candidate_id);