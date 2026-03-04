# 单元测试中处理 UserContextUtil.getCurrentUserRecruitNeedLogin() 指南

## 问题背景

在单元测试中，当业务代码调用 `UserContextUtil.getCurrentUserRecruitNeedLogin()` 时，由于没有真实的HTTP请求上下文，会导致返回null或抛出异常。本指南提供了几种解决方案。

## 解决方案概览

### 方案1：使用 @BeforeEach/@AfterEach 设置用户上下文 ⭐ **推荐**

**适用场景**：大多数单元测试场景，简单易用
**优点**：代码简洁，易于理解和维护
**缺点**：所有测试方法使用相同的用户上下文

```java
@BeforeEach
void setUp() {
    IamUserContextDTO mockUser = createMockRecruiterUser();
    UserContextUtil.setCurrentUser(mockUser);
}

@AfterEach
void tearDown() {
    UserContextUtil.clear();
}
```

### 方案2：使用 MockedStatic 模拟静态方法

**适用场景**：需要精确控制不同测试场景的用户上下文
**优点**：灵活性高，可以为每个测试方法设置不同的用户上下文
**缺点**：代码相对复杂，需要使用Mockito

```java
try (MockedStatic<UserContextUtil> mockedUserContextUtil = Mockito.mockStatic(UserContextUtil.class)) {
    mockedUserContextUtil.when(UserContextUtil::getCurrentUserRecruitNeedLogin)
            .thenReturn(mockUser);
    // 执行测试逻辑
}
```

### 方案3：创建测试基类

**适用场景**：多个测试类都需要用户上下文模拟
**优点**：代码复用性高，统一管理用户上下文
**缺点**：需要继承基类，可能影响测试类的继承结构

```java
class MyServiceTest extends BaseServiceTestWithUserContext {
    // 用户上下文已经在基类中自动设置
}
```

## 详细实现示例

### 1. 基础用户上下文设置

```java
/**
 * 创建模拟的招聘者用户数据
 */
private IamUserContextDTO createMockRecruiterUser() {
    IamUserContextDTO user = new IamUserContextDTO();
    user.setId("12345");
    user.setAccountId("account123");
    user.setCompanyCode("TEST_COMPANY_001");
    user.setEmail("test.recruiter@example.com");
    user.setUserName("Test Recruiter");
    user.setFirstName("Test");
    user.setLastName("Recruiter");
    user.setUserStatus("ACTIVE");
    user.setUserType(1); // 招聘者类型
    user.setPrimaryUser(true); // 主账号
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    user.setOrigin("TEST");
    
    return user;
}
```

### 2. 方案1实现：@BeforeEach/@AfterEach

```java
@SpringBootTest
class JobServiceTest {
    
    @Resource
    private JobDomainService jobDomainService;

    @BeforeEach
    void setUp() {
        IamUserContextDTO mockUser = createMockRecruiterUser();
        UserContextUtil.setCurrentUser(mockUser);
        log.info("测试用户上下文已设置: companyCode={}", mockUser.getCompanyCode());
    }

    @AfterEach
    void tearDown() {
        UserContextUtil.clear();
        log.info("测试用户上下文已清理");
    }

    @Test
    void testPublishJob() {
        // 直接调用业务方法，用户上下文已经设置好了
        JobCreateDTO dto = createTestJobCreateDTO("Test Job");
        Long jobId = jobDomainService.publishJob(dto);
        assertNotNull(jobId);
    }
}
```

### 3. 方案2实现：MockedStatic

```java
@SpringBootTest
class JobServiceMockTest {
    
    @Resource
    private JobDomainService jobDomainService;

    @Test
    void testPublishJobWithDifferentUsers() {
        // 测试公司1
        IamUserContextDTO company1User = createMockRecruiterUser("COMPANY_001");
        
        try (MockedStatic<UserContextUtil> mockedUtil = Mockito.mockStatic(UserContextUtil.class)) {
            mockedUtil.when(UserContextUtil::getCurrentUserRecruitNeedLogin)
                    .thenReturn(company1User);
            
            JobCreateDTO dto1 = createTestJobCreateDTO("Same Title");
            Long jobId1 = jobDomainService.publishJob(dto1);
            assertNotNull(jobId1);
            
            // 测试公司2
            IamUserContextDTO company2User = createMockRecruiterUser("COMPANY_002");
            mockedUtil.when(UserContextUtil::getCurrentUserRecruitNeedLogin)
                    .thenReturn(company2User);
            
            JobCreateDTO dto2 = createTestJobCreateDTO("Same Title");
            Long jobId2 = jobDomainService.publishJob(dto2);
            assertNotNull(jobId2);
            assertNotEquals(jobId1, jobId2);
        }
    }
}
```

### 4. 方案3实现：测试基类

