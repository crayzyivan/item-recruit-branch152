-- === r_cities 表 新增西语/日语名称字段 ===
ALTER TABLE r_cities
    ADD spanish_name VARCHAR(128) NULL COMMENT '西班牙语',
    ADD japanese_name VARCHAR(128) NULL COMMENT '日语';

-- === r_countries 表 新增西语/日语名称字段 ===
ALTER TABLE r_countries
    ADD spanish_name VARCHAR(128) NULL COMMENT '西班牙语',
    ADD japanese_name VARCHAR(128) NULL COMMENT '日语';

-- === r_states 表 新增西语/日语名称字段 ===
ALTER TABLE r_states
    ADD spanish_name VARCHAR(128) NULL COMMENT '西班牙语',
    ADD japanese_name VARCHAR(128) NULL COMMENT '日语';