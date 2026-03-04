package com.item.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.ApplicationsEntity;
import com.item.vo.ApplicationsSyncVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 关联表服务接口
 * 
 * 提供关联表相关的业务操作，使用MyBatis Plus的QueryWrapper实现复杂查询。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-11
 */
public interface ApplicationsService extends IService<ApplicationsEntity> {

    /**
     * 根据更新时间字段的开始时间查询
     * @param updateStartDate
     * @return
     */
    List<ApplicationsEntity> listByUpdateStartDate(LocalDateTime  updateStartDate);

    /**
     * 根据查询条件查询
     * @param syncVo
     * @return
     */
    List<ApplicationsEntity> listBySyncVo(ApplicationsSyncVO syncVo);
}
