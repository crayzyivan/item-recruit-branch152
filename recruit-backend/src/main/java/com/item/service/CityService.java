package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.CityEntity;

import java.util.List;
import java.util.Locale;

/**
 * @author : lh
 */
public interface CityService extends IService<CityEntity> {

    List<CityEntity> searchCitiesByLanguage(String keyword, int maxSize, Locale locale);
}
