alter table r_job
    add interview_length int default 10 not null comment '面试时长 单位分钟';

alter table r_job
    modify create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间';

ALTER TABLE `r_points_operation_log`
    ADD COLUMN `pricing_model` tinyint(1) NOT NULL DEFAULT 0 COMMENT '计费模式(0:Minute Based,1:Token Based)';

ALTER TABLE `r_points_operation_log`
    ADD COLUMN `duration_seconds` int COMMENT '面试时长秒数';

ALTER TABLE r_points_operation_log MODIFY remark VARCHAR(1000);

ALTER TABLE `r_points_operation_log`
    ADD COLUMN `duration_minutes` int COMMENT '面试时长分钟数';
ALTER TABLE `r_points_operation_log`
    ADD COLUMN `consumed_tokens` int COMMENT '消耗的token数';