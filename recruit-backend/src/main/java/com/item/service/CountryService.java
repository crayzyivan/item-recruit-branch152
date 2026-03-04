package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.CountryEntity;

import java.util.List;
import java.util.Locale;

/**
 * @author : lh
 */
public interface CountryService extends IService<CountryEntity> {

    List<CountryEntity> searchCountryByLanguage(String keyword, int maxSize, Locale locale);
}
