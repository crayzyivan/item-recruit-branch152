-- 步骤1: 添加新字段 apply_method
ALTER TABLE r_candidate_job
    ADD COLUMN apply_method int not null default 0 COMMENT '申请方式(0:平台申请;1:邀请面试自动投递)';
-- 步骤1: 添加新字段 is_password_told (可为null)
ALTER TABLE r_candidate
    ADD COLUMN is_password_told int NULL COMMENT '密码是否已告知(0:未告知,1:已告知)';


-- === r_cities 表 ===
ALTER TABLE r_cities
    ADD COLUMN chinese_name VARCHAR(100) COMMENT '中文名称' AFTER name;

-- === r_countries 表 ===
ALTER TABLE r_countries
    ADD COLUMN chinese_name VARCHAR(100) COMMENT '中文名称' AFTER name;


-- === r_states 表 ===
ALTER TABLE r_states
    ADD COLUMN chinese_name VARCHAR(100) COMMENT '中文名称' AFTER name;