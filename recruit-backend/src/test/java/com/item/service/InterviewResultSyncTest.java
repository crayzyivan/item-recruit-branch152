package com.item.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.item.entity.AiVettedResultEntity;
import com.item.entity.ApplicationInterviewReportsEntity;
import com.item.entity.ApplicationScreeningReportsEntity;
import com.item.entity.ApplicationsEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.config.S3Config;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.pgmapper.PgApplicationInterviewReportsMapper;
import com.item.service.migration.DataMigrationMappingService;
import com.item.util.S3Utils;
import com.item.util.VideoUtils;
import com.item.vo.ApplicationsSyncVO;
import com.item.vo.InterviewReportVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试面试结果同步
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-29  16:14
 */
@Slf4j
@SpringBootTest
public class InterviewResultSyncTest {
    @Resource
    private S3Config s3Config;
    @Resource
    private InterviewResultSyncService interviewResultSyncService;
    @Resource
    private S3Utils s3Utils;
    @Resource
    private VideoUtils videoUtils;
    @Resource
    private ApplicationInterviewReportsService  applicationInterviewReportsService;
    @Resource
    private CandidateJobDomainService candidateJobDomainService;
    @Resource
    private PgApplicationInterviewReportsMapper interviewReportsMapper;
    @Resource
    private AiVettedResultService aiVettedResultService;
    @Resource
    private ApplicationsService applicationsService;
    @Resource
    private DataMigrationMappingService dataMigrationMappingService;
    @Resource
    private ApplicationScreeningReportsService applicationScreeningReportsService;
    @Resource
    private AiScreeningSyncService aiScreeningSyncService;

    @Test
    public void test1() {
        interviewResultSyncService.syncInterviewResultFromPg(595L,"ec4d33d8-d455-4432-817e-f563fefd751f");
    }

