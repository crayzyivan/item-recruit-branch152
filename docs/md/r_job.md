# 根据下放的表格 生成代码
```sql
CREATE TABLE `r_job` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `customer_id` bigint NOT NULL COMMENT '客户Id',
  `master_account_id` bigint NOT NULL COMMENT '客户主账号Id',
  `longitude` decimal(9,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(8,6) DEFAULT NULL COMMENT '纬度',
  `location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '位置',
  `job_status` tinyint NOT NULL COMMENT '状态(0:draf,1:active,-1:closed,2:other)',
  `need_listed` tinyint(1) NOT NULL COMMENT '是否需要显示在列表中',
  `url_code` varchar(100) DEFAULT NULL COMMENT 'url中显示的字符用-连接',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL,
  `min_salary` int DEFAULT NULL COMMENT '最小薪资(单位:分)',
  `max_salary` int DEFAULT NULL COMMENT '最大信息(单位:分)',
  `category_id` int DEFAULT NULL,
  `type_id` int DEFAULT NULL,
  `create_by` bigint NOT NULL COMMENT '创建人Id',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `ext_1` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '扩展字段1',
  `ext_2` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '扩展字段2',
  `deleted` tinyint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci


```



## 根据这个表生成对应的实体
## 不使用mybatis的 xml文件 使用QueryWrapper模式 生成增删改查
## 生成一个发布职位的controller 根据上面表格中的字段生成 增加职位详情、职位要求、主要职责、技能相关字段 并且使用validation校验
## 生成一个插入es的数据 将其中的id、master_account_id、title 、location、 url_code 、职位详情、职位要求、主要职责、技能 属性存到es中
## 增加一个根据搜索内容获取职位列表的controller 需要从es中根据title 、location、 url_code 、职位详情、职位要求、主要职责、技能 属性分页查询 
## 从es查询的时候注意深分页问题 使用最优方案写这个代码 将es中的实体和mysql中的实体隔离开
## 将es底层相关的操作存在es目录中不要放在mapper中