package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.PlaceEntity;

import java.util.List;

/**
 * Place Service
 *
 * @author system
 */
public interface PlaceService extends IService<PlaceEntity> {

    /**
     * 根据 placeId 查询地点信息
     *
     * @param placeId Google Map Place ID
     * @return 地点信息列表
     */
    List<PlaceEntity> listByPlaceId(String placeId);
    
    /**
     * 根据 placeId 列表批量查询地点信息
     * 返回当前用户语言对应的地点信息
     *
     * @param placeIds Google Map Place ID 列表
     * @return 地点信息列表
     */
    List<PlaceEntity> listByPlaceIds(List<String> placeIds);
    
    /**
     * 异步创建 Place 信息
     * 根据 placeId 从 Google Map API 获取多语言地点信息并保存到数据库
     * 
     * @param placeId Google Map Place ID
     */
    void asyncCreatePlaceInfo(String placeId);
    
    /**
     * 从 Google Map API 获取指定语言的地点详情
     * 
     * @param placeId Google Map Place ID
     * @param language 语言代码（zh、en、ja、es）
     * @return PlaceEntity 对象，如果获取失败返回 null
     */
    PlaceEntity fetchPlaceDetailFromGoogleMap(String placeId, String language);
}
