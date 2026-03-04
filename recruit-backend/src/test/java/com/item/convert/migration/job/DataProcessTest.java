package com.item.convert.migration.job;

import com.item.dto.migration.job.CompanyInfoDTO;
import com.item.dto.migration.job.DataMigrationMappingDTO;
import com.item.dto.migration.job.MappingDataDTO;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class DataProcessTest {

    @Test
    void testCompany() {
        // 获取资源文件
        ClassPathResource resource = new ClassPathResource("company.csv");
        List<DataMigrationMappingDTO> mappingData = new ArrayList<>();
        // 使用BufferedReader按行读取
        try (InputStream inputStream = resource.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            int rows = 1;
            String line;
            // 循环读取每一行
            while ((line = bufferedReader.readLine()) != null) {
                if (rows++ == 1) {
                    continue;
                }

                // 处理每行内容
                log.info("读取到行内容: " + line);
                int cell = 10;
                if (line.indexOf("Unis, LLC")>0){
                    cell+=1;
                }
                String[] split = line.split(",");
                if (split.length<cell || StringUtils.isBlank(split[cell])) {
                    continue;
                }
                // 在这里可以添加你的业务逻辑，如解析、验证等
                DataMigrationMappingDTO dto = new DataMigrationMappingDTO();
                CompanyInfoDTO companyInfoDTO = new CompanyInfoDTO();
                companyInfoDTO.setCompanyName(split[1]);
                //dev 9  staging 10   prod 11
                companyInfoDTO.setCompanyCode(split[cell]);
                dto.setExt(JsonUtils.toJson(companyInfoDTO));
                dto.setBusType(MigrationBusTypeEnum.COMPANY_DATA.getCode());
                dto.setPgsqlId(split[0]);
                dto.setMysqlId(0L);
                mappingData.add(dto);
            }
        } catch (Exception e) {
            log.error("出现错误", e);
        }
        MappingDataDTO mappingDataDTO = new MappingDataDTO();
        mappingDataDTO.setMappingData(mappingData);
        log.info("处理数据成功json 字符串 {}", JsonUtils.toJson(mappingDataDTO));
    }

    @Test
    void testBEndUser() {
        // 获取资源文件
        ClassPathResource resource = new ClassPathResource("b-end-user.csv");
        List<DataMigrationMappingDTO> mappingData = new ArrayList<>();
        // 使用BufferedReader按行读取
        try (InputStream inputStream = resource.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            int rows = 1;
            String line;
            // 循环读取每一行
            while ((line = bufferedReader.readLine()) != null) {
                if (rows++ == 1) {
                    continue;
                }
                // 处理每行内容
                log.info("读取到行内容: " + line);
                String[] split = line.split(",");
                if (split.length < 10||StringUtils.isBlank(split[10])) {
                    continue;
                }
                // 在这里可以添加你的业务逻辑，如解析、验证等
                DataMigrationMappingDTO dto = new DataMigrationMappingDTO();
                //邮箱
                dto.setExt(split[3]);
                dto.setBusType(MigrationBusTypeEnum.RECRUIT.getCode());
                dto.setPgsqlId(split[4]);
                // 9 dev   10 staging  11 prod
                dto.setMysqlId(Long.valueOf(split[10]));
                mappingData.add(dto);
            }
        } catch (Exception e) {
            log.error("出现错误", e);
        }
        MappingDataDTO mappingDataDTO = new MappingDataDTO();
        mappingDataDTO.setMappingData(mappingData);
        log.info("处理数据成功json 字符串 {}", JsonUtils.toJson(mappingDataDTO));
    }
}
