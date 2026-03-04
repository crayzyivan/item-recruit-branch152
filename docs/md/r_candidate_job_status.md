# 更新应聘者职位关系状态 
## controller层写在CandidateJobController
## 根据r_candidate_job主键 更新apply_status状态
## 入参是一个对象 包括主键以及状态字段 增加validation相关注解校验 message使用英文
## 增加数据是否存在的校验
## 返回是否更新成功

# 代码层次
## controller层写在CandidateJobController
## 业务service写在candidateJobDomainService
## 操作数据库的service如果自定义的更新则写在CandidateJobService中
## 定义一个状态枚举类 枚举形式参考SalaryType枚举 枚举中的状态有 简历申请状态(0:未被查看;1:已送达;2:录用;3:筛选中,4:筛选未通过,5:AI面试,6:AI面试未通过,7:背调,8:背调未通过-1:拒绝)