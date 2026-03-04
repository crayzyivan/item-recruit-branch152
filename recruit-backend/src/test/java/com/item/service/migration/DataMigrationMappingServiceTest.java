package com.item.service.migration;

import com.item.convert.migration.job.JobMigrationConvert;
import com.item.dto.migration.job.CompanyInfoDTO;
import com.item.dto.migration.job.DataMigrationMappingDTO;
import com.item.dto.migration.job.MappingDataDTO;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据迁移映射服务测试类
 * 
 * 使用SpringBootTest进行集成测试，测试所有CRUD操作，
 * 包括保存映射、双向查询、存在性检查、删除操作等功能。
 * 测试覆盖正常流程和边界情况。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@SpringBootTest
@Transactional
@Rollback
class DataMigrationMappingServiceTest {

    @Resource
    private DataMigrationMappingService dataMigrationMappingService;
    @Resource
    private JobMigrationConvert jobMigrationConvert;

    // 测试数据常量
    private static final String TEST_PGSQL_ID_1 = "pgsql_test_001";
    private static final String TEST_PGSQL_ID_2 = "pgsql_test_002";
    private static final Long TEST_MYSQL_ID_1 = 1001L;
    private static final Long TEST_MYSQL_ID_2 = 1002L;
    private static final MigrationBusTypeEnum TEST_BUS_TYPE_1 = MigrationBusTypeEnum.JOB;
    private static final MigrationBusTypeEnum TEST_BUS_TYPE_2 = MigrationBusTypeEnum.CANDIDATE;
    private MappingDataDTO request = null;

    @BeforeEach
    void setUp() {
        log.info("=== 开始测试数据迁移映射服务 ===");
        log.info("DataMigrationMappingService: {}", dataMigrationMappingService.getClass().getName());
        String dataStr = """
                {
                  "mappingData": [
                    {
                      "pgsqlId": "1",
                      "mysqlId": 1,
                      "busType": 8,
                      "ext": "{\\"companyCode\\":\\"item\\",\\"companyName\\":\\"item\\"}"
                    },
                    {
                      "pgsqlId": "2",
                      "mysqlId": 1,
                      "busType": 8,
                      "ext": "{\\"companyCode\\":\\"unis\\",\\"companyName\\":\\"unis\\"}"
                    }
                  ]
                }""";
        DataMigrationMappingDTO d1 = new DataMigrationMappingDTO();
        d1.setExt("""
                {"companyCode":"item","companyName":"item"}""");
        d1.setPgsqlId("1");
        DataMigrationMappingDTO d2 = new DataMigrationMappingDTO();
        d2.setExt("""
                {"companyCode":"item","companyName":"item"}""");
        d2.setPgsqlId("2");
        MappingDataDTO m1 = new MappingDataDTO();
        m1.setMappingData(List.of(d1,d2));
        JsonUtils.toJson(m1);
        request = JsonUtils.toObject(dataStr, MappingDataDTO.class);

    }


