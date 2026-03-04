package com.item.service;

import com.item.dto.xmlfeed.XmlFeedConfigCreateDTO;
import com.item.entity.XmlFeedConfigEntity;
import com.item.vo.feed.XmlFeedConfigVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * XML Feed配置管理服务接口
 *
 * @author liyunlong
 * @since 2025-08-26
 */
public interface XmlFeedConfigService {

    /**
     * 根据平台类型获取配置信息
     * 根据当前登录用户的公司代码自动匹配
     *
     * @param platformType 平台类型
     * @return 配置信息
     */
    XmlFeedConfigVO getConfigByPlatformType(Integer platformType);

    /**
     * 创建或更新XML Feed配置
     *
     * @param createDTO 创建请求DTO
     * @return 配置信息
     */
    XmlFeedConfigVO createOrUpdateConfig(XmlFeedConfigCreateDTO createDTO);

    /**
     * 手动更新XML Feed
     *
     * @param platformType 平台类型：1-LinkedIn, 2-Indeed
     * @return 更新是否成功
     */
    XmlFeedConfigVO manualUpdate(Integer platformType);

    /**
     * 更新job 更新XML Feed
     *
     * @return 更新是否成功
     */
    void jobUpdate(String companyCode,Long userId);

    /**
     * 查询过期的配置列表
     * 用于定时任务查询需要自动更新的配置
     *
     * @param currentTime 当前时间
     * @return 过期配置列表
     */
    List<XmlFeedConfigEntity> findExpiredConfigurations(LocalDateTime currentTime);

    /**
     * 更新配置实体
     * 用于定时任务更新配置时间戳等信息
     *
     * @param configEntity 配置实体
     * @return 是否更新成功
     */
    boolean updateConfigEntity(XmlFeedConfigEntity configEntity);
}
