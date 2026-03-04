# 需求 根据jobId更新jobStatus状态字段 

## 接口写在controller层 入参是一个对象 其中validation校验 message使用英文提示 都是必须字段
## 业务层写在job domain service类中，
## 数据库根据jobId更新jobstatus，返回boolean 是否更新成功