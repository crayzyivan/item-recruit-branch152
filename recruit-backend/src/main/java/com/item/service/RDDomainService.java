package com.item.service;

/**
 * @author : lh
 */
public interface RDDomainService {

    void updateJobEsOld2NewLocation(Integer pageIndex, Integer pageSize, Long currentJobId);
    void updateJobInterviewLength(Integer pageIndex, Integer pageSize, Long currentJobId, Integer interviewLength);
}
