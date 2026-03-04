package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.LoginDTO;
import com.item.dto.RecruiterDTO;
import com.item.entity.RecruiterEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecruiterService extends IService<RecruiterEntity> {

    RecruiterDTO recruiterLogin(LoginDTO loginDTO);

} 