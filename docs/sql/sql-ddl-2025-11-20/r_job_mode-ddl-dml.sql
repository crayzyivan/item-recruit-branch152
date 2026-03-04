-- r_job_mode增加字段 并且更新属性
alter table r_job_mode
    add chinese_name varchar(100) null comment '中文';

-- 办公模式表：name字段（英文）→ chinese_name字段（中文）更新脚本
UPDATE r_job_mode
SET chinese_name = CASE
                       WHEN mode_name = 'On-Site' THEN '现场办公'
                       WHEN mode_name = 'Remote' THEN '远程办公'
                       WHEN mode_name = 'Hybrid' THEN '混合办公'
                       WHEN mode_name = 'Other' THEN '其他'
                       ELSE chinese_name
    END
WHERE mode_name IN ('On-Site', 'Remote', 'Hybrid', 'Other');