```java
// 基类
@SpringBootTest
public abstract class BaseServiceTestWithUserContext {
    
    protected static final String DEFAULT_COMPANY_CODE = "TEST_COMPANY_001";

    @BeforeEach
    void setUpUserContext() {
        IamUserContextDTO mockUser = createDefaultMockUser();
        UserContextUtil.setCurrentUser(mockUser);
    }

    @AfterEach
    void tearDownUserContext() {
        UserContextUtil.clear();
    }

    protected IamUserContextDTO createDefaultMockUser() {
        return createMockRecruiterUser(DEFAULT_COMPANY_CODE, "12345");
    }

    protected void switchToRecruiter(String companyCode, String userId) {
        IamUserContextDTO recruiter = createMockRecruiterUser(companyCode, userId);
        UserContextUtil.setCurrentUser(recruiter);
    }
}

// 测试类
class JobServiceExtendedTest extends BaseServiceTestWithUserContext {
    
    @Resource
    private JobDomainService jobDomainService;

    @Test
    void testPublishJob() {
        // 用户上下文已经在基类中设置好了
        JobCreateDTO dto = createTestJobCreateDTO("Test Job");
        Long jobId = jobDomainService.publishJob(dto);
        assertNotNull(jobId);
    }

    @Test
    void testMultiCompanyScenario() {
        // 使用默认用户发布第一个job
        Long jobId1 = jobDomainService.publishJob(createTestJobCreateDTO("Same Title"));
        
        // 切换到另一个公司
        switchToRecruiter("COMPANY_002", "67890");
        
        // 发布相同title的job
        Long jobId2 = jobDomainService.publishJob(createTestJobCreateDTO("Same Title"));
        
        assertNotEquals(jobId1, jobId2);
    }
}
```

## 不同用户类型的模拟

### 招聘者用户（主账号）
```java
private IamUserContextDTO createMockRecruiterUser(String companyCode) {
    IamUserContextDTO user = new IamUserContextDTO();
    user.setId("12345");
    user.setCompanyCode(companyCode);
    user.setUserType(1); // 招聘者类型
    user.setPrimaryUser(true); // 主账号
    // ... 其他字段设置
    return user;
}
```

### 应聘者用户
```java
private IamUserContextDTO createMockCandidateUser(String userId) {
    IamUserContextDTO user = new IamUserContextDTO();
    user.setId(userId);
    user.setUserType(2); // 应聘者类型
    user.setPrimaryUser(false); // 非主账号
    user.setCandidateOneselfId(Long.parseLong(userId));
    // ... 其他字段设置
    return user;
}
```

## 最佳实践建议

### 1. 选择合适的方案
- **简单场景**：使用方案1（@BeforeEach/@AfterEach）
- **复杂场景**：使用方案2（MockedStatic）
- **多测试类**：使用方案3（测试基类）

### 2. 必须清理用户上下文
```java
@AfterEach
void tearDown() {
    UserContextUtil.clear(); // 必须调用，避免影响其他测试
}
```

### 3. 设置完整的用户信息
确保设置所有业务逻辑需要的字段：
- `id`：用户ID
- `companyCode`：公司代码（招聘者必需）
- `userType`：用户类型（1=招聘者，2=应聘者）
- `primaryUser`：是否主账号
- `candidateOneselfId`：应聘者ID（应聘者必需）

### 4. 日志记录
```java
@BeforeEach
void setUp() {
    IamUserContextDTO mockUser = createMockRecruiterUser();
    UserContextUtil.setCurrentUser(mockUser);
    log.info("测试用户上下文已设置: companyCode={}, userId={}", 
            mockUser.getCompanyCode(), mockUser.getId());
}
```

### 5. 异常场景测试
```java
@Test
void testWithoutUserContext() {
    UserContextUtil.clear(); // 清除用户上下文
    
    assertThrows(BusinessException.class, () -> {
        jobDomainService.publishJob(createTestJobCreateDTO("Test"));
    });
}
```

## 常见问题解决

### Q1: 测试时抛出 "Invalid authentication" 异常
**原因**：用户上下文为null或用户类型不正确
**解决**：确保在测试前设置了正确的用户上下文

### Q2: 测试时抛出 "Not authenticated" 异常
**原因**：用户类型不匹配（如应聘者调用招聘者接口）
**解决**：检查用户的 `userType` 和 `primaryUser` 字段设置

### Q3: 不同测试方法之间相互影响
**原因**：没有在测试后清理用户上下文
**解决**：确保在 `@AfterEach` 中调用 `UserContextUtil.clear()`

### Q4: MockedStatic 使用后其他测试失败
**原因**：MockedStatic 没有正确关闭
**解决**：使用 try-with-resources 语法确保自动关闭

## 示例文件位置

本项目提供了以下示例文件：
- `BaseServiceTestWithUserContext.java` - 方案3基类
- `JobDomainServiceExtendedTest.java` - 方案3使用示例

## 总结

选择合适的方案来处理单元测试中的用户上下文：
3. **多测试类**：使用方案3，统一管理

记住始终在测试后清理用户上下文，避免测试之间的相互影响。
