package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.DictionaryDTO;
import com.item.entity.DictionaryEntity;

import java.util.List;
import java.util.Map;

public interface DictionaryService extends IService<DictionaryEntity> {
    DictionaryDTO get(Long id);

    List<DictionaryDTO> listByType(String type);

    /**
     * 根据类型批量查询
     *
     * @param types
     * @return
     */
    List<DictionaryDTO> listByTypes(List<String> types);

    /**
     * 根据id批量查询
     *
     * @param ids
     * @return
     */
    List<DictionaryDTO> listDtoByIds(List<Long> ids);

    /**
     * 获取所有字典内容
     *
     * @return
     */
    List<DictionaryDTO> getAll();

    /**
     * 获取字典映射 如果是货币类型 需要特殊处理
     *
     * @param types
     * @return
     */
    Map<Long, String> listDictMapping(List<String> types);
}