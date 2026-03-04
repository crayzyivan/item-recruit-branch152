CREATE TABLE `r_places` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `language` varchar(10) DEFAULT NULL COMMENT '目前支持 4 种语言',
                            `place_id` text DEFAULT NULL COMMENT '对应 Google Map Place Id',
                            `description` text DEFAULT NULL COMMENT '对应语言的地点名称',
                            `address_components` text DEFAULT NULL COMMENT '对应语言的地址信息，对应 Google Map Place Detail 数据',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='谷歌地图地点信息表';

CREATE INDEX idx_place_id_prefix256
    ON r_places (place_id(256));

ALTER TABLE recruit.r_candidate_job ADD place_id text NULL COMMENT 'Google Map Place Id';

CREATE INDEX idx_iso2 USING BTREE ON recruit.r_countries (iso2);