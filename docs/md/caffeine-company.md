# Caffeine 本地缓存实现文档

## 概述
按照需求实现了使用 Caffeine 的本地缓存功能，通过创建 LoadingCache Spring Bean 来缓存公司信息，提高 `JobDomainServiceImpl.searchJobsFromEs` 方法的性能。

## 实现内容

###  Caffeine 配置类
创建了 `CaffeineConfig` 配置类 (`recruit-backend/src/main/java/com/item/framework/config/CaffeineConfig.java`)：

#### 主要特性：
- **Bean 名称**: `companyInfoLoadingCache`
- **缓存类型**: `LoadingCache<String, CompanyInfoSimpleDTO>`
- **写入后过期**: 60秒
- **最大限制**: 300个条目
- **移除监听器**: 配置了日志记录功能
- **自动加载**: 缓存失效时自动调用 `CompanyService.getCompanyInfoByCode` 方法重新加载

### JobDomainServiceImpl 修改
修改了 `JobDomainServiceImpl` 类来使用 LoadingCache：


#### searchJobsFromEs 方法增强：
- 在处理搜索结果时，使用 LoadingCache 获取公司信息

### 4. 错误处理
- 对 null 值进行了适当处理
- 添加了异常捕获和日志记录
- 缓存加载失败时返回空的 DTO 对象

### 5. 单元测试
创建了 `CaffeineConfigTest` 测试类，验证：
- LoadingCache 的创建
- 缓存的加载和命中
- null 值处理
- 服务返回 null 的情况