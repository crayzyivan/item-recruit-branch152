package com.item.service;

import com.item.entity.ApplicationsEntity;
import com.item.vo.ApplicationsSyncVO;

import java.util.List;

/**
 * Applications同步服务接口
 * 
 * 处理PostgreSQL candidates.applications表到MySQL r_candidate_job表的数据同步，
 * 提供单条记录同步功能，确保数据一致性。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
public interface ApplicationsSyncService {

    /**
     * 同步单条Applications记录
     * 
     * 将PostgreSQL candidates.applications表中的单条记录同步到MySQL r_candidate_job表，
     * 用于实时同步或手动同步特定记录。
     * 
     * @param application PostgreSQL Applications实体对象
     * @return 是否同步成功
     */
    boolean syncSingleApplication(ApplicationsEntity application);


    /**
     * 批量同步
     * @param syncVo
     * @return
     */
    boolean applicationSync(ApplicationsSyncVO syncVo);

    /**
     * 未同步的
     * @return
     */
    List<String> getNotApplicationSync();
}
