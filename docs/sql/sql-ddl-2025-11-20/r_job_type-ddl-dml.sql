-- r_job_type增加字段 并且更新属性
alter table r_job_type
    add chinese_name varchar(100) null comment '中文名称';

-- 雇佣类型表：name字段（英文）→ chinese_name字段（中文）更新脚本;
UPDATE r_job_type
SET chinese_name = CASE
                       WHEN type_name = 'Full Time' THEN '全职'
                       WHEN type_name = 'Part Time' THEN '兼职'
                       WHEN type_name = 'Contract' THEN '合同工'
                       WHEN type_name = 'Temporary' THEN '临时工'
                       WHEN type_name = 'Internship' THEN '实习生'
                       WHEN type_name = 'Other' THEN '其他'
                       ELSE chinese_name  -- 不覆盖已有中文值或非目标数据
    END
WHERE type_name IN ('Full Time', 'Part Time', 'Contract', 'Temporary', 'Internship', 'Other');
