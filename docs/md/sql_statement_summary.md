# r_job_category INSERT语句说明

## 概述
基于RP-245任务需求，为前端独有和匹配度不足的categories生成INSERT语句，手动指定ID从51开始。

## SQL语句特点
- **手动指定ID**：从51开始，避免AUTO_INCREMENT
- **无重复插入**：确保不与现有50个categories冲突
- **覆盖完整**：包含所有12个需要补充的categories

## 插入的Categories详细列表

### 完全独有的Categories (ID 51-55)
| ID | Category Name | 说明 |
|----|---------------|------|
| 51 | AI Automation | 人工智能自动化，前端独有概念 |
| 52 | Content Creation | 内容创作，前端独有概念 |
| 53 | Human Resources | 人力资源，前端独有概念 |
| 54 | Freelance and Contract Work | 自由职业和合同工作，前端独有概念 |
| 55 | Engineering | 工程类别，后端无直接对应 |

### 匹配度不足需要补充的Categories (ID 56-62)
| ID | Category Name | 匹配情况 | 说明 |
|----|---------------|----------|------|
| 56 | Business and Finance | 70%匹配 | 比"Accounting and Financial Services"更宽泛 |
| 57 | Marketing and Sales | 80%匹配 | 比"Advertising and Marketing"包含销售 |
| 58 | Creative Arts and Design | 30%匹配 | 整合了多个创意设计概念 |
| 59 | Retail and Customer Service | 70%匹配 | 整合零售和客服概念 |
| 60 | Construction and Trades | 80%匹配 | 比"Construction and Contracting"包含技工 |
| 61 | Scientific Research | 80%匹配 | 比"Research and Development"更专注科学 |
| 62 | Community and Social Services | 80%匹配 | 整合社区和社会服务概念 |

## 影响
- **新增记录数**：12条
- **ID范围**：51-62
- **表结构**：保持不变，只是数据插入
- **前端兼容**：完全支持前端22个categories的需求
