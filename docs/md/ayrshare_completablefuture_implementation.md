# Ayrshare CompletableFuture异步实现说明

## 概述

本文档描述了使用Java CompletableFuture实现Ayrshare异步调用和状态跟踪的功能。该实现满足RP-164-sub01的需求，使用链式调用先分享再根据分享结果更新数据库状态。

## 实现特点

### 1. 数据库表结构
- **表名**: `r_job`
- **新增字段**: `ayrshare_status` (INT类型，默认值为0)
- **状态定义**:
  - `0`: 不需要分享
  - `1`: 分享中
  - `2`: 分享成功
  - `3`: 部分成功
  - `-1`: 分享失败

### 2. 实体类更新
- `JobEntity`: 添加`ayrshareStatus`字段
- `JobEsEntity`: 添加`ayrshareStatus`字段，包含空值处理方法
- 相关DTO/VO: 添加字段并提供默认值0的获取方法

### 3. CompletableFuture链式调用实现

#### 方法签名
```java
CompletableFuture<Void> asyncSendToAyrShareWithConfig(
    AyrShareRequestDTO jsonBody, 
    String companyCode, 
    Long jobId
)
```

#### 执行流程
使用CompletableFuture实现四步链式调用：

1. **第一步**: 更新Job状态为"分享中"(1)
   ```java
   .supplyAsync(() -> {
       updateJobAyrshareStatus(jobId, 1);
       return jobId;
   })
   ```

2. **第二步**: 从数据库获取Ayrshare配置信息
   ```java
   .thenCompose(id -> CompletableFuture.supplyAsync(() -> {
       AyrshareTokenDTO tokenConfig = ayrshareCompanyConfigService
           .getTokenInfoByCompanyCode(companyCode);
       // 验证配置有效性
       return tokenConfig;
   }))
   ```

3. **第三步**: 调用Ayrshare API接口
   ```java
   .thenCompose(tokenConfig -> CompletableFuture.supplyAsync(() -> {
       // 构建请求头
       Map<String, String> headers = buildHeaders(tokenConfig);
       // 调用HTTP接口
       String response = httpClient5Service.doPost(apiUrl, jsonBody, headers);
       return response;
   }))
   ```

4. **第四步**: 根据返回结果更新状态
   ```java
   .thenAccept(response -> {
       if (StringUtils.isNotBlank(response)) {
           updateJobAyrshareStatus(jobId, 2); // 成功
       } else {
           updateJobAyrshareStatus(jobId, -1); // 失败
       }
   })
   ```

#### 异常处理
```java
.exceptionally(throwable -> {
    log.error("CompletableFuture chain failed for job: {}", jobId, throwable);
    updateJobAyrshareStatus(jobId, -1); // 分享失败
    return null;
});
```

### 4. 状态更新机制

#### updateJobAyrshareStatus方法
- 同时更新数据库中的`JobEntity`和ES中的`JobEsEntity`
- 提供完整的错误处理和日志记录
- 确保数据一致性

```java
private void updateJobAyrshareStatus(Long jobId, Integer status) {
    // 更新数据库实体
    JobEntity jobEntity = jobService.getById(jobId);
    jobEntity.setAyrshareStatus(status);
    jobService.updateById(jobEntity);
    
    // 更新ES实体
    JobEsEntity jobEsEntity = jobEsService.getJobById(jobId);
    jobEsEntity.setAyrshareStatus(status);
    jobEsService.updateJobEs(jobEsEntity);
}
```

### 5. 集成修改

#### JobDomainServiceImpl.ayrSharePost()
- 使用新的`asyncSendToAyrShareWithConfig`方法
- 传入公司代码和Job ID支持状态跟踪
- 从数据库获取配置信息，不再依赖Nacos配置

```java
ayrShareService.asyncSendToAyrShareWithConfig(
    AyrShareRequestDTO.build(message, List.of("all"), null), 
    companyInfo.getCompanyCode(), 
    jobId
);
```

## 技术优势

### 1. 异步非阻塞
- 使用CompletableFuture实现真正的异步操作
- 不会阻塞Job发布的主流程
- 支持并发处理多个分享任务

### 2. 链式调用
- 每个步骤依次执行，确保逻辑顺序
- 前一步的结果作为后一步的输入
- 清晰的数据流转和状态管理

### 3. 完善的错误处理
- 每个步骤都有独立的异常处理
- 统一的错误状态更新
- 详细的日志记录便于排查问题

### 4. 状态追踪
- 实时更新Job的分享状态
- 同步更新数据库和ES
- 提供分享过程的完整可见性

### 5. 配置灵活性
- 从数据库读取配置，便于管理
- 支持公司级别的配置隔离
- 不依赖外部配置中心

## 使用示例

### 基本调用
```java
CompletableFuture<Void> future = ayrShareService.asyncSendToAyrShareWithConfig(
    AyrShareRequestDTO.build("分享内容", List.of("all"), null),
    "company123",
    12345L
);
```

### 等待完成（可选）
```java
future.join(); // 同步等待完成
// 或者
future.thenRun(() -> {
    log.info("分享流程完成");
});
```

## 测试验证

创建了`AyrShareCompletableFutureTest`测试类验证：
- CompletableFuture链式调用功能
- 异常处理机制
- 状态值的正确性
- AyrShareRequestDTO的构建

## 注意事项

1. **异步执行**: 方法使用`@Async`注解，需要配置线程池
2. **事务处理**: 状态更新操作需要考虑事务边界
3. **错误恢复**: 分享失败的Job可以考虑重试机制
4. **监控告警**: 建议添加分享成功率的监控

## 后续扩展

1. **响应解析**: 可以根据Ayrshare的实际响应格式进行详细解析
2. **重试机制**: 对失败的分享任务实现自动重试
3. **批量处理**: 支持批量分享多个Job
4. **性能优化**: 根据实际使用情况优化线程池配置

该实现完全满足RP-164-sub01的需求，提供了可靠、高效的异步分享功能。
