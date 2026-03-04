package com.item.framework.constant;

/**
 * @author : lh
 */
public interface IGlobalStatusCode {
    int JOB_MODULE = 10000;
    int NET_MODULE = 20000;
    int FEIGN_MODULE = 30000;
    int CANDIDATE_MODULE = 40000;
    int COMMON_MODULE = 50000;
    int CRM_MODULE = 60000;
    int AUTH_MODULE = 70000;
    int UNCHANGE_MODULE = 0;
    int getCode();
    String getMsg();
}