    @Test
    public void init(){
        List<ApplicationInterviewReportsEntity> list = applicationInterviewReportsService.listAll();
        for (ApplicationInterviewReportsEntity reportsEntity:list){
            try{
                interviewResultSyncService.syncInterviewResultFromPg(reportsEntity.getId(),reportsEntity.getApplicationId());
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }

    @Test
    public void upload() throws IOException {
        File file =new File("C:\\Users\\yunlong.li\\Desktop\\video.mp4");
        byte[] videoContent = Files.readAllBytes(file.toPath()) ;
        // 2. 生成S3文件名
        String fileName = videoUtils.generateVideoFileName("1");
        // 3. 上传到S3
        String s3Key = s3Utils.uploadFromByteArray(videoContent, fileName, "video/mp4");
        log.info("========================:"+s3Key);
    }



    @Test
    public void isS3KeyExists() {
        boolean s3KeyExists = s3Utils.isS3KeyExists("recruit/PHL_interview_video_1.mp4");
        log.info("=====================:"+s3KeyExists);
    }

    @Test
    public void getS3Key() {
        String videoFileName = videoUtils.generateVideoFileName("15b0ca3c-71b7-4e60-a5cb-a042bf3733b4");
        String s3Key = s3Utils.getS3Key(videoFileName);
        log.info("=====================:"+s3Key);
    }

    @Test
    public void generatePresignedUrl(){
        String url = s3Utils.generatePresignedUrl("recruit/prod/PHL_interview_report_ec4d33d8-d455-4432-817e-f563fefd751f.pdf", 3600 * 24);
        log.info("=================:"+url);
    }



//    @Test
//    public void generateStreamingVideoUrl(){
//        String url = s3Utils.generateStreamingVideoUrl("recruit/PHL_interview_video_15b0ca3c-71b7-4e60-a5cb-a042bf3733b4.mp4", 3600 * 24);
//        log.info("=================:"+url);
//    }


    @Test
    public void getInterviewReport(){
        long startTime = System.currentTimeMillis();
        InterviewReportVO interviewReport = candidateJobDomainService.getInterviewReport(859L);
        log.info("time={}",(System.currentTimeMillis()-startTime));
        log.info("interviewReport={}",interviewReport);

    }

    @Test
    public void uploadVideoS3(){
        LambdaQueryWrapper<ApplicationInterviewReportsEntity>  wrapper = new LambdaQueryWrapper<>();
//        wrapper.gt(ApplicationInterviewReportsEntity::getCreatedOn,
//                OffsetDateTime.of(2025, , 1, 0, 0, 0, 0, ZoneOffset.UTC));
//        wrapper.lt(ApplicationInterviewReportsEntity::getCreatedOn,
//                OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        wrapper.orderByDesc(ApplicationInterviewReportsEntity::getCreatedOn);
        List<ApplicationInterviewReportsEntity> list = interviewReportsMapper.selectList(wrapper);
        for (ApplicationInterviewReportsEntity source:list){
            //转换视频地址
            String videoFileName = videoUtils.generateVideoFileName(source.getApplicationId());
            String s3Key = s3Utils.getS3Key(videoFileName);
            if (!s3Utils.isS3KeyExists(s3Key)){
                try{
                    //interviewResultSyncService.downloadAndUploadVideoToS3(source.getInterviewRecordingUrl(),source.getId(),source.getApplicationId());
                    log.info("=====================:"+source.getId());
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        }



    }


    @Test
    public void deletePdfReport(){
//        List<AiVettedResultEntity> list = aiVettedResultService.list(new LambdaQueryWrapper<>());
//        for (AiVettedResultEntity result:list){
//            log.info("result============={}",result.getInterviewReportUrl());
//            s3Utils.deleteFile(result.getInterviewReportUrl());
//        }

        s3Utils.deleteFile("recruit/dev/PHL_interview_report_ec4d33d8-d455-4432-817e-f563fefd751f.pdf");
    }


    @Test
    public void uploadVideo() throws IOException {
        String applicationId="6bb4c9e7-83f5-45ce-b71b-795b7460fb48";
        File file=new File("D:\\download\\6bb4c9e7-83f5-45ce-b71b-795b7460fb48_compressed.mp4");
        byte[] mp4Content = Files.readAllBytes(file.toPath());
        // 2. 生成S3文件名
        String fileName = videoUtils.generateVideoFileName(applicationId);
        // 3. 上传到S3
        String s3Key = s3Utils.uploadFromByteArray(mp4Content, fileName, "video/mp4");
        log.info("============================s3Key:{}",s3Key);
    }



    @Test
    public void applicationNotSync(){
        List<ApplicationsEntity> applicationsEntityList = applicationsService.list();
        List<DataMigrationMappingEntity> candidateJobList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.CANDIDATE_JOB);
        List<DataMigrationMappingEntity> jobList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.JOB);
        List<DataMigrationMappingEntity> candidateList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.CANDIDATE);

        List<String> pgCandidateJobIds=candidateJobList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();
        List<String> pgJobIds=jobList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();
        List<String> pgCandidateIds=candidateList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();

        for (ApplicationsEntity  applicationsEntity:applicationsEntityList){
            if (!pgCandidateJobIds.contains(applicationsEntity.getId())){
                String pgJobId=applicationsEntity.getJobId().toString();
                if (pgJobIds.contains(pgJobId) && pgCandidateIds.contains(applicationsEntity.getCandidateId())){
                    log.info("applicationsEntity id={}",applicationsEntity.getId());
                }
            }
        }
    }

    @Test
    public void screenNotSync(){
        List<ApplicationScreeningReportsEntity> allApplicationIds= applicationScreeningReportsService.listBySyncVo(new ApplicationsSyncVO());
        List<DataMigrationMappingEntity> candidateJobList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.CANDIDATE_JOB);
        List<DataMigrationMappingEntity> screenList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.SCREENING_REPORTS);

        List<String> pgIds=screenList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();
        List<String> pgCandidateJobIds=candidateJobList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();

        for (ApplicationScreeningReportsEntity reportsEntity:allApplicationIds){
            if (!pgIds.contains(String.valueOf(reportsEntity.getId()))){
                if (pgCandidateJobIds.contains(reportsEntity.getApplicationId())){
                    log.info("applicationId={}",reportsEntity.getApplicationId());
                }
            }
        }
    }

    @Test
    public void aiScreeningSync(){
        ApplicationsSyncVO applicationsSyncVO = new ApplicationsSyncVO();
        applicationsSyncVO.setApplicationIds(Arrays.asList("b0255000-3f27-480b-bf5e-7305349a1e46"));
        boolean b = aiScreeningSyncService.aiScreeningSync(applicationsSyncVO);
    }



    @Test
    public void interviewResultNotSync(){
        List<ApplicationInterviewReportsEntity> list = applicationInterviewReportsService.listAll();
        List<DataMigrationMappingEntity> candidateJobList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.CANDIDATE_JOB);
        List<DataMigrationMappingEntity> interviewList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.INTERVIEW_REPORTS);

        List<String> pgApplicationIds=interviewList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();
        List<String> pgCandidateJobIds=candidateJobList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();

        for (ApplicationInterviewReportsEntity reportsEntity:list){
            if (!pgApplicationIds.contains(reportsEntity.getApplicationId())){
                if (pgCandidateJobIds.contains(reportsEntity.getApplicationId())){
                    log.info("applicationId={}",reportsEntity.getApplicationId());
                }
            }
        }
    }




}