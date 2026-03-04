package com.item.dto.iam;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author : lh
 */
@Data
public class IamUserContextDTO {
    private String id;
    private String accountId;
    private String companyCode;
    private String contactNumber;
    private String email;
    private String firstName;
    private String lastName;
    private String userName;
    private String userStatus;
    private Integer userType;
    private Boolean primaryUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String origin;
    private List<String> grantedAppCodes;
    private List<IamUserRoleDTO> userRoles;
    private String authorization;
    private Long candidateOneselfId;

    //recruit self customer field
    private Integer userIdentifyCode;
    
    /**
     * External information containing employee ID and account type
     */
    private ExternalInfoDTO externalInfo;
}
