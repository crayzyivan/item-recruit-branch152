package com.item.dto.ayrshare;

import com.item.framework.constant.AyrshareStatus;
import com.item.framework.constant.CommonConstants;
import static com.item.framework.constant.CommonConstants.StrConstants.ERROR;
import static com.item.framework.constant.CommonConstants.StrConstants.SUCCESS;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Ayrshare发布响应DTO
 * 
 * @author lh
 * @since 1.0.0
 */
@Data
public class AyrSharePostResDTO {
    
    /**
     * 响应状态：success/error
     */
    private String status;
    
    /**
     * 错误信息列表
     */
    private List<AyrShareErrorDTO> errors;
    
    /**
     * 成功发布的帖子ID列表
     */
    private List<AyrSharePostIdDTO> postIds;
    
    /**
     * 请求ID
     */
    private String id;
    
    /**
     * Facebook帖子ID
     */
    private String fbId;
    
    /**
     * 引用ID
     */
    private String refId;
    
    /**
     * 发布的内容
     */
    private String post;
    
    /**
     * 是否验证
     */
    private Boolean validate;

    private List<AyrSharePostResDTO> posts;

    /**
     * 合并成功以及失败字段 状态字段
     *
     * @param postIds
     * @return
     */
    public static AyrSharePostResDTO merge(List<AyrSharePostResDTO> postIds) {
        AyrSharePostResDTO res = new AyrSharePostResDTO();
        postIds.stream().forEach(r -> {
            //如果成功 并且 当前状态为空 设置为成功
            if (CommonConstants.StrConstants.SUCCESS.equalsIgnoreCase(r.getStatus()) && StringUtils.isBlank(res.getStatus())) {
                res.setStatus(SUCCESS);
            }
            //如果有任何一个是错误状态 赋值为错误
            if (CommonConstants.StrConstants.ERROR.equalsIgnoreCase(r.getStatus())) {
                res.setStatus(ERROR);
            }
            //如果成功的不是空 合并到成功中
            if (CollectionUtils.isNotEmpty(r.getPostIds())) {
                if (res.getPostIds() == null) {
                    res.setPostIds(new ArrayList<>());
                }
                res.getPostIds().addAll(r.getPostIds());
            }
            //如果失败的不是空 合并到失败中
            if (CollectionUtils.isNotEmpty(r.getErrors())) {
                if (res.getErrors() == null) {
                    res.setErrors(new ArrayList<>());
                }
                res.getErrors().addAll(r.getErrors());
            }
        });
        return res;
    }

    public static AyrshareStatus checkResponse(List<AyrSharePostResDTO> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return AyrshareStatus.SHARE_FAILED;
        }
        boolean allSuccess = postIds.stream().allMatch(r -> r.getStatus().equalsIgnoreCase(SUCCESS));
        if (allSuccess) {
            return AyrshareStatus.SHARE_SUCCESS;
        }
        boolean allError = postIds.stream().allMatch(r -> r.getStatus().equalsIgnoreCase(ERROR));
        if (allError) {
            return AyrshareStatus.SHARE_FAILED;
        }
        return AyrshareStatus.PARTIAL_SUCCESS;
    }
}
