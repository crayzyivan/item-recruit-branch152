package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.convert.LeadsCustomerCompanyConvert;
import com.item.dto.LeadsCustomerCompanyDTO;
import com.item.entity.LeadsCustomerCompanyEntity;
import com.item.mapper.LeadsCustomerCompanyMapper;
import com.item.service.LeadsCustomerCompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * lead customer company 映射关系服务实现类
 * </p>
 *
 * @author lh on 2025/7/29
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeadsCustomerCompanyServiceImpl extends ServiceImpl<LeadsCustomerCompanyMapper, LeadsCustomerCompanyEntity>
        implements LeadsCustomerCompanyService {

    private final LeadsCustomerCompanyConvert leadsCustomerCompanyConvert;

    @Override
    public boolean addLeadsCustomerCompany(LeadsCustomerCompanyDTO dto) {
        log.info("Adding leads customer company data: {}", dto);
        LeadsCustomerCompanyEntity entity = leadsCustomerCompanyConvert.dtoToEntity(dto);
        boolean result = save(entity);
        log.info("Add leads customer company data result: {}", result);
        return result;
    }

    @Override
    public LeadsCustomerCompanyDTO getCustomerByCompanyCode(String companyCode) {
        log.info("Getting leads customer company data by company code: {}", companyCode);
        if (StringUtils.isBlank(companyCode)) {
            log.warn("Company code is blank");
            return null;
        }

        LambdaQueryWrapper<LeadsCustomerCompanyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(LeadsCustomerCompanyEntity::getId, LeadsCustomerCompanyEntity::getCrmCustomerCode,
                LeadsCustomerCompanyEntity::getCrmCustomerId, LeadsCustomerCompanyEntity::getCentralCompanyCode,
                LeadsCustomerCompanyEntity::getCentralLeadCompanyId);
        queryWrapper.eq(LeadsCustomerCompanyEntity::getCentralCompanyCode, companyCode);
        queryWrapper.last("LIMIT 1");

        LeadsCustomerCompanyEntity entity = getOne(queryWrapper);
        return entity != null ? leadsCustomerCompanyConvert.entityToDto(entity) : null;
    }

    @Override
    public boolean updateByCompanyCode(String companyCode, LeadsCustomerCompanyDTO dto) {
        log.info("Updating leads customer company data by company code: {}, data: {}", companyCode, dto);
        if (StringUtils.isBlank(companyCode)) {
            log.warn("Company code is blank");
            return false;
        }

        LambdaUpdateWrapper<LeadsCustomerCompanyEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LeadsCustomerCompanyEntity::getCentralCompanyCode, companyCode);

        LeadsCustomerCompanyEntity entity = leadsCustomerCompanyConvert.dtoToEntity(dto);
        boolean result = update(entity, updateWrapper);
        log.info("Update leads customer company data by company code result: {}", result);
        return result;

    }

    @Override
    public boolean deleteByCompanyCode(String companyCode) {
        log.info("Deleting leads customer company data by company code: {}", companyCode);
        if (StringUtils.isBlank(companyCode)) {
            log.warn("Company code is blank");
            return false;
        }
        LambdaQueryWrapper<LeadsCustomerCompanyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeadsCustomerCompanyEntity::getCentralCompanyCode, companyCode);

        boolean result = remove(queryWrapper);
        log.info("Delete leads customer company data by company code result: {}", result);
        return result;
    }

    @Override
    public LeadsCustomerCompanyDTO getById(Long id) {
        log.info("Getting leads customer company data by id: {}", id);
        if (id == null) {
            log.warn("ID is null");
            return null;
        }

        LeadsCustomerCompanyEntity entity = super.getById(id);
        return entity != null ? leadsCustomerCompanyConvert.entityToDto(entity) : null;
    }

    @Override
    public boolean updateById(Long id, LeadsCustomerCompanyDTO dto) {
        log.info("Updating leads customer company data by id: {}, data: {}", id, dto);
        if (id == null) {
            log.warn("ID is null");
            return false;
        }

        LeadsCustomerCompanyEntity entity = leadsCustomerCompanyConvert.dtoToEntity(dto);
        entity.setId(id);
        boolean result = updateById(entity);
        log.info("Update leads customer company data by id result: {}", result);
        return result;
    }

    @Override
    public boolean deleteById(Long id) {
        log.info("Deleting leads customer company data by id: {}", id);
        if (id == null) {
            log.warn("ID is null");
            return false;
        }
        boolean result = removeById(id);
        log.info("Delete leads customer company data by id result: {}", result);
        return result;
    }

    @Override
    public List<LeadsCustomerCompanyDTO> listByCompanyCode(String companyCode) {
        log.info("Listing leads customer company data by company code: {}", companyCode);
        if (StringUtils.isBlank(companyCode)) {
            log.warn("Company code is blank");
            return List.of();
        }

        LambdaQueryWrapper<LeadsCustomerCompanyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeadsCustomerCompanyEntity::getCentralCompanyCode, companyCode);

        List<LeadsCustomerCompanyEntity> entityList = list(queryWrapper);
        return leadsCustomerCompanyConvert.entityListToDtoList(entityList);
    }

    @Override
    public LeadsCustomerCompanyDTO getByCrmCustomerId(Long crmCustomerId) {
        log.info("Getting leads customer company data by CRM customer id: {}", crmCustomerId);
        if (crmCustomerId == null) {
            log.warn("CRM customer ID is null");
            return null;
        }

        LambdaQueryWrapper<LeadsCustomerCompanyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeadsCustomerCompanyEntity::getCrmCustomerId, crmCustomerId);
        queryWrapper.last("LIMIT 1");

        LeadsCustomerCompanyEntity entity = getOne(queryWrapper);
        return entity != null ? leadsCustomerCompanyConvert.entityToDto(entity) : null;
    }

    @Override
    public LeadsCustomerCompanyDTO getByCrmLeadsId(Long crmLeadsId) {
        log.info("Getting leads customer company data by CRM leads id: {}", crmLeadsId);
        if (crmLeadsId == null) {
            log.warn("CRM leads ID is null");
            return null;
        }

        LambdaQueryWrapper<LeadsCustomerCompanyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeadsCustomerCompanyEntity::getCrmLeadsId, crmLeadsId);
        queryWrapper.last("LIMIT 1");

        LeadsCustomerCompanyEntity entity = getOne(queryWrapper);
        return entity != null ? leadsCustomerCompanyConvert.entityToDto(entity) : null;
    }
}