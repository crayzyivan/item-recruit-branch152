# 根据职位ID生成职位分享链接需求

## 需求概述

根据职位ID查询job表数据，获取urlCode属性并生成用于分享的职位链接，便于外部访问和推广。

## 功能要求

### 接口设计
- **Controller**: JobController类中新增接口
- **路径**: `/job/share/job-info/{jobId}`
- **方法**: GET
- **入参**: jobId (路径参数)
- **返回**: String类型的分享链接

### Service层
- **接口**: JobDomainService中新增方法
- **方法名**: generateJobInfoShareLink
- **参数**: Long jobId
- **返回**: String分享链接

## 业务流程

1. **参数验证** - 验证jobId有效性
2. **数据查询** - 根据jobId查询r_job表获取JobEntity
3. **存在性检查** - 数据不存在则抛出JOB_NOT_FOUND异常
4. **获取URL编码** - 提取urlCode属性
5. **生成短ID** - 为jobId生成唯一短标识
6. **拼接链接** - 按规则组装完整分享链接
7. **返回结果** - 返回生成的分享链接

## 实现规范

### 数据查询
- 使用JobService.getJobsByIds方法查询职位数据
- 职位不存在时抛出BusinessException异常

### 链接生成规则
参考arySharePost方法的实现规则：
- 基础URL + 前缀 + urlCode + 短ID + 类型后缀
- 最终格式：`{基础URL}/{前缀}/{urlCode}-{短ID}&type=info`

<!-- 技术细节：
- 基础URL: jobPublishUrl配置项 (默认: https://recruit-dev.item.pub)
- 前缀: jobPublishUrlPrefix配置项 (默认: candidate)  
- 类型后缀: jobPublishInfoType配置项 (默认: &type=info)
- 短ID生成: ShortIdGenerator.generateShortId方法
- 字符串拼接: CommonUtils.joinInclinedRod和CommonUtils.join方法
-->

## 异常处理

- **职位不存在**: 抛出JOB_NOT_FOUND业务异常
- **参数无效**: jobId为null或非正数时的验证处理
- **URL编码异常**: urlCode为空时的异常处理
- **短ID生成异常**: 短ID服务异常时的处理

## API示例

### 请求
```
GET /job/share/job-info/12345
```

### 响应
**成功**: 返回分享链接字符串
```
"https://recruit-dev.item.pub/candidate/abc-company/software-engineer-xyz123&type=info"
```

**失败**: 返回错误信息
```json
{
  "code": "JOB_NOT_FOUND",
  "message": "Job not found"
}
```


<!-- 实现参考：
- 查询方法: JobService.getJobsByIds(Long id)
- 异常定义: JobResponseCode.JOB_NOT_FOUND  
- 链接生成: JobDomainServiceImpl.arySharePost方法
- 短ID服务: ShortIdGenerator接口
- 工具方法: CommonUtils相关方法
-->
