package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.LeadsCustomerCompanyDTO;
import com.item.entity.LeadsCustomerCompanyEntity;

import java.util.List;

/**
 * <p>
 * lead customer company 映射关系服务接口
 * </p>
 *
 * @author lh on 2025/7/29
 * @since 1.0.0
 */
public interface LeadsCustomerCompanyService extends IService<LeadsCustomerCompanyEntity> {
    
    /**
     * 添加r_leads_customer_company数据
     *
     * @param dto 数据对象
     * @return 是否成功
     */
    boolean addLeadsCustomerCompany(LeadsCustomerCompanyDTO dto);
    
    /**
     * 根据r_leads_customer_company.company_code 查对应的数据
     *
     * @param companyCode 公司代码
     * @return 数据对象
     */
    LeadsCustomerCompanyDTO getCustomerByCompanyCode(String companyCode);
    
    /**
     * 根据r_leads_customer_company.company_code 更新对应的数据
     *
     * @param companyCode 公司代码
     * @param dto 更新数据
     * @return 是否成功
     */
    boolean updateByCompanyCode(String companyCode, LeadsCustomerCompanyDTO dto);
    
    /**
     * 根据r_leads_customer_company.company_code 删除对应的数据
     *
     * @param companyCode 公司代码
     * @return 是否成功
     */
    boolean deleteByCompanyCode(String companyCode);
    
    /**
     * 根据r_leads_customer_company.id 查对应的数据
     *
     * @param id 主键ID
     * @return 数据对象
     */
    LeadsCustomerCompanyDTO getById(Long id);
    
    /**
     * 根据r_leads_customer_company.id 更新对应的数据
     *
     * @param id 主键ID
     * @param dto 更新数据
     * @return 是否成功
     */
    boolean updateById(Long id, LeadsCustomerCompanyDTO dto);
    
    /**
     * 根据r_leads_customer_company.id 删除对应的数据
     *
     * @param id 主键ID
     * @return 是否成功
     */
    boolean deleteById(Long id);
    
    /**
     * 根据公司代码查询所有相关数据
     *
     * @param companyCode 公司代码
     * @return 数据列表
     */
    List<LeadsCustomerCompanyDTO> listByCompanyCode(String companyCode);
    
    /**
     * 根据CRM客户ID查询数据
     *
     * @param crmCustomerId CRM客户ID
     * @return 数据对象
     */
    LeadsCustomerCompanyDTO getByCrmCustomerId(Long crmCustomerId);
    
    /**
     * 根据CRM Leads ID查询数据
     *
     * @param crmLeadsId CRM Leads ID
     * @return 数据对象
     */
    LeadsCustomerCompanyDTO getByCrmLeadsId(Long crmLeadsId);
}