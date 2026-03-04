package com.item.convert;

import com.item.dto.IamCreateSubUserReqDTO;
import com.item.dto.IamSignUpDTO;
import com.item.dto.iam.ExistsCompanyAdapterReqDTO;
import com.item.dto.iam.ExistsCompanyAdapterResDTO;
import com.item.dto.iam.ExistsCompanyDTO;
import com.item.dto.iam.ExistsCompanyReqDTO;
import com.item.dto.iam.ExistsCompanyResDTO;
import com.item.dto.iam.ExistsUserAdapterReqDTO;
import com.item.dto.iam.ExistsUserAdapterResDTO;
import com.item.dto.iam.ExistsUserDTO;
import com.item.dto.iam.ExistsUserReqDTO;
import com.item.dto.iam.ExistsUserResDTO;
import com.item.dto.iam.IamCreateSubUserAdapterDTO;
import com.item.dto.iam.IamCreateSubUserAdapterResDTO;
import com.item.dto.iam.IamCreateSubUserDTO;
import com.item.dto.iam.IamCreateSubUserResDTO;
import com.item.dto.iam.IamSignUpAdapterDTO;
import com.item.dto.iam.IamSignUpAdapterResDTO;
import com.item.dto.iam.IamSignUpManagerCompanyDTO;
import com.item.dto.iam.IamSignUpManagerCompanyResDTO;
import com.item.vo.IamCreateSubUserVO;
import com.item.vo.IamSignUpVO;
import com.item.vo.iam.ExistsCompanyResVO;
import com.item.vo.iam.ExistsUserResVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * @author : lh
 */
@Mapper(componentModel = "spring")
public interface IamModeConvert {
    IamSignUpManagerCompanyDTO convert2IamDTO(IamSignUpAdapterDTO iamSignUpAdapterDTO);
    ExistsCompanyReqDTO convert2IamDTO(ExistsCompanyAdapterReqDTO existsCompanyAdapter);
    ExistsUserReqDTO convert2IamDTO(ExistsUserAdapterReqDTO existsUserAdapterReqDTO);
    @Mapping(target = "companyType", source = "companyType", qualifiedByName = "intToString")
    IamSignUpAdapterDTO convert2AdapterDTO(IamSignUpDTO iamSignUpDTO);
    ExistsUserAdapterReqDTO convert2AdapterDTO(ExistsUserDTO existsUserDTO);

    IamCreateSubUserAdapterDTO convert2AdapterDTO(IamCreateSubUserReqDTO createSubUserReqDTO);

    IamCreateSubUserDTO convert2IamDTO(IamCreateSubUserAdapterDTO createSubUserAdapterDTO);

    IamSignUpAdapterResDTO convert2ResDTO(IamSignUpManagerCompanyResDTO iamSignUpAdapterDTO);

    ExistsCompanyAdapterResDTO convert2ResDTO(ExistsCompanyResDTO existsCompanyResDTO);

    IamCreateSubUserAdapterResDTO convert2ResDTO(IamCreateSubUserResDTO createSubUserResDTO);
    ExistsUserAdapterResDTO convert2ResDTO(ExistsUserResDTO existsUserResDTO);

    IamSignUpVO convert2VO(IamSignUpAdapterResDTO iamSignUpAdapterResDTO);

    IamCreateSubUserVO convert2VO(IamCreateSubUserAdapterResDTO iamCreateSubUserAdapterResDTO);

    ExistsCompanyAdapterReqDTO convert2AdapterDTO(ExistsCompanyDTO existsCompanyDTO);
    ExistsCompanyResVO convert2VO(ExistsCompanyAdapterResDTO existsCompanyDTO);
    ExistsUserResVO convert2VO(ExistsUserAdapterResDTO existsCompanyDTO);

    @Named("intToString")
    default String intToString(Integer value) {
        return value != null ? String.valueOf(value) : null;
    }
}
