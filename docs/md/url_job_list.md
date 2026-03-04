# 我有一个需求 需要根据主账号的customerId生成一个链接返回

## controller写在jobController类中 
 - 入参是masterAccountId 路径参数 
 - 返回是一个String

## service写在JobDomainService接口类中
## 生成链接的规则是 将companyName删除特殊字符处理并拼接上
  参考com.item.service.impl.JobDomainServiceImpl#publishJob方法中的 **自动发布到其他网站** 中publishURLIdStr的生成规则
