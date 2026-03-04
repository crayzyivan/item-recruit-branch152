package com.item.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.item.framework.config.S3Config;
import com.item.framework.constant.ContentTypeEnum;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Component
@Slf4j
public class S3Utils {
    private final AmazonS3 s3Client;

    private static final String NAME_SEPARATOR = "_";

    private static final String FILE_SEPARATOR = "/";

    private final S3Config s3Config;

    public S3Utils(S3Config s3Config) {
        this.s3Config = s3Config;
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(s3Config.getRegion())
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(s3Config.getAccessKey(), s3Config.getSecretKey())))
                .build();
    }

    /**
     * 上传文件（MultipartFile）
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    /**
     * 上传文件（MultipartFile），可指定ContentType
     */
    public String uploadFile( MultipartFile file, String contentType) {
        String bucketName = s3Config.getBucketName();
        String fileName = s3Config.getFolder() + FILE_SEPARATOR + UUID.randomUUID() + NAME_SEPARATOR + file.getOriginalFilename();
        try (InputStream is = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            s3Client.putObject(bucketName, fileName, is, metadata);
            return this.getkey(s3Client.getUrl(bucketName, fileName).toString());
        } catch (Exception e) {
            log.error("Failed to upload file from S3: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED,"Failed to upload file from S3");
        }
    }

    public String uploadFileFixedFileName(MultipartFile file) {
        return uploadFileFixedFileName(file, null);
    }
    /**
     * 上传文件（MultipartFile），可指定ContentType 固定文件名
     */
    public String uploadFileFixedFileName(MultipartFile file, String contentType) {
        String bucketName = s3Config.getBucketName();
        String fileName = s3Config.getFolder() + FILE_SEPARATOR + file.getOriginalFilename();
        try (InputStream is = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            s3Client.putObject(bucketName, fileName, is, metadata);
            return this.getkey(s3Client.getUrl(bucketName, fileName).toString());
        } catch (Exception e) {
            log.error("uploadFileFixedFileName failed to upload file from S3: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED,"Failed to upload file from S3");
        }
    }

    public String getkey(String url){
        return url.substring(url.indexOf(s3Config.getFolder()));
    }

    /**
     * 上传文件（File）
     */
    public String uploadFile( File file) {
        return uploadFile(file, null);
    }

    /**
     * 上传文件（File），可指定ContentType
     */
    public String uploadFile(File file, String contentType) {
        String bucketName = s3Config.getBucketName();
        String fileName = s3Config.getFolder() + FILE_SEPARATOR + file.getName();
        try (InputStream is = new FileInputStream(file)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            s3Client.putObject(bucketName, fileName, is, metadata);
            return this.getkey(s3Client.getUrl(bucketName, fileName).toString());
        } catch (Exception e) {
            log.error("Failed to upload file from S3: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED,"Failed to upload file from S3");
        }
    }

    /**
     * 上传文件（InputStream），自动检测ContentType
     * @param is 输入流
     * @param fileName 文件名
     * @return S3文件key
     */
    public String uploadFile(InputStream is, String fileName) {
        return uploadFile(is, fileName, null);
    }

    /**
     * 上传文件（InputStream），可指定ContentType
     * @param is 输入流
     * @param fileName 文件名
     * @param contentType 内容类型，为null时自动检测
     * @return S3文件key
     */
    public String uploadFile(InputStream is, String fileName, String contentType) {
        return uploadFile(is, fileName, contentType, null);
    }

    /**
     * 上传文件（InputStream），完整参数版本
     * @param is 输入流
     * @param fileName 文件名
     * @param contentType 内容类型，为null时自动检测
     * @param contentLength 内容长度，为null时尝试从流中获取
     * @return S3文件key
     */
    public String uploadFile(InputStream is, String fileName, String contentType, Long contentLength) {
        // 参数验证
        if (is == null) {
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR, "InputStream cannot be null");
        }
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR, "FileName cannot be empty");
        }

        String bucketName = s3Config.getBucketName();
        String s3Key = s3Config.getFolder() + FILE_SEPARATOR + fileName;

        try {
            // 构建元数据
            ObjectMetadata metadata = new ObjectMetadata();

            // 设置内容类型
            if (StringUtils.hasText(contentType)) {
                metadata.setContentType(contentType);
            } else {
                // 自动检测内容类型
                String detectedContentType = ContentTypeEnum.fromFileName(fileName).getMimeType();
                metadata.setContentType(detectedContentType);
                log.debug("Auto-detected content type for file {}: {}", fileName, detectedContentType);
            }

            // 设置内容长度（如果提供）
            if (contentLength != null && contentLength > 0) {
                metadata.setContentLength(contentLength);
                log.debug("Set content length for file {}: {} bytes", fileName, contentLength);
            }
            // 确保 S3 支持 Range 请求
            metadata.addUserMetadata("Cache-Control", "public, max-age=86400");
            // 上传文件
            log.info("Uploading file to S3: bucket={}, key={}", bucketName, s3Key);
            s3Client.putObject(bucketName, s3Key, is, metadata);

            String resultKey = this.getkey(s3Client.getUrl(bucketName, s3Key).toString());
            log.info("Successfully uploaded file to S3: {}", resultKey);

            return resultKey;

        } catch (Exception e) {
            log.error("Failed to upload file to S3: fileName={}, error={}", fileName, e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED,
                    "Failed to upload file to S3: " + e.getMessage());
        }
    }

    /**
     * 优化版：从ByteArray直接上传到S3
     * 适用于已经在内存中的文件内容，避免临时文件
     *
     * @param fileContent 文件内容字节数组
     * @param fileName 文件名
     * @return S3文件key
     */
    public String uploadFromByteArray(byte[] fileContent, String fileName) {
        return uploadFromByteArray(fileContent, fileName, null);
    }
    
    /**
     * 优化版：从ByteArray直接上传到S3，可指定ContentType
     * 
     * @param fileContent 文件内容字节数组
     * @param fileName 文件名
     * @param contentType 内容类型，为null时自动检测
     * @return S3文件key
     */
    public String uploadFromByteArray(byte[] fileContent, String fileName, String contentType) {
        // 参数验证
        if (fileContent == null || fileContent.length == 0) {
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR, "File content cannot be null or empty");
        }
        
        try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
            return uploadFile(inputStream, fileName, contentType, (long) fileContent.length);
        } catch (Exception e) {
            log.error("Failed to upload file from byte array: fileName={}", fileName, e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED, 
                    "Failed to upload file from byte array: " + e.getMessage());
        }
    }


    /**
     * 下载文件，返回InputStream（需手动关闭）
     */
    public InputStream downloadFile( String key) {
        try {
            S3Object s3Object = s3Client.getObject(s3Config.getBucketName(), key);
            return s3Object.getObjectContent();
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_DOWNLOAD_FAILED,"Failed to download file from S3");
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile( String key) {
        try {
            s3Client.deleteObject(s3Config.getBucketName(), key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_DELETE_FAILED,"Failed to delete file from S3");
        }
    }

    /**
     * 生成预签名URL 可预览（默认1小时）
     */
    public String generatePresignedUrl( String key) {
        return generatePresignedUrl(key, 3600);
    }

    /**
     * 生成预签名URL 直接下载（默认1小时）
     */
    public String generateDownloadUrl( String key) {
        return generateDownloadUrl(key, 3600);
    }

    /**
     * 生成预签名URL 可预览（自定义过期秒数）
     */
    public String generatePresignedUrl(String key, int expireSeconds) {
        if (!StringUtils.hasText(key)) {
            return "";
        }
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            // 3. 设置请求头和选项
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(s3Config.getBucketName(), URLDecoder.decode(key, StandardCharsets.UTF_8))
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            // 添加 x-amz-checksum-mode 参数（启用校验和验证）
            request.addRequestParameter("response-content-disposition",
                    "inline; filename=\"" + URLEncoder.encode(key.substring(key.indexOf("/")), StandardCharsets.UTF_8) + "\"");
            request.addRequestParameter("x-id", "GetObject");
            request.addRequestParameter("x-amz-checksum-mode", "ENABLED");
            request.addRequestParameter("host", "unis-stage-data.s3.us-west-2.amazonaws.com");
            request.addRequestParameter("response-content-type", ContentTypeEnum.fromFileName(key).getMimeType());

            URL url = s3Client.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            log.error("Failed to generate a pre-signed URL for S3: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.URL_GENERATION_FAILED,"Failed to generate a pre-signed URL for S3");
        }
    }

    /**
     * 生成预签名URL 下载（自定义过期秒数）
     */
    public String generateDownloadUrl(String key, int expireSeconds) {
        if (!StringUtils.hasText(key)) {
            return "";
        }
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            // 3. 设置请求头和选项
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(s3Config.getBucketName(), URLDecoder.decode(key, StandardCharsets.UTF_8))
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            log.error("Failed to generate a pre-signed URL for S3: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.URL_GENERATION_FAILED,"Failed to generate a pre-signed URL for S3");
        }
    }

    /**
     * 判断文件是否存在
     */
    public boolean doesObjectExist(String key) {
        try {
            return s3Client.doesObjectExist(s3Config.getBucketName(), key);
        } catch (Exception e) {
            log.error("Failed to check if the file exists in S3: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.CHECK_FILE_FAILED,"Failed to check if the file exists in S3");
        }
    }

    /**
     * 上传XML文件到S3
     */
    public String uploadFeedXmlToS3(String feedXmlNamePrefix, String companyCode,String xmlContent) {
        try {
            log.info("Uploading XML to S3: {}", feedXmlNamePrefix);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/xml");
            metadata.setContentLength(xmlContent.getBytes(StandardCharsets.UTF_8).length);
            // 组合 S3 中的完整 Key（folder + file）
            String s3Key=getFeedXmlKey(feedXmlNamePrefix,companyCode);
            // 上传
            s3Client.putObject(s3Config.getBucketName(), s3Key,
                    new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)), metadata);
            // 获取 S3 对象的 URL
            URL urlObj = s3Client.getUrl(s3Config.getBucketName(), s3Key);
            String url = urlObj.toString();
            log.info("XML uploaded successfully to S3 key:{}, url:{}", s3Key, url);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload XML to S3: {}", feedXmlNamePrefix, e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED, "Failed to upload XML to S3");
        }
    }

    /**
     * 从S3下载XML文件
     */
    public String getFeedXmlFromS3(String feedXmlNamePrefix,String companyCode) {
        try {
            // 拼接 S3 上完整的 key（如果上传时有 folder 前缀）
            String s3Key=getFeedXmlKey(feedXmlNamePrefix,companyCode);

            log.info("Fetching XML from S3: {}", s3Key);

            S3Object s3Object = s3Client.getObject(s3Config.getBucketName(), s3Key);
            try (InputStream inputStream = s3Object.getObjectContent()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to fetch XML from S3: {}", feedXmlNamePrefix, e);
            throw new BusinessException(GlobalStatusCode.FILE_DOWNLOAD_FAILED, "Failed to fetch XML from S3");
        }
    }

    /**
     * 获取S3中的XML文件的Key
     */
    private String getFeedXmlKey(String feedXmlNamePrefix, String companyCode){
        if (StringUtils.hasText(companyCode)){
            String feedCompanyXmlName="";
            String[] parts = feedXmlNamePrefix.split("\\.", 2); // 按第一个点分割成 [文件名, 扩展名]
            if (parts.length == 2) {
                feedCompanyXmlName = parts[0] + "_" + companyCode + "." + parts[1];
            } else {
                // 没有扩展名的情况
                feedCompanyXmlName = feedXmlNamePrefix + "_" + companyCode;
            }
            return s3Config.getFolder() + FILE_SEPARATOR + feedCompanyXmlName;
        } else{
            return s3Config.getFolder() + FILE_SEPARATOR + feedXmlNamePrefix;
        }
    }

    /**
     * 判断S3文件是否存在
     *
     * @param s3Key S3文件key
     * @return true-存在，false-不存在
     */
    public boolean isS3KeyExists(String s3Key) {
        if (!StringUtils.hasText(s3Key)) {
            return false;
        }
        try {
            boolean exists = doesObjectExist(s3Key);
            return exists;
        } catch (Exception e) {
            log.error("Failed to check S3 key existence: key={}, error={}", s3Key, e.getMessage(), e);
            return false;
        }
    }

    public String getS3Key (String fileName){
        String bucketName = s3Config.getBucketName();
        String s3Key = s3Config.getFolder() + FILE_SEPARATOR + fileName;
        String resultKey = this.getkey(s3Client.getUrl(bucketName, s3Key).toString());
        return resultKey;
    }

    /**
     * 原地更新S3文件内容
     * @param key S3文件key
     * @param newContent 新的文件内容
     * @param contentType 内容类型
     * @return 是否更新成功
     */
    public boolean updateFileInPlace(String key, byte[] newContent, String contentType) {
        if (!StringUtils.hasText(key) || newContent == null) {
            return false;
        }
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(newContent.length);
            metadata.setContentType(contentType);
            metadata.setCacheControl("no-cache");
            PutObjectRequest request = new PutObjectRequest(
                    s3Config.getBucketName(),
                    key,
                    new ByteArrayInputStream(newContent),
                    metadata
            );
            s3Client.putObject(request);
            return true;
        } catch (Exception e) {
            log.error("Failed to update S3 file in place: {}", key, e);
            return false;
        }
    }

    /**
     * 从S3下载文件为字节数组
     * @param key S3文件key
     * @return 文件字节数组
     */
    public byte[] downloadFileAsBytes(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        try {
            S3Object s3Object = s3Client.getObject(s3Config.getBucketName(), key);
            try (S3ObjectInputStream inputStream = s3Object.getObjectContent();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", key, e);
            return null;
        }
    }

}