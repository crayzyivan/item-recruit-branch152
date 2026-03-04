package com.item.convert;


import com.item.dto.iam.IamPointsTransactionDTO;
import com.item.dto.iam.IamPointsTransactionReqDTO;
import com.item.entity.PointsOperationLog;
import com.item.vo.PointLogListVo;
import com.item.vo.PointLogVO;
import com.item.vo.PointTopUpVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 积分操作记录表转换
 */
@Mapper(componentModel = "spring")
public interface PointOperationLogConverter {
    PointOperationLogConverter INSTANCE = Mappers.getMapper(PointOperationLogConverter.class);

    PointsOperationLog convertVoToEntity(PointLogVO vo);
    PointsOperationLog convertTopUpVoToEntity(PointTopUpVO upVO);

    IamPointsTransactionReqDTO convert2ReqDTO(IamPointsTransactionDTO pointsTransactionDTO);

    List<PointLogListVo> convertListToListVo(List<PointsOperationLog> list);
}
