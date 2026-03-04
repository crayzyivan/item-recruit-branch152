package com.item.service.impl;

import com.item.entity.XmlFeedUpdateLogEntity;
import com.item.mapper.XmlFeedUpdateLogMapper;
import com.item.service.XmlFeedUpdateLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * XML Feed更新日志服务实现类
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XmlFeedUpdateLogServiceImpl implements XmlFeedUpdateLogService {

    private final XmlFeedUpdateLogMapper xmlFeedUpdateLogMapper;

    /**
     * 保存更新日志
     *
     * @param logEntity 更新日志实体
     * @return 是否保存成功
     */
    @Override
    public boolean save(XmlFeedUpdateLogEntity logEntity) {
        return xmlFeedUpdateLogMapper.insert(logEntity) > 0;
    }

    /**
     * 根据ID更新日志
     *
     * @param logEntity 更新日志实体
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(XmlFeedUpdateLogEntity logEntity) {
        return xmlFeedUpdateLogMapper.updateById(logEntity) > 0;
    }

    /**
     * 根据ID获取更新日志
     *
     * @param id 日志ID
     * @return 更新日志实体
     */
    @Override
    public XmlFeedUpdateLogEntity getById(Long id) {
        return xmlFeedUpdateLogMapper.selectById(id);
    }
}
