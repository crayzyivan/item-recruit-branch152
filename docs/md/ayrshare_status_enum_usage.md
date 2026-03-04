# Ayrshare状态枚举使用说明

## 概述

`AyrshareStatus` 枚举类用于规范化管理Job的Ayrshare分享状态，提供类型安全的状态值管理和丰富的状态判断方法。

## 枚举值定义

| 枚举值 | 代码值 | 描述 | 说明 |
|-------|--------|------|------|
| `NO_SHARE` | 0 | No Share | 不需要分享 |
| `SHARING` | 1 | Sharing | 分享中 |
| `SHARE_SUCCESS` | 2 | Share Success | 分享成功 |
| `PARTIAL_SUCCESS` | 3 | Partial Success | 部分成功 |
| `SHARE_FAILED` | -1 | Share Failed | 分享失败 |

## 基本使用方法

### 1. 获取状态值

```java
// 获取状态代码
Integer code = AyrshareStatus.SHARING.getCode(); // 返回 1

// 获取状态描述
String description = AyrshareStatus.SHARING.getDescription(); // 返回 "Sharing"
```

### 2. 根据代码值获取枚举

```java
// 根据代码值获取枚举实例
AyrshareStatus status = AyrshareStatus.getByCode(1); // 返回 SHARING
AyrshareStatus defaultStatus = AyrshareStatus.getByCode(null); // 返回 NO_SHARE
AyrshareStatus invalidStatus = AyrshareStatus.getByCode(999); // 返回 null
```

### 3. 状态判断方法

```java
AyrshareStatus status = AyrshareStatus.SHARE_SUCCESS;

// 判断是否为成功状态（包括完全成功和部分成功）
boolean isSuccess = status.isSuccess(); // true

// 判断是否为失败状态
boolean isFailed = status.isFailed(); // false

// 判断是否为进行中状态
boolean isInProgress = status.isInProgress(); // false

// 判断是否为终止状态（成功或失败，非进行中）
boolean isTerminal = status.isTerminal(); // true
```

### 4. 获取所有枚举值

```java
List<AyrshareStatus> allStatuses = AyrshareStatus.getAll();
// 返回包含所有5个枚举值的不可变列表
```

## 在业务代码中的使用

### 1. 状态更新

```java
// 旧的方式（不推荐）
updateJobAyrshareStatus(jobId, 1); // 魔法数字，不清晰

// 新的方式（推荐）
updateJobAyrshareStatus(jobId, AyrshareStatus.SHARING); // 类型安全，语义清晰
```

### 2. 状态判断

```java
// 获取Job状态
Integer statusCode = jobEntity.getAyrshareStatus();
AyrshareStatus status = AyrshareStatus.getByCode(statusCode);

// 业务逻辑判断
if (status != null && status.isTerminal()) {
    // 分享已完成（成功或失败）
    if (status.isSuccess()) {
        log.info("Job {} 分享成功", jobId);
    } else if (status.isFailed()) {
        log.warn("Job {} 分享失败", jobId);
    }
} else if (status != null && status.isInProgress()) {
    // 分享进行中
    log.info("Job {} 正在分享中", jobId);
}
```

### 3. 在CompletableFuture链中使用

```java
return CompletableFuture
    .supplyAsync(() -> {
        updateJobAyrshareStatus(jobId, AyrshareStatus.SHARING);
        return jobId;
    })
    .thenCompose(this::callAyrshareApi)
    .thenAccept(response -> {
        AyrshareStatus finalStatus = response.isSuccess() ? 
            AyrshareStatus.SHARE_SUCCESS : AyrshareStatus.SHARE_FAILED;
        updateJobAyrshareStatus(jobId, finalStatus);
    })
    .exceptionally(throwable -> {
        updateJobAyrshareStatus(jobId, AyrshareStatus.SHARE_FAILED);
        return null;
    });
```

## 数据库和ES实体使用

### 1. 实体类中的默认值处理

```java
// JobEntity, JobEsEntity, JobDto, JobVO等类中
public Integer getAyrshareStatus() {
    return ayrshareStatus != null ? ayrshareStatus : AyrshareStatus.NO_SHARE.getCode();
}
```

### 2. 状态检查示例

```java
// 检查Job是否可以重新分享
public boolean canRetryShare(JobEntity jobEntity) {
    AyrshareStatus status = AyrshareStatus.getByCode(jobEntity.getAyrshareStatus());
    return status != null && (status == AyrshareStatus.NO_SHARE || status.isFailed());
}
```

## 最佳实践

1. **使用枚举值而非魔法数字**：总是使用 `AyrshareStatus.SHARING` 而不是数字 `1`
2. **空值处理**：使用 `getByCode()` 方法时要处理返回的null值
3. **状态转换验证**：在状态转换前使用相应的判断方法验证当前状态
4. **日志记录**：在日志中使用 `getDescription()` 方法提供可读的状态描述

## 状态转换规则

```
NO_SHARE(0) → SHARING(1) → SHARE_SUCCESS(2) | SHARE_FAILED(-1)
                        → PARTIAL_SUCCESS(3)

// 失败状态可以重新尝试
SHARE_FAILED(-1) → SHARING(1)
```

## 注意事项

1. 枚举值的代码值一旦定义不应随意修改，因为数据库中存储的是代码值
2. 新增枚举值时要考虑向后兼容性
3. 在单元测试中要覆盖所有枚举值和状态判断方法
4. 使用 `getByCode()` 方法时要注意处理null返回值