    public static void main(String[] args) {

        List<String> emailList2 = List.of(
                "jimoh.yusuf@unisco.com",
                "david.augustine@item.com",
                "christine.wei@cubework.com",
                "jett.whitaker@item.com",
                "hiring@unisco.com",
                "mark@azulworkforce.com",
                "kyle.flores@unisco.com",
                "joe.garcia@unisco.com",
                "harold.cuarezma@unisco.com",
                "jimmy.esparza@unisco.com",
                "ismael.robles@unisco.com",
                "beezer.sullivan@unisco.com",
                "tom.yu@item.com",
                "nabin.gautam@unisco.com",
                "demo@item.com",
                "joseph.sparacino@unisco.com",
                "alejandra.capetillo@unisco.com",
                "aurora.rodarte@unisco.com",
                "unis_lso_recruiters@unisco.com",
                "aura.bonnell@unisco.com",
                "erica.baldwin@unisco.com",
                "shini.kei@plus-automation.com",
                "stacy.nguyen@item.com",
                "mkummer@kummer.us",
                "Yu_jim@hotmail.com",
                "samwise.luo@unisco.com",
                "raul.escobar@unisco.com",
                "scott.simanek@unisco.com",
                "harry.wei@item.com",
                "xavier.chu@cubework.com",
                "daniel.cordova@unisco.com",
                "ericwang199576@outlook.com",
                "trump_478@outlook.com",
                "riley.qiu@item.com",
                "demo2.unis@gmail.com"
        );
        Map<String, String> emailMap = new HashMap<>();
        emailMap.put("shini.kei@plus-automation.com", "41f86d75-e6eb-4cef-8914-37bd00f36c2e");
        emailMap.put("harold.cuarezma@unisco.com", "c46e569c-1503-48c1-9917-c6a0760974e8");
        emailMap.put("mkummer@kummer.us", "4743ba39-2040-4ffa-9d4f-c1eac1f3d634");
        emailMap.put("demo2.unis@gmail.com", "370f9c61-2b83-4702-b8dc-41e55182081f");
        emailMap.put("jimoh.yusuf@unisco.com", "e2cbac84-8d01-44b3-bd03-a2a320042073");
        emailMap.put("unis_lso_recruiters@unisco.com", "7b25dede-f1e1-4d4f-838c-78ac0db5edb9");
        emailMap.put("stacy.nguyen@item.com", "f61a065e-1e21-48cc-80d4-7633a9608ff5");
        emailMap.put("raul.escobar@unisco.com", "9f5c0e7c-3d55-4fb3-bf08-658c01c6897e");
        emailMap.put("mark@azulworkforce.com", "3d388600-b202-4e57-8a83-85b337bf0b46");
        emailMap.put("jett.whitaker@item.com", "95badbf3-758a-4192-9ed9-052def1987c7");
        emailMap.put("kyle.flores@unisco.com", "39b92821-60ef-4b1b-9546-61e8676cea83");
        emailMap.put("david.augustine@item.com", "0bb5e8a5-3e1b-433d-84bc-273d05ca6cd5");
        emailMap.put("ericwang199576@outlook.com", "b5ebe5d8-d4fc-4017-adda-9c0650493d85");
        emailMap.put("scott.simanek@unisco.com", "2f90eba0-f7f6-40a2-9ee0-319dd69d6246");
        emailMap.put("hiring@unisco.com", "a2778b59-8ed0-4edc-ac70-8ed0e66067da");
        emailMap.put("ismael.robles@unisco.com", "c5946d1c-2e6a-4225-af8f-b0feb83b444d");
        emailMap.put("tom.yu@item.com", "4ad7f191-4d22-414e-a6f9-36f86f261fc7");
        emailMap.put("joseph.sparacino@unisco.com", "d50adb56-1cc1-4e6d-a9d4-41b0550c27f6");
        emailMap.put("christine.wei@cubework.com", "410d537a-3607-4199-84be-6da2118a058f");
        emailMap.put("jimmy.esparza@unisco.com", "04aa129f-bdf7-4b34-899c-d266ed7213f9");
        emailMap.put("daniel.cordova@unisco.com", "ea8a1c45-d891-46d6-b354-16b1ba3a59b9");
        emailMap.put("trump_478@outlook.com", "198a3ade-759c-4e1f-ba83-33ccf60c7c9f");
        emailMap.put("joe.garcia@unisco.com", "22ddc4d2-5b6a-4c36-b10f-9cc41d295af3");
        StringBuilder sb = new StringBuilder();

        for (String s : emailList2) {
            sb.append(emailMap.getOrDefault(s, "")).append("\n");
        }
        System.out.println(sb.toString());
    }
    @Test
    void test01(){
        List<DataMigrationMappingDTO> mappingData = request.getMappingData();
        List<DataMigrationMappingEntity> dataMigrationMappingEntities = jobMigrationConvert.convert2DataMigrationMappingEntitys(mappingData);
        int saveBatchMappings = dataMigrationMappingService.saveBatchMappings(dataMigrationMappingEntities);
        log.info("insert count {}", saveBatchMappings);
        List<DataMigrationMappingEntity> byPgsqlIdsAndType = dataMigrationMappingService.findByPgsqlIdsAndType(List.of("1", "2"), MigrationBusTypeEnum.COMPANY_DATA);
        String ext = byPgsqlIdsAndType.getFirst().getExt();
        CompanyInfoDTO object = JsonUtils.toObject(ext, CompanyInfoDTO.class);
        log.info("object = {}", object);
    }
    /**
     * 测试保存映射功能
     */
    @Test
    void testSaveMapping() {
        log.info("=== 测试保存映射功能 ===");

        // 1. 测试正常保存
        boolean result1 = dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_1, TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);
        assertTrue(result1, "保存映射应该成功");
        log.info("✅ 正常保存测试通过");

