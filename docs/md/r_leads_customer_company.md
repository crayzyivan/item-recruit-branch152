# 需求根据sql结构以及项目使用的技术栈以及现在项目代码结构 生成相关代码
## 具体sql结构以及字段 如下
```sql
            CREATE TABLE `r_leads_customer_company` (
                                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                                       `crm_leads_id` bigint NOT NULL DEFAULT '0' COMMENT '调用crm创建leads的id',
                                                       `crm_customer_id` bigint NOT NULL COMMENT 'leads 转customer的id',
                                                       `crm_customer_code` varchar(64) DEFAULT NULL COMMENT 'leads 转customer的code',
                                                       `crm_leads_customer_status` tinyint NOT NULL DEFAULT '1' COMMENT '转换状态1 初始创建leads， 2 leads转customer完成',
                                                       `central_company_code` varchar(64) DEFAULT NULL COMMENT '注册账号company_code',
                                                       `central_lead_company_id` bigint DEFAULT NULL COMMENT '注册账号返回leadCompanyId',
                                                       `central_manager_id` bigint DEFAULT NULL COMMENT '注册账号时返回',
                                                       `response_msg` varchar(512) DEFAULT NULL COMMENT '转换存储message',
                                                       `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志 0未删除 1已删除',
                                                       `create_time` datetime NOT NULL,
                                                       `update_time` datetime NOT NULL,
                                                       PRIMARY KEY (`id`),
                                                       KEY `idx_company_code` (`central_company_code`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='lead customer company 映射关系';
```

## 参考com.item.service.JobService结构

## 需要生成的代码包括
- 添加r_leads_customer_company数据
- 根据r_leads_customer_company.company_code 查对应的数据
- 根据r_leads_customer_company.company_code 更新对应的数据
- 根据r_leads_customer_company.company_code 删除对应的数据
- 根据r_leads_customer_company.id 查对应的数据
- 根据r_leads_customer_company.id 更新对应的数据
- 根据r_leads_customer_company.id 删除对应的数据

## 实体类参考com.item.entity.JobEntity