package com.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateJobEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CandidateJobMapper extends BaseMapper<CandidateJobEntity> {

    @Select("SELECT " +
            "  rcj.* " +
            "FROM " +
            "  r_candidate rc " +
            "  LEFT JOIN r_candidate_job rcj ON rcj.candidate_id = rc.id " +
            "  LEFT JOIN r_job rj ON rj.id = rcj.job_id " +
            "WHERE " +
            "  rc.candidate_email = #{candidateEmail} " +
            "  AND rj.interview_url_id = #{interviewUrlId} " +
            "  AND rcj.interview_mail_status = 1 " +
            "  AND rcj.apply_status < #{applyStatus} " +
            "  AND (#{applicationId} IS NULL OR rcj.id = #{applicationId}) " +
            "  AND (#{candidateName} IS NULL OR rc.candidate_name = #{candidateName})")
    List<CandidateJobEntity> getCandidateJobEntityByInterviewInfo(@Param("candidateEmail") String candidateEmail, @Param("interviewUrlId") String interviewUrlId, @Param("applyStatus") Integer applyStatus, @Param("applicationId") Long applicationId, @Param("candidateName") String candidateName);

    @Select("SELECT DISTINCT rc.* " +
            "FROM r_candidate_job rcj " +
            "LEFT JOIN r_candidate rc ON rc.id = rcj.candidate_id " +
            "WHERE rcj.company_code = #{companyCode}")
    Page<CandidateEntity> selectCandidatePage(Page<CandidateEntity> page, @Param("companyCode") String companyCode);
} 