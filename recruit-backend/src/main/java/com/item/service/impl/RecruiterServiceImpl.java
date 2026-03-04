package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.convert.RecruiterConverter;
import com.item.dto.LoginDTO;
import com.item.dto.RecruiterDTO;
import com.item.entity.RecruiterEntity;
import com.item.mapper.RecruiterMapper;
import com.item.service.RecruiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruiterServiceImpl extends ServiceImpl<RecruiterMapper, RecruiterEntity> implements RecruiterService {

    @Override
    public RecruiterDTO recruiterLogin(LoginDTO loginDTO) {
        QueryWrapper<RecruiterEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RecruiterEntity::getRecruiterEmail, loginDTO.getEmail());
        return RecruiterConverter.INSTANCE.convertEntityToDto(this.getOne(queryWrapper));
    }

}
