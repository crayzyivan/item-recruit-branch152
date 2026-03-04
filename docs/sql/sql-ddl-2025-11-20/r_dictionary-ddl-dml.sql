-- 字典表r_dictionary增加字段
alter table r_dictionary
    add `chinese_name` varchar(100) null comment '字典中value属性中文名';
-- 字典表更新属性
-- 1. 性别表（gender）：value → chinese_name
UPDATE r_dictionary
SET chinese_name = CASE
                       WHEN value = 'Male' THEN '男性'
                       WHEN value = 'Female' THEN '女性'
                       WHEN value = 'Non-binary' THEN '非二元'
                       WHEN value = 'Prefer not to say' THEN '不愿透露'
                       ELSE chinese_name -- 保留已有中文值，不覆盖
    END
WHERE value IN ('Male', 'Female', 'Non-binary', 'Prefer not to say');
-- 仅更新目标原始值


-- 2. 货币类型表（currency）：remark（符号）→ chinese_name（中文名称）
UPDATE r_dictionary
SET chinese_name = CASE
                       WHEN remark = 'US Dollar' THEN '美元'
                       WHEN remark = 'British Pound' THEN '英镑'
                       WHEN remark = 'Euro' THEN '欧元'
                       WHEN remark = 'Canadian Dollar' THEN '加拿大元'
                       WHEN remark = 'Chinese Yuan' THEN '人民币'
                       WHEN remark = 'Indian Rupee' THEN '印度卢比'
                       WHEN remark = 'Philippine Peso' THEN '菲律宾比索'
                       ELSE chinese_name -- 保留已有中文值，不覆盖其他未匹配的记录
    END
WHERE remark IN ('US Dollar', 'British Pound', 'Euro', 'Canadian Dollar', 'Chinese Yuan', 'Indian Rupee', 'Philippine Peso');


-- 3. 薪资周期表（pay_period）：value → chinese_name
UPDATE r_dictionary
SET chinese_name = CASE
                       WHEN value = 'Hourly' THEN '时薪'
                       WHEN value = 'Daily' THEN '日薪'
                       WHEN value = 'Weekly' THEN '周薪'
                       WHEN value = 'Monthly' THEN '月薪'
                       WHEN value = 'Yearly' THEN '年薪'
                       WHEN value = 'Commission' THEN '佣金制'
                       ELSE chinese_name
    END
WHERE value IN ('Hourly', 'Daily', 'Weekly', 'Monthly', 'Yearly', 'Commission');


-- 4. 教育阶段表（education_stage）：value → chinese_name
UPDATE r_dictionary
SET chinese_name = CASE
                       WHEN value = 'High School' THEN '高中'
                       WHEN value = 'Community College' THEN '社区大学'
                       WHEN value = 'University' THEN '大学'
                       WHEN value = 'Vocational/Technical School' THEN '职业/技术学校'
                       WHEN value = 'Other' THEN '其他'
                       ELSE chinese_name
    END
WHERE value IN ('High School', 'Community College', 'University', 'Vocational/Technical School', 'Other');

-- 5. 学历层次表（education_degree）：value → chinese_name
UPDATE r_dictionary
SET chinese_name = CASE
                       WHEN value = 'High School Diploma' THEN '高中文凭'
                       WHEN value = 'GED' THEN '普通教育文凭'
                       WHEN value = 'Associate''s Degree' THEN '副学士学位'
                       WHEN value = 'Bachelor''s Degree' THEN '学士学位'
                       WHEN value = 'Master''s Degree' THEN '硕士学位'
                       WHEN value = 'Doctoral Degree' THEN '博士学位'
                       WHEN value = 'Post-Doctoral' THEN '博士后'
                       WHEN value = 'Other' THEN '其他'
                       ELSE chinese_name
    END
WHERE value IN (
                'High School Diploma', 'GED', 'Associate''s Degree',
                'Bachelor''s Degree', 'Master''s Degree', 'Doctoral Degree',
                'Post-Doctoral', 'Other'
    );