-- r_job_mode增加字段 并且更新属性
alter table r_job_mode
    add spanish_name varchar(128) null comment '西班牙语',
    add japanese_name varchar(128) null comment '日语';

-- 办公模式表：合并更新 日语+西班牙语 三个字段【单条SQL，推荐】
UPDATE r_job_mode
SET japanese_name = CASE
                        WHEN mode_name = 'On-Site' THEN '出社勤務'
                        WHEN mode_name = 'Remote' THEN '在宅勤務'
                        WHEN mode_name = 'Hybrid' THEN 'ハイブリッド勤務'
                        WHEN mode_name = 'Other' THEN 'その他'
                        ELSE japanese_name
        END,
    spanish_name = CASE
                       WHEN mode_name = 'On-Site' THEN 'Presencial'
                       WHEN mode_name = 'Remote' THEN 'Remoto'
                       WHEN mode_name = 'Hybrid' THEN 'Híbrido'
                       WHEN mode_name = 'Other' THEN 'Otro'
                       ELSE spanish_name
        END
WHERE mode_name IN ('On-Site', 'Remote', 'Hybrid', 'Other');
