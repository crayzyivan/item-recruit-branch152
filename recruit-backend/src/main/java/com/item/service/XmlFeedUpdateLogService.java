package com.item.service;

import com.item.entity.XmlFeedUpdateLogEntity;

/**
 * XML Feed更新日志服务接口
 *
 * @author liyunlong
 * @since 2025-08-26
 */
public interface XmlFeedUpdateLogService {

    /**
     * 保存更新日志
     *
     * @param logEntity 更新日志实体
     * @return 是否保存成功
     */
    boolean save(XmlFeedUpdateLogEntity logEntity);

    /**
     * 根据ID更新日志
     *
     * @param logEntity 更新日志实体
     * @return 是否更新成功
     */
    boolean updateById(XmlFeedUpdateLogEntity logEntity);

    /**
     * 根据ID获取更新日志
     *
     * @param id 日志ID
     * @return 更新日志实体
     */
    XmlFeedUpdateLogEntity getById(Long id);
}
