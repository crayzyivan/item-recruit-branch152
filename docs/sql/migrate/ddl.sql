-- 数据迁移插入一个其他类型
INSERT INTO r_job_category(id, category_name)
VALUES (60,'Other');

-- 面试结果字段变更
ALTER TABLE r_ai_vetted_result
    ADD COLUMN data_source INT NOT NULL DEFAULT 0
    COMMENT '数据来源(0:recruit,1菲律宾)';
alter table r_ai_vetted_result MODIFY overall_skill_assessment TEXT COMMENT '评价';
alter table r_ai_vetted_result_skill MODIFY skill_assessment TEXT COMMENT '评价';
ALTER TABLE r_ai_vetted_result_skill MODIFY skill_score tinyint NULL COMMENT '技能评分';
ALTER TABLE r_job_status_record MODIFY COLUMN create_time datetime NULL COMMENT '创建时间';

-- 映射关联关系
CREATE TABLE `r_data_migration_pgsql_mysql_id` (
                                                   `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                   `pgsql_id` varchar(255) NOT NULL DEFAULT '' COMMENT 'PostgreSQL库中主键ID',
                                                   `mysql_id` bigint NOT NULL DEFAULT '0' COMMENT 'MySQL库中主键ID',
                                                   `bus_type` int NOT NULL DEFAULT '0' COMMENT '业务类型：1-职位表，2-候选人表，3-候选人职位关联表，4-工作经历表，5-候选人教育背景表，6-面试报告表，7-背景调查数据表',
                                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                   `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
                                                   `ext` text COMMENT '扩展属性',
                                                   PRIMARY KEY (`id`),
                                                   UNIQUE KEY `uk_pgsql_id_bus_type` (`pgsql_id`,`bus_type`) COMMENT 'PostgreSQL ID和业务类型唯一索引',
                                                   KEY `idx_mysql_id_bus_type` (`mysql_id`,`bus_type`) COMMENT 'MySQL ID和业务类型索引',
                                                   KEY `idx_bus_type` (`bus_type`) COMMENT '业务类型索引',
                                                   KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据迁移PostgreSQL-MySQL ID映射关系表';