        // 2. 测试更新已存在的映射
        boolean result2 = dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_1, TEST_MYSQL_ID_2, TEST_BUS_TYPE_1);
        assertTrue(result2, "更新已存在映射应该成功");
        
        // 验证更新结果
        Optional<DataMigrationMappingEntity> updated = dataMigrationMappingService.findByPgsqlIdAndType(TEST_PGSQL_ID_1, TEST_BUS_TYPE_1);
        assertTrue(updated.isPresent(), "更新后应该能找到记录");
        assertEquals(TEST_MYSQL_ID_2, updated.get().getMysqlId(), "MySQL ID应该已更新");
        log.info("✅ 更新已存在映射测试通过");

        // 3. 测试参数验证
        boolean result3 = dataMigrationMappingService.saveMapping(null, TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);
        assertFalse(result3, "PostgreSQL ID为null时应该返回false");

        boolean result4 = dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_1, null, TEST_BUS_TYPE_1);
        assertFalse(result4, "MySQL ID为null时应该返回false");

        boolean result5 = dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_1, TEST_MYSQL_ID_1, null);
        assertFalse(result5, "业务类型为null时应该返回false");

        boolean result6 = dataMigrationMappingService.saveMapping("", TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);
        assertFalse(result6, "PostgreSQL ID为空字符串时应该返回false");
        log.info("✅ 参数验证测试通过");
    }

    /**
     * 测试根据PostgreSQL ID和类型查询
     */
    @Test
    void testFindByPgsqlIdAndType() {
        log.info("=== 测试根据PostgreSQL ID和类型查询 ===");

        // 1. 准备测试数据
        dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_1, TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);
        dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_2, TEST_MYSQL_ID_2, TEST_BUS_TYPE_2);

        // 2. 测试正常查询
        Optional<DataMigrationMappingEntity> result1 = dataMigrationMappingService.findByPgsqlIdAndType(TEST_PGSQL_ID_1, TEST_BUS_TYPE_1);
        assertTrue(result1.isPresent(), "应该能找到对应的映射记录");
        assertEquals(TEST_PGSQL_ID_1, result1.get().getPgsqlId(), "PostgreSQL ID应该匹配");
        assertEquals(TEST_MYSQL_ID_1, result1.get().getMysqlId(), "MySQL ID应该匹配");
        assertEquals(TEST_BUS_TYPE_1.getCode(), result1.get().getBusType(), "业务类型应该匹配");
        log.info("✅ 正常查询测试通过");

        // 3. 测试查询不存在的记录
        Optional<DataMigrationMappingEntity> result2 = dataMigrationMappingService.findByPgsqlIdAndType("non_exist", TEST_BUS_TYPE_1);
        assertFalse(result2.isPresent(), "不存在的记录应该返回空Optional");
        log.info("✅ 查询不存在记录测试通过");

        // 4. 测试参数验证
        Optional<DataMigrationMappingEntity> result3 = dataMigrationMappingService.findByPgsqlIdAndType(null, TEST_BUS_TYPE_1);
        assertFalse(result3.isPresent(), "PostgreSQL ID为null时应该返回空Optional");

        Optional<DataMigrationMappingEntity> result4 = dataMigrationMappingService.findByPgsqlIdAndType(TEST_PGSQL_ID_1, null);
        assertFalse(result4.isPresent(), "业务类型为null时应该返回空Optional");
        log.info("✅ 参数验证测试通过");
    }

    /**
     * 测试根据MySQL ID和类型查询
     */
    @Test
    void testFindByMysqlIdAndType() {
        log.info("=== 测试根据MySQL ID和类型查询 ===");

        // 1. 准备测试数据
        dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_1, TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);
        dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_2, TEST_MYSQL_ID_2, TEST_BUS_TYPE_2);

        // 2. 测试正常查询
        Optional<DataMigrationMappingEntity> result1 = dataMigrationMappingService.findByMysqlIdAndType(TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);
        assertTrue(result1.isPresent(), "应该能找到对应的映射记录");
        assertEquals(TEST_PGSQL_ID_1, result1.get().getPgsqlId(), "PostgreSQL ID应该匹配");
        assertEquals(TEST_MYSQL_ID_1, result1.get().getMysqlId(), "MySQL ID应该匹配");
        assertEquals(TEST_BUS_TYPE_1.getCode(), result1.get().getBusType(), "业务类型应该匹配");
        log.info("✅ 正常查询测试通过");

        // 3. 测试查询不存在的记录
        Optional<DataMigrationMappingEntity> result2 = dataMigrationMappingService.findByMysqlIdAndType(9999L, TEST_BUS_TYPE_1);
        assertFalse(result2.isPresent(), "不存在的记录应该返回空Optional");
        log.info("✅ 查询不存在记录测试通过");

        // 4. 测试参数验证
        Optional<DataMigrationMappingEntity> result3 = dataMigrationMappingService.findByMysqlIdAndType(null, TEST_BUS_TYPE_1);
        assertFalse(result3.isPresent(), "MySQL ID为null时应该返回空Optional");

        Optional<DataMigrationMappingEntity> result4 = dataMigrationMappingService.findByMysqlIdAndType(TEST_MYSQL_ID_1, null);
        assertFalse(result4.isPresent(), "业务类型为null时应该返回空Optional");
        log.info("✅ 参数验证测试通过");
    }

    /**
     * 测试存在性检查功能
     */
    @Test
    void testExistsCheck() {
        log.info("=== 测试存在性检查功能 ===");

        // 1. 准备测试数据
        dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_1, TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);

        // 2. 测试根据PostgreSQL ID检查存在性
        boolean exists1 = dataMigrationMappingService.existsByPgsqlIdAndType(TEST_PGSQL_ID_1, TEST_BUS_TYPE_1);
        assertTrue(exists1, "已保存的记录应该存在");

        boolean exists2 = dataMigrationMappingService.existsByPgsqlIdAndType("non_exist", TEST_BUS_TYPE_1);
        assertFalse(exists2, "不存在的记录应该返回false");

        boolean exists3 = dataMigrationMappingService.existsByPgsqlIdAndType(null, TEST_BUS_TYPE_1);
        assertFalse(exists3, "PostgreSQL ID为null时应该返回false");
        log.info("✅ PostgreSQL ID存在性检查测试通过");

        // 3. 测试根据MySQL ID检查存在性
        boolean exists4 = dataMigrationMappingService.existsByMysqlIdAndType(TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);
        assertTrue(exists4, "已保存的记录应该存在");

        boolean exists5 = dataMigrationMappingService.existsByMysqlIdAndType(9999L, TEST_BUS_TYPE_1);
        assertFalse(exists5, "不存在的记录应该返回false");

        boolean exists6 = dataMigrationMappingService.existsByMysqlIdAndType(null, TEST_BUS_TYPE_1);
        assertFalse(exists6, "MySQL ID为null时应该返回false");
        log.info("✅ MySQL ID存在性检查测试通过");
    }

    /**
     * 测试删除操作
     */
    @Test
    void testDeleteOperations() {
        log.info("=== 测试删除操作 ===");

        // 1. 准备测试数据
        dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_1, TEST_MYSQL_ID_1, TEST_BUS_TYPE_1);
        dataMigrationMappingService.saveMapping(TEST_PGSQL_ID_2, TEST_MYSQL_ID_2, TEST_BUS_TYPE_2);

        // 2. 测试根据PostgreSQL ID删除
        boolean deleted1 = dataMigrationMappingService.deleteByPgsqlIdAndType(TEST_PGSQL_ID_1, TEST_BUS_TYPE_1);
        assertTrue(deleted1, "删除操作应该成功");

        // 验证删除结果
        boolean exists1 = dataMigrationMappingService.existsByPgsqlIdAndType(TEST_PGSQL_ID_1, TEST_BUS_TYPE_1);
        assertFalse(exists1, "删除后记录应该不存在");
        log.info("✅ PostgreSQL ID删除测试通过");

        // 3. 测试根据MySQL ID删除
        boolean deleted2 = dataMigrationMappingService.deleteByMysqlIdAndType(TEST_MYSQL_ID_2, TEST_BUS_TYPE_2);
        assertTrue(deleted2, "删除操作应该成功");

        // 验证删除结果
        boolean exists2 = dataMigrationMappingService.existsByMysqlIdAndType(TEST_MYSQL_ID_2, TEST_BUS_TYPE_2);
        assertFalse(exists2, "删除后记录应该不存在");
        log.info("✅ MySQL ID删除测试通过");

        // 4. 测试删除不存在的记录
        boolean deleted3 = dataMigrationMappingService.deleteByPgsqlIdAndType("non_exist", TEST_BUS_TYPE_1);
        assertFalse(deleted3, "删除不存在的记录应该返回false");

        // 5. 测试参数验证
        boolean deleted4 = dataMigrationMappingService.deleteByPgsqlIdAndType(null, TEST_BUS_TYPE_1);
        assertFalse(deleted4, "PostgreSQL ID为null时应该返回false");

        boolean deleted5 = dataMigrationMappingService.deleteByMysqlIdAndType(null, TEST_BUS_TYPE_1);
        assertFalse(deleted5, "MySQL ID为null时应该返回false");
        log.info("✅ 删除参数验证测试通过");
    }

    /**
     * 测试批量保存功能
     */
    @Test
    void testSaveBatchMappings() {
        log.info("=== 测试批量保存功能 ===");

        // 1. 准备测试数据
        List<DataMigrationMappingEntity> mappings = new ArrayList<>();
        
        DataMigrationMappingEntity mapping1 = new DataMigrationMappingEntity();
        mapping1.setPgsqlId("batch_test_001");
        mapping1.setMysqlId(2001L);
        mapping1.setBusType(MigrationBusTypeEnum.JOB.getCode());
        mapping1.setCreateTime(LocalDateTime.now());
        mappings.add(mapping1);

        DataMigrationMappingEntity mapping2 = new DataMigrationMappingEntity();
        mapping2.setPgsqlId("batch_test_002");
        mapping2.setMysqlId(2002L);
        mapping2.setBusType(MigrationBusTypeEnum.CANDIDATE.getCode());
        mapping2.setCreateTime(LocalDateTime.now());
        mappings.add(mapping2);

        // 2. 测试批量保存
        int successCount = dataMigrationMappingService.saveBatchMappings(mappings);
        assertEquals(2, successCount, "批量保存应该成功保存2条记录");
        log.info("✅ 批量保存测试通过");

        // 3. 验证保存结果
        boolean exists1 = dataMigrationMappingService.existsByPgsqlIdAndType("batch_test_001", MigrationBusTypeEnum.JOB);
        assertTrue(exists1, "第一条记录应该存在");

        boolean exists2 = dataMigrationMappingService.existsByPgsqlIdAndType("batch_test_002", MigrationBusTypeEnum.CANDIDATE);
        assertTrue(exists2, "第二条记录应该存在");
        log.info("✅ 批量保存结果验证通过");

        // 4. 测试空列表
        int emptyResult = dataMigrationMappingService.saveBatchMappings(new ArrayList<>());
        assertEquals(0, emptyResult, "空列表应该返回0");

        int nullResult = dataMigrationMappingService.saveBatchMappings(null);
        assertEquals(0, nullResult, "null列表应该返回0");
        log.info("✅ 批量保存边界情况测试通过");
    }

    /**
     * 测试按类型查询和统计功能
     */
    @Test
    void testFindAllByTypeAndCount() {
        log.info("=== 测试按类型查询和统计功能 ===");

        // 1. 准备测试数据
        dataMigrationMappingService.saveMapping("type_test_001", 3001L, MigrationBusTypeEnum.JOB);
        dataMigrationMappingService.saveMapping("type_test_002", 3002L, MigrationBusTypeEnum.JOB);
        dataMigrationMappingService.saveMapping("type_test_003", 3003L, MigrationBusTypeEnum.CANDIDATE);

        // 2. 测试按类型查询
        List<DataMigrationMappingEntity> jobMappings = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.JOB);
        assertTrue(jobMappings.size() >= 2, "JOB类型应该至少有2条记录");
        log.info("✅ 按类型查询测试通过，JOB类型记录数: {}", jobMappings.size());

        List<DataMigrationMappingEntity> candidateMappings = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.CANDIDATE);
        assertTrue(candidateMappings.size() >= 1, "CANDIDATE类型应该至少有1条记录");
        log.info("✅ 按类型查询测试通过，CANDIDATE类型记录数: {}", candidateMappings.size());

        // 3. 测试统计功能
        long jobCount = dataMigrationMappingService.countByType(MigrationBusTypeEnum.JOB);
        assertTrue(jobCount >= 2, "JOB类型统计应该至少为2");
        log.info("✅ 统计功能测试通过，JOB类型统计: {}", jobCount);

        long candidateCount = dataMigrationMappingService.countByType(MigrationBusTypeEnum.CANDIDATE);
        assertTrue(candidateCount >= 1, "CANDIDATE类型统计应该至少为1");
        log.info("✅ 统计功能测试通过，CANDIDATE类型统计: {}", candidateCount);

        // 4. 测试参数验证
        List<DataMigrationMappingEntity> nullTypeList = dataMigrationMappingService.findAllByType(null);
        assertTrue(nullTypeList.isEmpty(), "null类型应该返回空列表");

        long nullTypeCount = dataMigrationMappingService.countByType(null);
        assertEquals(0L, nullTypeCount, "null类型统计应该返回0");
        log.info("✅ 按类型查询和统计参数验证测试通过");
    }

    /**
     * 测试逻辑删除功能
     */
    @Test
    void testLogicalDelete() {
        log.info("=== 测试逻辑删除功能 ===");

        // 1. 数据准备 - 保存测试记录
        String testPgsqlId = "logical_delete_test_001";
        Long testMysqlId = 5001L;
        MigrationBusTypeEnum testBusType = MigrationBusTypeEnum.JOB;

        boolean saved = dataMigrationMappingService.saveMapping(testPgsqlId, testMysqlId, testBusType);
        assertTrue(saved, "测试数据保存应该成功");
        log.info("✅ 测试数据准备完成");

        // 2. 验证记录存在
        assertTrue(dataMigrationMappingService.existsByPgsqlIdAndType(testPgsqlId, testBusType), "保存后记录应该存在");
        
        Optional<DataMigrationMappingEntity> beforeDelete = dataMigrationMappingService.findByPgsqlIdAndType(testPgsqlId, testBusType);
        assertTrue(beforeDelete.isPresent(), "删除前应该能查询到记录");
        assertEquals(testMysqlId, beforeDelete.get().getMysqlId(), "MySQL ID应该匹配");
        log.info("✅ 删除前数据验证通过");

        // 3. 执行逻辑删除操作
        boolean deleted = dataMigrationMappingService.deleteByPgsqlIdAndType(testPgsqlId, testBusType);
        assertTrue(deleted, "逻辑删除操作应该成功");
        log.info("✅ 逻辑删除操作执行完成");

        // 4. 验证逻辑删除效果
        // 4.1 查询应该返回空（被逻辑删除过滤）
        Optional<DataMigrationMappingEntity> afterDelete = dataMigrationMappingService.findByPgsqlIdAndType(testPgsqlId, testBusType);
        assertFalse(afterDelete.isPresent(), "逻辑删除后查询应该返回空，验证@TableLogic注解生效");
        log.info("✅ 逻辑删除后查询过滤验证通过");

        // 4.2 存在性检查应该返回false
        boolean existsAfterDelete = dataMigrationMappingService.existsByPgsqlIdAndType(testPgsqlId, testBusType);
        assertFalse(existsAfterDelete, "逻辑删除后存在性检查应该返回false");
        log.info("✅ 逻辑删除后存在性检查验证通过");

        // 4.3 反向查询也应该返回空
        Optional<DataMigrationMappingEntity> reverseQuery = dataMigrationMappingService.findByMysqlIdAndType(testMysqlId, testBusType);
        assertFalse(reverseQuery.isPresent(), "逻辑删除后反向查询也应该返回空");
        
        boolean reverseExists = dataMigrationMappingService.existsByMysqlIdAndType(testMysqlId, testBusType);
        assertFalse(reverseExists, "逻辑删除后反向存在性检查也应该返回false");
        log.info("✅ 逻辑删除后反向查询验证通过");

        // 4.4 验证按类型查询不包含已删除记录
        List<DataMigrationMappingEntity> typeList = dataMigrationMappingService.findAllByType(testBusType);
        boolean containsDeleted = typeList.stream()
                .anyMatch(entity -> testPgsqlId.equals(entity.getPgsqlId()) && testMysqlId.equals(entity.getMysqlId()));
        assertFalse(containsDeleted, "按类型查询结果不应该包含已逻辑删除的记录");
        log.info("✅ 按类型查询逻辑删除过滤验证通过");

        // 5. 测试双向删除的一致性
        String testPgsqlId2 = "logical_delete_test_002";
        Long testMysqlId2 = 5002L;
        
        // 保存第二条测试记录
        dataMigrationMappingService.saveMapping(testPgsqlId2, testMysqlId2, testBusType);
        assertTrue(dataMigrationMappingService.existsByMysqlIdAndType(testMysqlId2, testBusType), "第二条记录应该存在");

        // 通过MySQL ID删除
        boolean deletedByMysqlId = dataMigrationMappingService.deleteByMysqlIdAndType(testMysqlId2, testBusType);
        assertTrue(deletedByMysqlId, "通过MySQL ID删除应该成功");

        // 验证双向查询都返回空
        assertFalse(dataMigrationMappingService.existsByPgsqlIdAndType(testPgsqlId2, testBusType), 
                "通过MySQL ID删除后，PostgreSQL ID查询应该返回false");
        assertFalse(dataMigrationMappingService.existsByMysqlIdAndType(testMysqlId2, testBusType), 
                "通过MySQL ID删除后，MySQL ID查询应该返回false");
        log.info("✅ 双向删除一致性验证通过");

        log.info("=== 逻辑删除功能测试完成 ===");
    }

    /**
     * 测试业务场景综合流程
     */
    @Test
    void testBusinessScenario() {
        log.info("=== 测试业务场景综合流程 ===");

        // 1. 模拟数据迁移场景：保存多个业务类型的映射
        String pgsqlJobId = "job_scenario_001";
        Long mysqlJobId = 4001L;
        String pgsqlCandidateId = "candidate_scenario_001";
        Long mysqlCandidateId = 4002L;

        // 保存职位映射
        boolean jobSaved = dataMigrationMappingService.saveMapping(pgsqlJobId, mysqlJobId, MigrationBusTypeEnum.JOB);
        assertTrue(jobSaved, "职位映射保存应该成功");

        // 保存候选人映射
        boolean candidateSaved = dataMigrationMappingService.saveMapping(pgsqlCandidateId, mysqlCandidateId, MigrationBusTypeEnum.CANDIDATE);
        assertTrue(candidateSaved, "候选人映射保存应该成功");
        log.info("✅ 数据迁移映射保存完成");

        // 2. 模拟查询场景：根据PostgreSQL ID查找MySQL ID
        Optional<DataMigrationMappingEntity> jobMapping = dataMigrationMappingService.findByPgsqlIdAndType(pgsqlJobId, MigrationBusTypeEnum.JOB);
        assertTrue(jobMapping.isPresent(), "应该能找到职位映射");
        assertEquals(mysqlJobId, jobMapping.get().getMysqlId(), "MySQL职位ID应该匹配");

        Optional<DataMigrationMappingEntity> candidateMapping = dataMigrationMappingService.findByPgsqlIdAndType(pgsqlCandidateId, MigrationBusTypeEnum.CANDIDATE);
        assertTrue(candidateMapping.isPresent(), "应该能找到候选人映射");
        assertEquals(mysqlCandidateId, candidateMapping.get().getMysqlId(), "MySQL候选人ID应该匹配");
        log.info("✅ 双向查询验证完成");

        // 3. 模拟更新场景：更新映射关系
        Long newMysqlJobId = 4003L;
        boolean jobUpdated = dataMigrationMappingService.saveMapping(pgsqlJobId, newMysqlJobId, MigrationBusTypeEnum.JOB);
        assertTrue(jobUpdated, "职位映射更新应该成功");

        Optional<DataMigrationMappingEntity> updatedJobMapping = dataMigrationMappingService.findByPgsqlIdAndType(pgsqlJobId, MigrationBusTypeEnum.JOB);
        assertTrue(updatedJobMapping.isPresent(), "更新后应该能找到职位映射");
        assertEquals(newMysqlJobId, updatedJobMapping.get().getMysqlId(), "MySQL职位ID应该已更新");
        log.info("✅ 映射更新验证完成");

        // 4. 模拟清理场景：删除映射关系
        boolean jobDeleted = dataMigrationMappingService.deleteByPgsqlIdAndType(pgsqlJobId, MigrationBusTypeEnum.JOB);
        assertTrue(jobDeleted, "职位映射删除应该成功");

        boolean candidateDeleted = dataMigrationMappingService.deleteByMysqlIdAndType(mysqlCandidateId, MigrationBusTypeEnum.CANDIDATE);
        assertTrue(candidateDeleted, "候选人映射删除应该成功");

        // 验证删除结果
        boolean jobExists = dataMigrationMappingService.existsByPgsqlIdAndType(pgsqlJobId, MigrationBusTypeEnum.JOB);
        assertFalse(jobExists, "删除后职位映射应该不存在");

        boolean candidateExists = dataMigrationMappingService.existsByMysqlIdAndType(mysqlCandidateId, MigrationBusTypeEnum.CANDIDATE);
        assertFalse(candidateExists, "删除后候选人映射应该不存在");
        log.info("✅ 映射清理验证完成");

        log.info("=== 业务场景综合流程测试完成 ===");
    }
}
