package com.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.item.entity.JobEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;


/**
 * <p>
 * 招聘职位 Mapper 接口
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
@Mapper
public interface JobMapper extends BaseMapper<JobEntity> {
    @Select("SELECT * FROM r_job order by create_time desc")
    @Results(id = "jobMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "typeId", column = "type_id"),
        @Result(property = "jobType", column = "type_id", one = @One(select = "com.item.mapper.JobTypeMapper.selectById")),
        @Result(property = "categoryId", column = "category_id"),
        @Result(property = "jobCategory", column = "category_id", one = @One(select = "com.item.mapper"
                + ".JobCategoryMapper.selectById")),
        @Result(property = "modeId", column = "mode_id"),
        @Result(property = "jobMode", column = "mode_id", one = @One(select = "com.item.mapper.JobModeMapper.selectById"))
    })
    IPage<JobEntity> selectJobList(IPage<JobEntity> page);
}