package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.dto.googlemap.GoogleMapPlaceDetailsResponseDTO;
import com.item.entity.PlaceEntity;
import com.item.framework.config.GoogleMapConfig;
import com.item.framework.net.HttpClient5Service;
import com.item.util.JsonUtils;
import com.item.util.UserContextUtil;
import com.item.mapper.PlaceMapper;
import com.item.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Place Service Implementation
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceServiceImpl extends ServiceImpl<PlaceMapper, PlaceEntity> implements PlaceService {

    /**
     * 系统支持的语言列表
     * en: 英语, zh: 中文, ja: 日语, es: 西班牙语
     */
    private static final List<String> SUPPORTED_LANGUAGES = List.of("en", "zh", "ja", "es");
    
    private final GoogleMapConfig googleMapConfig;
    private final HttpClient5Service httpClient5Service;

    @Override
    public List<PlaceEntity> listByPlaceId(String placeId) {
        if (StringUtils.isBlank(placeId)) {
            return List.of();
        }
        
        LambdaQueryWrapper<PlaceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlaceEntity::getPlaceId, placeId);
        
        return list(queryWrapper);
    }
    
    @Override
    public List<PlaceEntity> listByPlaceIds(List<String> placeIds) {
        if (CollectionUtils.isEmpty(placeIds)) {
            return List.of();
        }
        
        // 获取当前用户语言
        Locale locale = UserContextUtil.getLanguageLocal();
        String language = getLanguageCode(locale);
        
        log.info("listByPlaceIds: placeIds={}, language={}", placeIds, language);
        
        LambdaQueryWrapper<PlaceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PlaceEntity::getPlaceId, placeIds)
                   .eq(PlaceEntity::getLanguage, language);
        
        return list(queryWrapper);
    }
    
    /**
     * 根据 Locale 获取语言代码
     * 
     * @param locale 用户语言环境
     * @return 语言代码（en、zh、ja、es），默认返回 en
     */
    private String getLanguageCode(Locale locale) {
        if (locale == null) {
            return "en";
        }
        
        String language = locale.getLanguage();
        
        // 支持的语言：en、zh、ja、es
        if ("zh".equals(language) || "ja".equals(language) || "es".equals(language)) {
            return language;
        }
        
        // 默认返回英语
        return "en";
    }
    
    @Override
    public void asyncCreatePlaceInfo(String placeId) {
        // 1. 参数校验
        if (StringUtils.isBlank(placeId)) {
            log.warn("asyncCreatePlaceInfo: placeId is blank, skip processing");
            return;
        }
        
        log.info("asyncCreatePlaceInfo: start processing placeId={}", placeId);
        
        // 2. 幂等性检查
        List<PlaceEntity> existingPlaces = listByPlaceId(placeId);
        if (CollectionUtils.isNotEmpty(existingPlaces)) {
            log.info("asyncCreatePlaceInfo: placeId={} already exists with {} languages, skip processing", 
                    placeId, existingPlaces.size());
            return;
        }
        
        // 3. 并行获取多语言数据
        List<CompletableFuture<PlaceEntity>> futures = SUPPORTED_LANGUAGES.stream()
                .map(language -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return fetchPlaceDetailFromGoogleMap(placeId, language);
                    } catch (Exception e) {
                        log.error("asyncCreatePlaceInfo: failed to fetch place detail for placeId={}, language={}", 
                                placeId, language, e);
                        return null;
                    }
                }))
                .toList();
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 4. 过滤有效结果
        List<PlaceEntity> placeEntities = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 5. 批量保存
        if (CollectionUtils.isEmpty(placeEntities)) {
            log.error("asyncCreatePlaceInfo: failed to fetch any place detail for placeId={}", placeId);
            return;
        }
        
        boolean saved = saveBatch(placeEntities);
        if (saved) {
            log.info("asyncCreatePlaceInfo: successfully saved {} place entities for placeId={}", 
                    placeEntities.size(), placeId);
        } else {
            log.error("asyncCreatePlaceInfo: failed to save place entities for placeId={}", placeId);
        }
    }
    
    @Override
    public PlaceEntity fetchPlaceDetailFromGoogleMap(String placeId, String language) {
        log.info("fetchPlaceDetailFromGoogleMap: start fetching placeId={}, language={}", placeId, language);
        
        try {
            // 1. 构建请求 URL
            String url = buildPlaceDetailsUrl(placeId, language);
            log.debug("fetchPlaceDetailFromGoogleMap: request url={}", url);
            
            // 2. 调用 Google Map API
            String responseJson = httpClient5Service.doGet(url, null);
            if (StringUtils.isBlank(responseJson)) {
                log.error("fetchPlaceDetailFromGoogleMap: empty response for placeId={}, language={}", 
                        placeId, language);
                return null;
            }
            
            log.debug("fetchPlaceDetailFromGoogleMap: response={}", responseJson);
            
            // 3. 解析响应
            GoogleMapPlaceDetailsResponseDTO response = JsonUtils.toObject(
                    responseJson, GoogleMapPlaceDetailsResponseDTO.class);
            
            if (response == null) {
                log.error("fetchPlaceDetailFromGoogleMap: failed to parse response for placeId={}, language={}", 
                        placeId, language);
                return null;
            }
            
            // 4. 检查状态
            if (!"OK".equals(response.getStatus())) {
                log.error("fetchPlaceDetailFromGoogleMap: API returned status={} for placeId={}, language={}", 
                        response.getStatus(), placeId, language);
                return null;
            }
            
            if (response.getResult() == null) {
                log.error("fetchPlaceDetailFromGoogleMap: result is null for placeId={}, language={}", 
                        placeId, language);
                return null;
            }
            
            // 5. 提取数据
            String name = response.getResult().getName();
            String formattedAddress = response.getResult().getFormattedAddress();
            
            // 构建 description: name + 空格 + formatted_address
            String description = buildDescription(name, formattedAddress);
            
            String addressComponents = null;
            if (CollectionUtils.isNotEmpty(response.getResult().getAddressComponents())) {
                addressComponents = JsonUtils.toJson(response.getResult().getAddressComponents());
            }
            
            // 6. 构建 PlaceEntity
            PlaceEntity placeEntity = new PlaceEntity();
            placeEntity.setPlaceId(placeId);
            placeEntity.setLanguage(language);
            placeEntity.setDescription(description);
            placeEntity.setAddressComponents(addressComponents);
            
            log.info("fetchPlaceDetailFromGoogleMap: successfully fetched place detail for placeId={}, language={}", 
                    placeId, language);
            
            return placeEntity;
            
        } catch (Exception e) {
            log.error("fetchPlaceDetailFromGoogleMap: exception occurred for placeId={}, language={}", 
                    placeId, language, e);
            return null;
        }
    }
    
    /**
     * 构建 Google Map Place Details API 请求 URL
     *
     * @param placeId Google Map Place ID
     * @param language 语言代码
     * @return 完整的请求 URL
     */
    private String buildPlaceDetailsUrl(String placeId, String language) {
        try {
            String encodedPlaceId = URLEncoder.encode(placeId, StandardCharsets.UTF_8.toString());
            
            return String.format("%s?place_id=%s&fields=address_components,name,formatted_address&language=%s&key=%s",
                    googleMapConfig.getPlaceDetailsUrl(),
                    encodedPlaceId,
                    language,
                    googleMapConfig.getApiKey());
        } catch (UnsupportedEncodingException e) {
            log.error("buildPlaceDetailsUrl: failed to encode placeId={}", placeId, e);
            // 如果编码失败，使用原始值
            return String.format("%s?place_id=%s&fields=address_components,name,formatted_address&language=%s&key=%s",
                    googleMapConfig.getPlaceDetailsUrl(),
                    placeId,
                    language,
                    googleMapConfig.getApiKey());
        }
    }
    
    /**
     * 构建 description 字段
     * 格式：name + 空格 + formatted_address
     *
     * @param name 地点名称
     * @param formattedAddress 格式化地址
     * @return 组合后的 description
     */
    private String buildDescription(String name, String formattedAddress) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(formattedAddress)) {
            return name + " - " + formattedAddress;
        } else if (StringUtils.isNotBlank(name)) {
            return name;
        } else if (StringUtils.isNotBlank(formattedAddress)) {
            return formattedAddress;
        } else {
            return null;
        }
    }
}
