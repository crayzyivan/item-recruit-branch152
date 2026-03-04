CREATE TABLE `r_ayrshare_company_config` (
                                             `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                                             `ayrshare_api_key` varchar(64) NOT NULL DEFAULT '' COMMENT 'ayrshare平台API KEY；长度是冗余了一半',
                                             `ayrshare_domain` varchar(128) NOT NULL DEFAULT 'id-7f3ce' COMMENT 'ayrshare平台domain',
                                             `ayrshare_profile_key` varchar(64) NOT NULL DEFAULT '' COMMENT 'ayrshare的配置；长度是冗余了一半',
                                             `ayrshare_subreddit` varchar(256) NOT NULL DEFAULT 'recruit' COMMENT 'reddit平台属性预留',
                                             `create_by_id` bigint NOT NULL DEFAULT '0' COMMENT '创建人',
                                             `create_by_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人name',
                                             `update_by_id` bigint DEFAULT NULL COMMENT '更新人id',
                                             `update_by_name` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人name',
                                             `company_code` varchar(64) NOT NULL DEFAULT '' COMMENT 'companyCode',
                                             `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0未删除 1已删除',
                                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                             PRIMARY KEY (`id`),
                                             KEY `idx_company_code` (`company_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='company ayrshare配置信息';

ALTER TABLE `r_job` ADD COLUMN `ayrshare_status` int NOT NULL DEFAULT 0 COMMENT 'Ayrshare分享状态：0-不需要分享，1-分享中，2-分享成功，3-部分成功，-1-分享失败';

-- ====================================
-- 1. Create r_xml_feed_config table
-- ====================================
CREATE TABLE `r_xml_feed_config` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `company_code` VARCHAR(50) NOT NULL COMMENT '公司代码',
                                     `platform_type` TINYINT NOT NULL COMMENT '平台类型：1-LinkedIn, 2-Indeed',
                                     `feed_url` VARCHAR(200) NOT NULL COMMENT 'XML Feed URL地址',
                                     `account_email` VARCHAR(100) NULL COMMENT 'Indeed专用账户邮箱',
                                     `guide_url` VARCHAR(300) NULL COMMENT '平台指导文档URL',
                                     `update_interval_hours` TINYINT NOT NULL COMMENT '自动更新间隔（小时），范围1-24',
                                     `last_update_time` DATETIME NULL COMMENT '最后更新时间',
                                     `next_update_time` DATETIME NULL COMMENT '下次更新时间',
                                     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `create_by` BIGINT NULL COMMENT '创建人ID',
                                     `update_by` BIGINT NULL COMMENT '修改人ID',
                                     `deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除：false-否，true-是',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='XML Feed配置表';

-- ====================================
-- 2. Create indexes for r_xml_feed_config
-- ====================================

-- 唯一索引：公司代码 + 平台类型 + 删除标记
CREATE UNIQUE INDEX `idx_xml_feed_config_company_platform` ON `r_xml_feed_config` (
                                                                                   `company_code`,
                                                                                   `platform_type`,
                                                                                   `deleted`
    );

-- 索引：下次更新时间 + 删除标记（用于定时任务查询）
CREATE INDEX `idx_xml_feed_config_next_update` ON `r_xml_feed_config` (
                                                                       `next_update_time`,
                                                                       `deleted`
    );

-- 索引：账户邮箱 + 平台类型 + 删除标记（用于邮箱唯一性检查）
CREATE INDEX `idx_xml_feed_config_account_email` ON `r_xml_feed_config` (
                                                                         `account_email`,
                                                                         `platform_type`,
                                                                         `deleted`
    );

-- ====================================
-- 3. Create r_xml_feed_update_log table
-- ====================================
CREATE TABLE `r_xml_feed_update_log` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `feed_config_id` BIGINT NOT NULL COMMENT 'Feed配置ID',
                                         `update_type` TINYINT NOT NULL COMMENT '更新类型：1-手动更新，2-自动更新',
                                         `update_status` TINYINT NOT NULL COMMENT '更新状态：1-成功，2-失败',
                                         `job_count` INT NULL COMMENT '更新的职位数量',
                                         `error_message` TEXT NULL COMMENT '错误信息',
                                         `operator_id` BIGINT NULL COMMENT '操作人ID',
                                         `start_time` DATETIME NOT NULL COMMENT '开始时间',
                                         `end_time` DATETIME NULL COMMENT '结束时间',
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='XML Feed更新日志表';

-- ====================================
-- 4. Create indexes for r_xml_feed_update_log
-- ====================================

-- 索引：配置ID + 开始时间倒序（用于日志查询）
CREATE INDEX `idx_xml_feed_log_config_id` ON `r_xml_feed_update_log` (
                                                                      `feed_config_id`,
                                                                      `start_time` DESC
    );

-- 索引：更新状态 + 开始时间倒序（用于状态统计查询）
CREATE INDEX `idx_xml_feed_log_status_time` ON `r_xml_feed_update_log` (
                                                                        `update_status`,
                                                                        `start_time` DESC
    );