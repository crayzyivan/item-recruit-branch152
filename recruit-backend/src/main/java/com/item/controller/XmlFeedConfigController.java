package com.item.controller;


import com.item.dto.xmlfeed.XmlFeedConfigCreateDTO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.service.XmlFeedConfigService;

import com.item.vo.feed.XmlFeedConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * XML Feed配置管理接口
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Slf4j
@RestController
@RequestMapping("/xml-feed")
@RequiredArgsConstructor
public class XmlFeedConfigController {

    private final XmlFeedConfigService xmlFeedConfigService;

    /**
     * 获取XML Feed配置信息
     * 
     * 根据当前登录代理人自动获取对应的company_code，
     * 查询指定平台的XML Feed配置详情。
     *
     * @param platformType 平台类型：1-LinkedIn, 2-Indeed
     * @return XML Feed配置信息，包含Feed URL、更新间隔、Guide URL等
     * @throws com.item.framework.error.BusinessException 当平台类型无效或配置不存在时抛出
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/config/{platformType}")
    public XmlFeedConfigVO getConfig(@PathVariable Integer platformType) {
        return xmlFeedConfigService.getConfigByPlatformType(platformType);
    }

    /**
     * 创建或更新XML Feed配置
     * 
     * 为当前登录用户的公司创建或更新XML Feed配置。
     * 系统会自动生成Feed URL和Guide URL，并计算下次更新时间。
     *
     * @param createDTO 配置创建参数，包含平台类型、邮箱（Indeed必填）、更新间隔等
     * @return 创建或更新后的XML Feed配置信息
     * @throws com.item.framework.error.BusinessException 当平台类型无效、Indeed平台缺少邮箱、更新间隔超出范围时抛出
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/config")
    public XmlFeedConfigVO createOrUpdateConfig(@RequestBody @Validated XmlFeedConfigCreateDTO createDTO) {
        return xmlFeedConfigService.createOrUpdateConfig(createDTO);
    }

    /**
     * 手动触发XML Feed更新
     * 
     * 立即触发指定平台的XML文件生成和上传。
     * 受2分钟冷却时间限制，防止频繁更新。
     *
     * @param platformType 平台类型：1-LinkedIn, 2-Indeed
     * @return 更新后的配置信息，包含最新的更新时间和下次更新时间
     * @throws com.item.framework.error.BusinessException 当平台配置不存在、处于冷却时间内或更新失败时抛出
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/manual-update/{platformType}")
    public XmlFeedConfigVO manualUpdate(@PathVariable Integer platformType) {
        return xmlFeedConfigService.manualUpdate(platformType);
    }
}
