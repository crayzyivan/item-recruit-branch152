## com.item.service.impl.JobCategoryServiceImp.getAllJobCategories  com.item.service.impl.JobModeServiceImp.getAll  com.item.service.JobTypeService.getAllJobTypes 影响
### 接口级别
#### /job/allJobCategories   com.item.controller.JobController.getAllJobCategories  获取所有category列表 首页category列表 岗位发布编辑 下拉
#### /job/pdf-generate-job-description 上传pdf文件生成工作描述
#### /job/jobList 应聘者 岗位列表
#### /job/recruiter/job-list 招聘者 岗位列表
#### /job/jobListByCompany/{companyCode} 应聘者根据分享连接 进入查看jobList 
#### /job/detail/{jobId} 应聘者 职位详情 根据id查询
#### /job/detail-all/{jobId} 招聘者 职位详情 根据id查询
#### /job/detail/info/{jobCode} 应聘者|招聘者 职位详情 根据分享连接查询  
#### /job/option/detail-all/{jobId} 招聘者 职位详情 根据id查询 下拉中选择后调用获取详情
#### /job/history/detail/{jobId} 根据jobId获取历史职位详情  接口路劲参数是 通过/job/history/list接口返回的jobId 
#### /job/random-recommendations 随机job
#### /job/all/location-type  com.item.controller.JobController.getAllLocationType  获取所有locationType列表 岗位发布编辑 下拉
#### /job/allJobTypes  com.item.controller.JobController.getAllJobTypes  获取所有JobTypes列表 岗位发布编辑 下拉
### /candidate/details com.item.controller.CandidateController.getCandidateDetails  应聘者简历信息
### /jobFlow/candidate-details   com.item.controller.JobFlowController.getCandidateDetails   招聘者看应聘者记录信息
### /report/applications-candidate-list  com.item.controller.ReportController.getApplicationsCandidateList  招聘者候选人列表
### /jobFlow/candidate-process com.item.controller.JobFlowController.getCandidateProcess 候选人流水线列表
### /jobFlow/application-list com.item.controller.JobFlowController.selectApplicationPageList 招聘者application列表
### /jobFlow/pending-review-list com.item.controller.JobFlowController.selectPendingReviewPageList 招聘者人工审核列表
### /jobFlow/denied-list com.item.controller.JobFlowController.selectDeniedPageList 招聘者拒绝列表
### /jobFlow/ready-list com.item.controller.JobFlowController.selectreadyPageList 招聘者就绪列表

### 功能
#### 在招聘者的jobs中点击一个job进入页面 显示


## 字典类影响
### 接口级 com.item.controller.DictionaryController
#### /api/dictionary/{id}
#### /api/dictionary/type/{type}
#### /api/dictionary/types
#### /api/dictionary/all
#### /jobFlow/candidate-process 候选人应聘流程时间线
#### /job/salary-types 薪资类型 时薪 月薪等
#### /job/currency-types  货币类型 美元 英镑等
#### 应聘者申请岗位时候 简历匹配时需要 薪资类型和货币类型 
#### /jobFlow/candidate-details 候选人详情
#### /jobFlow/shared-details/{encryptedId}  候選人詳情
#### /candidate/details 详情
#### /job/pdf-generate-job-description 上传pdf文件生成工作描述
#### /jobFlow/candidate-process 候选人应聘流程时间线
#### /jobFlow/shared-details/{encryptedId} 、 /jobFlow/candidate-details  候选人详情

### 方法级
#### com.item.service.impl.AiScreeningSyncServiceImpl.syncScreeningResult 定时任务使用
#### com.item.service.impl.CandidateJobDomainServiceImpl.resumeAIMatch(com.item.entity.CandidateJobEntity, java.lang.Long, java.lang.String)  com.item.service.impl.AIServiceImpl.aiMatch 应聘者申请岗位  定时任务简历匹配
#### 定时任务 简历匹配 resumeAIMatchTaskHandler
#### com.item.service.impl.XmlFeedServiceImpl.generateJobsXML liyunlong
#### /candidate/resume-parsing 简历解析
#### /jobFlow/screen-result-by-id/{id} 根据关联id查询筛选结果
#### /report/dashboard-candidate-list dashboard页上候选人列表
#### /report/applications-candidate-list dashboard页上候选人列表


## 地点影响
###  同字典和枚举影响
#### /location 开头的所有接口


## 代码中常量文件夹中的常量和枚举 初步检查不需要修改


