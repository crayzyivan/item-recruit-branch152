package com.item.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.entity.AiVettedResultEntity;
import com.item.entity.AiVettedResultSkillEntity;
import com.item.framework.constant.CommonConstants;
import com.item.mapper.AiVettedResultMapper;
import com.item.mapper.AiVettedResultSkillMapper;
import com.item.task.core.handler.annotation.ScheduleTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RefreshVettedResultTask {

    @Autowired
    private AiVettedResultMapper aiVettedResultMapper;

    @Autowired
    private AiVettedResultSkillMapper aiVettedResultSkillMapper;

    @ScheduleTask("refreshVettedScore")
    public void refreshVettedScore() {
        log.info("Start refreshing AI vetted result scores...");

        // 1. 查询总记录数
        Long totalCount = aiVettedResultMapper.selectCount(null);
        log.info("Total records in r_ai_vetted_result table: {}", totalCount);

        if (totalCount == 0) {
            log.info("No records to process");
            return;
        }

        int pageSize = 20;
        int pageNum = 1;
        int totalProcessed = 0;

        while (true) {
            // 2. 分页查询 AiVettedResult
            Page<AiVettedResultEntity> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<AiVettedResultEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(AiVettedResultEntity::getId);
            Page<AiVettedResultEntity> resultPage = aiVettedResultMapper.selectPage(page, wrapper);

            List<AiVettedResultEntity> records = resultPage.getRecords();
            if (records.isEmpty()) {
                break;
            }

            // 3. 提取所有 vettedResultId
            List<Long> vettedResultIds = records.stream()
                    .map(AiVettedResultEntity::getId)
                    .collect(Collectors.toList());

            // 4. 批量查询技能数据
            LambdaQueryWrapper<AiVettedResultSkillEntity> skillWrapper = new LambdaQueryWrapper<>();
            skillWrapper.in(AiVettedResultSkillEntity::getVettedResultId, vettedResultIds);
            List<AiVettedResultSkillEntity> skillList = aiVettedResultSkillMapper.selectList(skillWrapper);

            // 5. 按 vettedResultId 分组
            Map<Long, List<AiVettedResultSkillEntity>> skillMap = skillList.stream()
                    .collect(Collectors.groupingBy(AiVettedResultSkillEntity::getVettedResultId));

            // 6. 处理每条记录
            for (AiVettedResultEntity entity : records) {
                List<AiVettedResultSkillEntity> skills = skillMap.get(entity.getId());
                if (skills == null || skills.isEmpty()) {
                    entity.setTechnicalSkillScore(null);
                    entity.setSoftSkillScore(null);
                    entity.setOverallScore(null);
                    continue;
                }

                // 计算 softSkillScore
                Integer softSkillScore = skills.stream()
                        .filter(s -> CommonConstants.AI_VETTED_SOFT_SKILL_NAME.equals(s.getSkillName()))
                        .findFirst()
                        .map(AiVettedResultSkillEntity::getSkillScore)
                        .orElse(0);

                // 计算 technicalSkillScore
                List<AiVettedResultSkillEntity> technicalSkills = skills.stream()
                        .filter(s -> !CommonConstants.AI_VETTED_SOFT_SKILL_NAME.equals(s.getSkillName()))
                        .toList();

                int technicalSkillScore = 0;
                if (!technicalSkills.isEmpty()) {
                    int sum = technicalSkills.stream()
                            .mapToInt(AiVettedResultSkillEntity::getSkillScore)
                            .sum();
                    technicalSkillScore = Math.round((float) sum / technicalSkills.size());
                }

                // 设置分数
                entity.setSoftSkillScore(softSkillScore);
                entity.setTechnicalSkillScore(technicalSkillScore);
                entity.setOverallScore(null);
            }

            // 7. 批量更新
            records.forEach(aiVettedResultMapper::updateById);

            totalProcessed += records.size();
            double progress = (totalProcessed * 100.0) / totalCount;
            log.info("Processed {}/{} records, progress: {}, current page: {}",
                    totalProcessed, totalCount, progress, pageNum);

            pageNum++;
        }

        // 8. 最终对比验证
        if (totalProcessed == totalCount) {
            log.info("Refresh completed! Total processed {} records, matches total count", totalProcessed);
        } else {
            log.warn("Refresh completed! Total processed {} records, but total count is {}, there is a difference",
                    totalProcessed, totalCount);
        }
    }

}
