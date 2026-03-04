# 根据下放的表格 生成代码
```sql
CREATE TABLE `r_candidate` (
                               `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                               `candidate_name` varchar(50) CHARACTER SET utf8mb3 NOT NULL COMMENT '候选人名字full name',
                               `candidate_email` varchar(100) CHARACTER SET utf8mb3 NOT NULL,
                               `password` varchar(100) CHARACTER SET utf8mb3 DEFAULT NULL,
                               `resume_url` varchar(500) CHARACTER SET utf8mb3 DEFAULT NULL,
                               `create_time` datetime DEFAULT NULL,
                               `update_time` datetime DEFAULT NULL,
                               `phone_number` varchar(20) CHARACTER SET utf8mb3 DEFAULT NULL,
                               `ext_1` varchar(200) CHARACTER SET utf8mb3 DEFAULT NULL,
                               `ext_2` varchar(200) CHARACTER SET utf8mb3 DEFAULT NULL,
                               `deleted` tinyint DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci




```



## 根据这个表生成对应的实体 并且补充注释
## 不使用mybatis的 xml文件 使用LambdaQueryWrapper模式 生成查寻的service逻辑 单个查询和列表查询
