-- r_job_type增加字段 并且更新属性
alter table r_job_type
    add spanish_name varchar(128) null comment '西班牙语',
    add japanese_name varchar(128) null comment '日语';

-- 新增：雇佣类型表 → spanish_name(西班牙语) + japanese_name(日语) 字段批量翻译更新脚本
UPDATE r_job_type
SET spanish_name = CASE
                       WHEN type_name = 'Full Time'  THEN 'Tiempo completo'
                       WHEN type_name = 'Part Time'  THEN 'Medio tiempo'
                       WHEN type_name = 'Contract'   THEN 'Trabajador por contrato'
                       WHEN type_name = 'Temporary'  THEN 'Trabajo temporal'
                       WHEN type_name = 'Internship' THEN 'Prácticas profesionales'
                       WHEN type_name = 'Other'      THEN 'Otros'
                       ELSE spanish_name
    END,
    japanese_name = CASE
                        WHEN type_name = 'Full Time'  THEN '正社員'
                        WHEN type_name = 'Part Time'  THEN 'パートタイム'
                        WHEN type_name = 'Contract'   THEN '契約社員'
                        WHEN type_name = 'Temporary'  THEN '派遣社員'
                        WHEN type_name = 'Internship' THEN 'インターン生'
                        WHEN type_name = 'Other'      THEN 'その他'
                        ELSE japanese_name
        END
WHERE type_name IN ('Full Time', 'Part Time', 'Contract', 'Temporary', 'Internship', 'Other');

