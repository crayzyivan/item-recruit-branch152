package com.item.controller;

import com.item.convert.DictionaryConverter;
import com.item.dto.DictionaryDTO;
import com.item.service.DictionaryService;
import com.item.vo.DictionaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/{id}")
    public DictionaryVO get(@PathVariable Long id) {
        DictionaryDTO dto = dictionaryService.get(id);
        return DictionaryConverter.INSTANCE.entityToVo(DictionaryConverter.INSTANCE.dtoToEntity(dto));
    }

    @GetMapping("/type/{type}")
    public List<DictionaryVO> listByType(@PathVariable String type) {
        List<DictionaryDTO> dtoList = dictionaryService.listByType(type);
        return DictionaryConverter.INSTANCE.dtolistToVolist(dtoList);
    }

    @GetMapping("/types")
    public Map<String, List<DictionaryVO>> listByTypes(@RequestBody List<String> types) {
        List<DictionaryDTO> dtoList = dictionaryService.listByTypes(types);
        List<DictionaryVO> voList = DictionaryConverter.INSTANCE.dtolistToVolist(dtoList);
        return voList.stream().collect(Collectors.groupingBy(DictionaryVO::getType));
    }

    @GetMapping("/all")
    public Map<String, List<DictionaryVO>> allList() {
        List<DictionaryDTO> dtoList = dictionaryService.getAll();
        List<DictionaryVO> voList = DictionaryConverter.INSTANCE.dtolistToVolist(dtoList);
        return voList.stream().collect(Collectors.groupingBy(DictionaryVO::getType));
    }
} 