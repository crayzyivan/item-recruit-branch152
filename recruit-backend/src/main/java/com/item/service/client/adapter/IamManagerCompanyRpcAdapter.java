package com.item.service.client.adapter;

import com.item.convert.IamModeConvert;
import com.item.dto.iam.ExistsCompanyAdapterReqDTO;
import com.item.dto.iam.ExistsCompanyAdapterResDTO;
import com.item.dto.iam.ExistsCompanyReqDTO;
import com.item.dto.iam.ExistsCompanyResDTO;
import com.item.dto.iam.ExistsUserAdapterReqDTO;
import com.item.dto.iam.ExistsUserAdapterResDTO;
import com.item.dto.iam.ExistsUserReqDTO;
import com.item.dto.iam.ExistsUserResDTO;
import com.item.dto.iam.FeignResponse;
import com.item.dto.iam.IamCreateSubUserAdapterDTO;
import com.item.dto.iam.IamCreateSubUserAdapterResDTO;
import com.item.dto.iam.IamCreateSubUserDTO;
import com.item.dto.iam.IamCreateSubUserResDTO;
import com.item.dto.iam.IamSignUpAdapterDTO;
import com.item.dto.iam.IamSignUpAdapterResDTO;
import com.item.dto.iam.IamSignUpManagerCompanyDTO;
import com.item.dto.iam.IamSignUpManagerCompanyResDTO;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.error.BusinessException;
import com.item.service.client.IamFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamManagerCompanyRpcAdapter {
    private final IamFeignClient iamClient;
    private final IamModeConvert iamModeConvert;

    public IamSignUpAdapterResDTO signUpManagerCompany(IamSignUpAdapterDTO signUpAdapter) {
        if (signUpAdapter == null) {
            log.error("signUpAdapter param is null");
            throw BusinessException.of(CommonResponseCode.COMMON_METHOD_PARAM_IS_NULL);
        }
        IamSignUpManagerCompanyDTO iamSignUpManagerCompanyDTO = iamModeConvert.convert2IamDTO(signUpAdapter);
        FeignResponse<IamSignUpManagerCompanyResDTO> iamSignUpManagerCompanyResDTOIamResponse = iamClient.signUpManagerCompany(iamSignUpManagerCompanyDTO);
        IamSignUpManagerCompanyResDTO iamSignUpManagerCompanyResDTO = getData(iamSignUpManagerCompanyResDTOIamResponse);
        return iamModeConvert.convert2ResDTO(iamSignUpManagerCompanyResDTO);
    }

    public IamCreateSubUserAdapterResDTO createSubUser(IamCreateSubUserAdapterDTO createSubUserAdapter) {
        if (createSubUserAdapter == null) {
            log.error("createSubUser param is null");
            throw BusinessException.of(CommonResponseCode.COMMON_METHOD_PARAM_IS_NULL);
        }
        IamCreateSubUserDTO iamCreateSubUserDTO = iamModeConvert.convert2IamDTO(createSubUserAdapter);
        FeignResponse<IamCreateSubUserResDTO> createSubUserResDTOIamResponse = iamClient.createSubUser(iamCreateSubUserDTO);
        IamCreateSubUserResDTO createSubUserResDTO = getData(createSubUserResDTOIamResponse);
        return iamModeConvert.convert2ResDTO(createSubUserResDTO);
    }


    public ExistsCompanyAdapterResDTO existCompany(ExistsCompanyAdapterReqDTO existsCompanyAdapterReqDTO) {
        if (existsCompanyAdapterReqDTO == null) {
            log.error("existCompany param is null");
            throw BusinessException.of(CommonResponseCode.COMMON_METHOD_PARAM_IS_NULL);
        }
        ExistsCompanyReqDTO existsCompanyReqDTO = iamModeConvert.convert2IamDTO(existsCompanyAdapterReqDTO);
        FeignResponse<ExistsCompanyResDTO> existsCompanyResDTOFeignResponse = iamClient.existCompany(existsCompanyReqDTO);
        ExistsCompanyResDTO existsCompanyResDTO = getData(existsCompanyResDTOFeignResponse);
        return iamModeConvert.convert2ResDTO(existsCompanyResDTO);
    }


    public ExistsUserAdapterResDTO existUser(ExistsUserAdapterReqDTO existsUserAdapterReqDTO) {
        if (existsUserAdapterReqDTO == null) {
            log.error("existUser param is null");
            throw BusinessException.of(CommonResponseCode.COMMON_METHOD_PARAM_IS_NULL);
        }
        ExistsUserReqDTO existsUserReqDTO = iamModeConvert.convert2IamDTO(existsUserAdapterReqDTO);
        FeignResponse<ExistsUserResDTO> existsCompanyResDTOFeignResponse = iamClient.existUser(existsUserReqDTO);
        ExistsUserResDTO existsUserResDTO = getData(existsCompanyResDTOFeignResponse);
        return iamModeConvert.convert2ResDTO(existsUserResDTO);
    }


    /**
     * 解析响应体
     * @param iamResponse
     * @return
     * @param <T>
     */
    private <T> T getData(FeignResponse<T> iamResponse) {
        if (iamResponse.getSuccess() == null || !iamResponse.getSuccess()) {
            log.error("get user info failed {}", iamResponse);
            return null;
        }
        if (iamResponse.getData() == null) {
            log.warn("get user info data is null {} ", iamResponse);
            return null;
        }
        return iamResponse.getData();
    }
}
