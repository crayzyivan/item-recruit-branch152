package com.item.vo;

import com.item.dto.iam.IamUserPermissionDTO;
import com.item.dto.iam.IamUserRoleDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Creates a new sub user within the current company
 *
 * @author hua.liu
 */
@Data
public class IamCreateSubUserVO {
    /**
     * User ID
     * Example: string
     */
    private String id;
    
    /**
     * Account ID
     * Example: string
     */
    private String accountId;
    
    /**
     * Company code
     * Example: string
     */
    private String companyCode;
    
    /**
     * Company name
     * Example: string
     */
    private String companyName;
    
    /**
     * Contact number
     * Example: string
     */
    private String contactNumber;
    
    /**
     * User email
     * Example: string
     */
    private String email;
    
    /**
     * User first name
     * Example: string
     */
    private String firstName;
    
    /**
     * User last name
     * Example: string
     */
    private String lastName;
    
    /**
     * User name
     * Example: string
     */
    private String userName;
    
    /**
     * User status
     * Example: string
     */
    private String userStatus;
    
    /**
     * User type
     * Example: 0
     */
    private String userType;
    
    /**
     * User type name
     * Example: string
     */
    private String userTypeName;
    
    /**
     * Whether user is primary user
     * Example: true
     */
    private Boolean primaryUser;
    
    /**
     * User roles list
     */
    private List<IamUserRoleDTO> userRoles;
    
    /**
     * User role IDs list
     */
    private List<String> userRoleIds;
    
    /**
     * User permissions list
     */
    private List<IamUserPermissionDTO> userPermissions;
    
    /**
     * User tags list
     */
    private List<String> userTags;
    
    /**
     * CRM user ID
     * Example: string
     */
    private String crmUserId;
    
    /**
     * TMS user ID
     * Example: string
     */
    private String tmsUserId;
    
    /**
     * Created at timestamp
     * Example: string
     */
    private String createdAt;
    
    /**
     * External information
     */
    private Map<String, Object> externalInfo;
    
    /**
     * Origin
     * Example: string
     */
    private String origin;
    
    /**
     * Granted app codes list
     */
    private List<String> grantedAppCodes;
} 