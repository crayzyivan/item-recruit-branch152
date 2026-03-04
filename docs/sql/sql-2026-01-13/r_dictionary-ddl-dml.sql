-- 字典表r_dictionary增加字段
alter table r_dictionary
    add spanish_name varchar(128) null comment '西班牙语',
    add japanese_name varchar(128) null comment '日语';
-- 字典表更新属性
-- 性别表（gender）：value → japanese_name + spanish_name 双字段合并更新
UPDATE r_dictionary
SET japanese_name = CASE
                        WHEN value = 'Male' THEN '男性'
                        WHEN value = 'Female' THEN '女性'
                        WHEN value = 'Non-binary' THEN 'ノンバイナリー'
                        WHEN value = 'Prefer not to say' THEN '回答を希望しない'
                        ELSE japanese_name -- 保留已有日语值，不覆盖
    END,
    spanish_name = CASE
                       WHEN value = 'Male' THEN 'Masculino'
                       WHEN value = 'Female' THEN 'Femenino'
                       WHEN value = 'Non-binary' THEN 'No binario'
                       WHEN value = 'Prefer not to say' THEN 'Prefiero no decirlo'
                       ELSE spanish_name -- 保留已有西班牙语值，不覆盖
        END
WHERE value IN ('Male', 'Female', 'Non-binary', 'Prefer not to say');


-- 2. 货币类型表（currency）：remark（符号）→ japanese_name(日语名称) + spanish_name(西班牙语名称)
UPDATE r_dictionary
SET japanese_name = CASE
                        WHEN remark = 'US Dollar' THEN '米ドル'
                        WHEN remark = 'British Pound' THEN '英ポンド'
                        WHEN remark = 'Euro' THEN 'ユーロ'
                        WHEN remark = 'Canadian Dollar' THEN 'カナダドル'
                        WHEN remark = 'Chinese Yuan' THEN '中国元'
                        WHEN remark = 'Indian Rupee' THEN 'インドルピー'
                        WHEN remark = 'Philippine Peso' THEN 'フィリピンペソ'
                        ELSE japanese_name -- 保留已有日语值，不覆盖
    END,
    spanish_name = CASE
                       WHEN remark = 'US Dollar' THEN 'Dólar estadounidense'
                       WHEN remark = 'British Pound' THEN 'Libra esterlina'
                       WHEN remark = 'Euro' THEN 'Euro'
                       WHEN remark = 'Canadian Dollar' THEN 'Dólar canadiense'
                       WHEN remark = 'Chinese Yuan' THEN 'Yuan chino'
                       WHEN remark = 'Indian Rupee' THEN 'Rupia india'
                       WHEN remark = 'Philippine Peso' THEN 'Peso filipino'
                       ELSE spanish_name -- 保留已有西班牙语值，不覆盖
        END
WHERE remark IN ('US Dollar', 'British Pound', 'Euro', 'Canadian Dollar', 'Chinese Yuan', 'Indian Rupee', 'Philippine Peso');


-- 3. 薪资周期表（pay_period）：value → japanese_name(日语名称) + spanish_name(西班牙语名称)
UPDATE r_dictionary
SET japanese_name = CASE
                        WHEN value = 'Hourly' THEN '時間給'
                        WHEN value = 'Daily' THEN '日給'
                        WHEN value = 'Weekly' THEN '週給'
                        WHEN value = 'Monthly' THEN '月給'
                        WHEN value = 'Yearly' THEN '年俸'
                        WHEN value = 'Commission' THEN '歩合制'
                        ELSE japanese_name -- 保留原有值，不覆盖脏数据
    END,
    spanish_name = CASE
                       WHEN value = 'Hourly' THEN 'Por hora'
                       WHEN value = 'Daily' THEN 'Diario'
                       WHEN value = 'Weekly' THEN 'Semanal'
                       WHEN value = 'Monthly' THEN 'Mensual'
                       WHEN value = 'Yearly' THEN 'Anual'
                       WHEN value = 'Commission' THEN 'Por comisión'
                       ELSE spanish_name -- 保留原有值，不覆盖脏数据
        END
WHERE value IN ('Hourly', 'Daily', 'Weekly', 'Monthly', 'Yearly', 'Commission');


-- 4. 教育阶段表（education_stage）：value → japanese_name(日语名称) + spanish_name(西班牙语名称)
UPDATE r_dictionary
SET japanese_name = CASE
                        WHEN value = 'High School' THEN '高等学校'
                        WHEN value = 'Community College' THEN 'コミュニティカレッジ'
                        WHEN value = 'University' THEN '大学'
                        WHEN value = 'Vocational/Technical School' THEN '専門・技術学校'
                        WHEN value = 'Other' THEN 'その他'
                        ELSE japanese_name
    END,
    spanish_name = CASE
                       WHEN value = 'High School' THEN 'Bachillerato'
                       WHEN value = 'Community College' THEN 'Colegio comunitario'
                       WHEN value = 'University' THEN 'Universidad'
                       WHEN value = 'Vocational/Technical School' THEN 'Colegio profesional/técnico'
                       WHEN value = 'Other' THEN 'Otro'
                       ELSE spanish_name
        END
WHERE value IN ('High School', 'Community College', 'University', 'Vocational/Technical School', 'Other');

-- 5. 学历层次表（education_degree）：value → japanese_name(日语名称) + spanish_name(西班牙语名称)
UPDATE r_dictionary
SET japanese_name = CASE
                        WHEN value = 'High School Diploma' THEN '高等学校卒業証書'
                        WHEN value = 'GED' THEN '一般教育発達証書'
                        WHEN value = 'Associate''s Degree' THEN '准学士号'
                        WHEN value = 'Bachelor''s Degree' THEN '学士号'
                        WHEN value = 'Master''s Degree' THEN '修士号'
                        WHEN value = 'Doctoral Degree' THEN '博士号'
                        WHEN value = 'Post-Doctoral' THEN '博士後期課程'
                        WHEN value = 'Other' THEN 'その他'
                        ELSE japanese_name
    END,
    spanish_name = CASE
                       WHEN value = 'High School Diploma' THEN 'Título de escuela secundaria'
                       WHEN value = 'GED' THEN 'Diploma de Educación General'
                       WHEN value = 'Associate''s Degree' THEN 'Título asociado'
                       WHEN value = 'Bachelor''s Degree' THEN 'Licenciatura'
                       WHEN value = 'Master''s Degree' THEN 'Maestría'
                       WHEN value = 'Doctoral Degree' THEN 'Doctorado'
                       WHEN value = 'Post-Doctoral' THEN 'Postdoctorado'
                       WHEN value = 'Other' THEN 'Otro'
                       ELSE spanish_name
        END
WHERE value IN (
                'High School Diploma', 'GED', 'Associate''s Degree',
                'Bachelor''s Degree', 'Master''s Degree', 'Doctoral Degree',
                'Post-Doctoral', 'Other'
    );