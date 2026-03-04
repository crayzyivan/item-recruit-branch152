package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.StateEntity;

import java.util.List;
import java.util.Locale;

/**
 * @author : lh
 */
public interface StateService extends IService<StateEntity> {

    List<StateEntity> searchStatesByLanguage(String keyword, int maxSize, Locale locale);
}
