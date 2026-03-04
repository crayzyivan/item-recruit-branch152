package com.item.service;

/**
 * @author : lh
 */
public interface GenerateUrlCodeService {
    String generateUrlCodeByConfig(String jobTitle, String urlCode, String companyName, String replace);
    String generateUrlQuestionLink(Long candidateJobId, String datetime);
}
