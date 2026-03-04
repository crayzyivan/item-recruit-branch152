package com.item.vo;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class PreLoginCheckResVO {
    private Long userId;
    private String userName;
    private String email;
    private Long candidateId;
    private Boolean access;

    public static PreLoginCheckResVO buildFail() {
        PreLoginCheckResVO preLoginCheckResVO = new PreLoginCheckResVO();
        preLoginCheckResVO.setAccess(false);
        return preLoginCheckResVO;
    }

    public static PreLoginCheckResVO buildSuccess(Long userId,  String userName, String email, Long candidateId) {
        PreLoginCheckResVO preLoginCheckResVO = new PreLoginCheckResVO();
        preLoginCheckResVO.setAccess(true);
        preLoginCheckResVO.setUserId(userId);
        preLoginCheckResVO.setUserName(userName);
        preLoginCheckResVO.setEmail(email);
        preLoginCheckResVO.setCandidateId(candidateId);
        return preLoginCheckResVO;
    }
}
