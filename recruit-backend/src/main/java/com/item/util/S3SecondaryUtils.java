package com.item.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.item.framework.config.S3SecondaryConfig;
import com.item.framework.constant.ContentTypeEnum;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * S3第二数据源工具类
 * 专门用于操作第二个S3存储服务
 * 
 * @author system
 */
@Component
@Slf4j
public class S3SecondaryUtils {
    private final AmazonS3 s3Client;

    private static final String NAME_SEPARATOR = "_";

    private static final String FILE_SEPARATOR = "/";
    
    // MIME类型常量
    private static final String MIME_TYPE_PDF = "application/pdf";
    private static final String MIME_TYPE_JPEG = "image/jpeg";
    private static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
    private static final String MIME_TYPE_CSV = "text/csv";
    private static final String MIME_TYPE_BINARY = "application/octet-stream";

    private final S3SecondaryConfig s3SecondaryConfig;

    public S3SecondaryUtils(S3SecondaryConfig s3SecondaryConfig) {
        this.s3SecondaryConfig = s3SecondaryConfig;
        
        // 只有当第二数据源启用时才初始化客户端
        if (s3SecondaryConfig.isEnabled()) {
            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(s3SecondaryConfig.getRegion())
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(s3SecondaryConfig.getAccessKey(), s3SecondaryConfig.getSecretKey())))
                    .build();
            log.info("S3第二数据源客户端初始化成功，区域: {}, 存储桶: {}", 
                s3SecondaryConfig.getRegion(), s3SecondaryConfig.getBucketName());
        } else {
            this.s3Client = null;
            log.info("S3第二数据源未启用");
        }
    }

    /**
     * 检查第二数据源是否可用
     */
    private void checkSecondaryDataSourceEnabled() {
        if (!s3SecondaryConfig.isEnabled() || s3Client == null) {
            throw new BusinessException(GlobalStatusCode.SYSTEM_ERROR, "S3第二数据源未启用或配置错误");
        }
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
    public String uploadFile(MultipartFile file, String contentType) {
        checkSecondaryDataSourceEnabled();
        
        String bucketName = s3SecondaryConfig.getBucketName();
        String fileName = s3SecondaryConfig.getFolder() + FILE_SEPARATOR + UUID.randomUUID() + NAME_SEPARATOR + file.getOriginalFilename();
        try (InputStream is = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            s3Client.putObject(bucketName, fileName, is, metadata);
            return this.getkey(s3Client.getUrl(bucketName, fileName).toString());
        } catch (Exception e) {
            log.error("Failed to upload file to S3 secondary: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED,"Failed to upload file to S3 secondary");
        }
    }

    public String uploadFileFixedFileName(MultipartFile file) {
        return uploadFileFixedFileName(file, null);
    }
    
    /**
     * 上传文件（MultipartFile），可指定ContentType 固定文件名
     */
    public String uploadFileFixedFileName(MultipartFile file, String contentType) {
        checkSecondaryDataSourceEnabled();
        
        String bucketName = s3SecondaryConfig.getBucketName();
        String fileName = s3SecondaryConfig.getFolder() + FILE_SEPARATOR + file.getOriginalFilename();
        try (InputStream is = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            s3Client.putObject(bucketName, fileName, is, metadata);
            return this.getkey(s3Client.getUrl(bucketName, fileName).toString());
        } catch (Exception e) {
            log.error("uploadFileFixedFileName failed to upload file to S3 secondary: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED,"Failed to upload file to S3 secondary");
        }
    }

    public String getkey(String url){
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String folder = s3SecondaryConfig.getFolder();
        int folderIndex = url.indexOf(folder);
        if (folderIndex >= 0) {
            return url.substring(folderIndex);
        }
        return url; // 如果找不到folder，返回原始URL
    }

    /**
     * 上传文件（File）
     */
    public String uploadFile(File file) {
        return uploadFile(file, null);
    }

    /**
     * 上传文件（File），可指定ContentType
     */
    public String uploadFile(File file, String contentType) {
        checkSecondaryDataSourceEnabled();
        
        String bucketName = s3SecondaryConfig.getBucketName();
        String fileName = s3SecondaryConfig.getFolder() + FILE_SEPARATOR + file.getName();
        try (InputStream is = new FileInputStream(file)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            s3Client.putObject(bucketName, fileName, is, metadata);
            return this.getkey(s3Client.getUrl(bucketName, fileName).toString());
        } catch (Exception e) {
            log.error("Failed to upload file to S3 secondary: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED,"Failed to upload file to S3 secondary");
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
        checkSecondaryDataSourceEnabled();
        
        // 参数验证
        if (is == null) {
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR, "InputStream cannot be null");
        }
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR, "FileName cannot be empty");
        }

        String bucketName = s3SecondaryConfig.getBucketName();
        String s3Key = s3SecondaryConfig.getFolder() + FILE_SEPARATOR + fileName;

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

            // 上传文件
            log.info("Uploading file to S3 secondary: bucket={}, key={}", bucketName, s3Key);
            s3Client.putObject(bucketName, s3Key, is, metadata);

            String resultKey = this.getkey(s3Client.getUrl(bucketName, s3Key).toString());
            log.info("Successfully uploaded file to S3 secondary: {}", resultKey);

            return resultKey;

        } catch (Exception e) {
            log.error("Failed to upload file to S3 secondary: fileName={}, error={}", fileName, e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED,
                    "Failed to upload file to S3 secondary: " + e.getMessage());
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
        checkSecondaryDataSourceEnabled();
        
        // 参数验证
        if (fileContent == null || fileContent.length == 0) {
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR, "File content cannot be null or empty");
        }
        
        try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
            return uploadFile(inputStream, fileName, contentType, (long) fileContent.length);
        } catch (Exception e) {
            log.error("Failed to upload file from byte array to S3 secondary: fileName={}", fileName, e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED, 
                    "Failed to upload file from byte array to S3 secondary: " + e.getMessage());
        }
    }

    /**
     * 下载文件，返回InputStream（需手动关闭）
     */
    public InputStream downloadFile(String key) {
        checkSecondaryDataSourceEnabled();
        
        if (!StringUtils.hasText(key)) {
            log.error("Download file failed: key is null or empty");
            throw new BusinessException(GlobalStatusCode.PARAM_ERROR, "File key cannot be null or empty");
        }
        
        try {
            // 首先检查文件是否存在
            if (!doesObjectExist(key)) {
                log.error("Download file failed: file does not exist, key: {}", key);
                throw new BusinessException(GlobalStatusCode.NOT_FIND_FILE, 
                    "File not found in S3 secondary: " + key);
            }
            
            log.debug("Downloading file from S3 secondary: key={}", key);
            S3Object s3Object = s3Client.getObject(s3SecondaryConfig.getBucketName(), key);
            log.debug("Successfully downloaded file from S3 secondary: key={}", key);
            return s3Object.getObjectContent();
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("Failed to download file from S3 secondary: key={}, error={}", key, e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_DOWNLOAD_FAILED,
                "Failed to download file from S3 secondary: " + e.getMessage());
        }
    }

    /**
     * 安全下载文件，当文件不存在时返回null
     * 
     * @param key S3文件key
     * @return InputStream，如果文件不存在则返回null
     */
    public InputStream downloadFileSafely(String key) {
        checkSecondaryDataSourceEnabled();
        
        if (!StringUtils.hasText(key)) {
            log.warn("Download file safely failed: key is null or empty");
            return null;
        }
        
        try {
            // 检查文件是否存在
            if (!doesObjectExist(key)) {
                log.warn("Download file safely failed: file does not exist, key: {}", key);
                return null;
            }
            
            log.debug("Downloading file safely from S3 secondary: key={}", key);
            S3Object s3Object = s3Client.getObject(s3SecondaryConfig.getBucketName(), key);
            log.debug("Successfully downloaded file safely from S3 secondary: key={}", key);
            return s3Object.getObjectContent();
        } catch (Exception e) {
            log.error("Failed to download file safely from S3 secondary: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String key) {
        checkSecondaryDataSourceEnabled();
        
        try {
            s3Client.deleteObject(s3SecondaryConfig.getBucketName(), key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3 secondary: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FILE_DELETE_FAILED,"Failed to delete file from S3 secondary");
        }
    }

    /**
     * 生成预签名URL 可预览（默认1小时）
     */
    public String generatePresignedUrl(String key) {
        return generatePresignedUrl(key, 3600);
    }

    /**
     * 生成PDF文件的预签名URL（默认1小时）
     * 
     * @param key S3文件key
     * @return PDF预签名URL
     */
    public String generatePdfPresignedUrl(String key) {
        return generatePresignedUrl(key, 3600, MIME_TYPE_PDF);
    }

    /**
     * 生成PDF文件的预签名URL（自定义过期时间）
     * 
     * @param key S3文件key
     * @param expireSeconds 过期秒数
     * @return PDF预签名URL
     */
    public String generatePdfPresignedUrl(String key, int expireSeconds) {
        return generatePresignedUrl(key, expireSeconds, MIME_TYPE_PDF);
    }

    /**
     * 生成预签名URL 直接下载（默认1小时）
     */
    public String generateDownloadUrl(String key) {
        return generateDownloadUrl(key, 3600);
    }

    /**
     * 生成预签名URL 可预览（自定义过期秒数）
     */
    public String generatePresignedUrl(String key, int expireSeconds) {
        return generatePresignedUrl(key, expireSeconds, null);
    }

    /**
     * 生成预签名URL 可预览（自定义过期秒数和文件类型）
     * 
     * @param key S3文件key
     * @param expireSeconds 过期秒数
     * @param contentType 文件类型，如"application/pdf"，为null时自动检测
     * @return 预签名URL
     */
    public String generatePresignedUrl(String key, int expireSeconds, String contentType) {
        checkSecondaryDataSourceEnabled();
        
        if (!StringUtils.hasText(key)) {
            return "";
        }
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            // 3. 设置请求头和选项
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(s3SecondaryConfig.getBucketName(), URLDecoder.decode(key, StandardCharsets.UTF_8))
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            // 添加 x-amz-checksum-mode 参数（启用校验和验证）
            String fileName = key;
            int lastSlashIndex = key.lastIndexOf("/");
            if (lastSlashIndex >= 0 && lastSlashIndex < key.length() - 1) {
                fileName = key.substring(lastSlashIndex + 1);
            }
            request.addRequestParameter("response-content-disposition",
                    "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
            request.addRequestParameter("x-id", "GetObject");
            request.addRequestParameter("x-amz-checksum-mode", "ENABLED");
            request.addRequestParameter("host", s3SecondaryConfig.getBucketName() + ".s3." + s3SecondaryConfig.getRegion() + ".amazonaws.com");
            
            // 设置文件类型
            String mimeType;
            if (StringUtils.hasText(contentType)) {
                mimeType = contentType;
                log.debug("Using specified content type: {}", contentType);
            } else {
                mimeType = detectContentType(key);
                log.debug("Auto-detected content type: {}", mimeType);
            }
            request.addRequestParameter("response-content-type", mimeType);

            URL url = s3Client.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            log.error("Failed to generate a pre-signed URL for S3 secondary: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.URL_GENERATION_FAILED,"Failed to generate a pre-signed URL for S3 secondary");
        }
    }

    /**
     * 生成预签名URL 下载（自定义过期秒数）
     */
    public String generateDownloadUrl(String key, int expireSeconds) {
        checkSecondaryDataSourceEnabled();
        
        if (!StringUtils.hasText(key)) {
            return "";
        }
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            // 3. 设置请求头和选项
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(s3SecondaryConfig.getBucketName(), URLDecoder.decode(key, StandardCharsets.UTF_8))
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            log.error("Failed to generate a pre-signed URL for S3 secondary: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.URL_GENERATION_FAILED,"Failed to generate a pre-signed URL for S3 secondary");
        }
    }

    /**
     * 判断文件是否存在
     */
    public boolean doesObjectExist(String key) {
        checkSecondaryDataSourceEnabled();
        
        try {
            return s3Client.doesObjectExist(s3SecondaryConfig.getBucketName(), key);
        } catch (Exception e) {
            log.error("Failed to check if the file exists in S3 secondary: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.CHECK_FILE_FAILED,"Failed to check if the file exists in S3 secondary");
        }
    }

    /**
     * 上传XML文件到S3
     */
    public String uploadFeedXmlToS3(String feedXmlNamePrefix, String companyCode, String xmlContent) {
        checkSecondaryDataSourceEnabled();
        
        try {
            log.info("Uploading XML to S3 secondary: {}", feedXmlNamePrefix);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/xml");
            metadata.setContentLength(xmlContent.getBytes(StandardCharsets.UTF_8).length);
            // 组合 S3 中的完整 Key（folder + file）
            String s3Key = getFeedXmlKey(feedXmlNamePrefix, companyCode);
            // 上传
            s3Client.putObject(s3SecondaryConfig.getBucketName(), s3Key,
                    new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)), metadata);
            // 获取 S3 对象的 URL
            URL urlObj = s3Client.getUrl(s3SecondaryConfig.getBucketName(), s3Key);
            String url = urlObj.toString();
            log.info("XML uploaded successfully to S3 secondary key:{}, url:{}", s3Key, url);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload XML to S3 secondary: {}", feedXmlNamePrefix, e);
            throw new BusinessException(GlobalStatusCode.FILE_UPLOAD_FAILED, "Failed to upload XML to S3 secondary");
        }
    }

    /**
     * 从S3下载XML文件
     */
    public String getFeedXmlFromS3(String feedXmlNamePrefix, String companyCode) {
        checkSecondaryDataSourceEnabled();
        
        try {
            // 拼接 S3 上完整的 key（如果上传时有 folder 前缀）
            String s3Key = getFeedXmlKey(feedXmlNamePrefix, companyCode);

            log.info("Fetching XML from S3 secondary: {}", s3Key);

            S3Object s3Object = s3Client.getObject(s3SecondaryConfig.getBucketName(), s3Key);
            try (InputStream inputStream = s3Object.getObjectContent()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to fetch XML from S3 secondary: {}", feedXmlNamePrefix, e);
            throw new BusinessException(GlobalStatusCode.FILE_DOWNLOAD_FAILED, "Failed to fetch XML from S3 secondary");
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
            return s3SecondaryConfig.getFolder() + FILE_SEPARATOR + feedCompanyXmlName;
        } else{
            return s3SecondaryConfig.getFolder() + FILE_SEPARATOR + feedXmlNamePrefix;
        }
    }

    /**
     * 检查第二数据源是否启用
     */
    public boolean isEnabled() {
        return s3SecondaryConfig.isEnabled() && s3Client != null;
    }

    /**
     * 获取第二数据源配置信息
     */
    public S3SecondaryConfig getConfig() {
        return s3SecondaryConfig;
    }

    /**
     * 检测文件内容类型
     * 当文件名不包含扩展名时，提供智能检测
     * 
     * @param key S3文件key
     * @return MIME类型
     */
    private String detectContentType(String key) {
        if (!StringUtils.hasText(key)) {
            return MIME_TYPE_BINARY; // 默认二进制类型
        }
        
        // 提取文件名
        String fileName = key;
        int lastSlashIndex = key.lastIndexOf("/");
        if (lastSlashIndex >= 0 && lastSlashIndex < key.length() - 1) {
            fileName = key.substring(lastSlashIndex + 1);
        }
        
        // 检查是否有扩展名
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            // 有扩展名，使用原有的检测逻辑
            return ContentTypeEnum.fromFileName(key).getMimeType();
        } else {
            // 没有扩展名，根据文件名模式进行智能检测
            return detectContentTypeByFileName(fileName);
        }
    }

    /**
     * 根据文件名模式检测内容类型
     * 
     * @param fileName 文件名（不包含路径）
     * @return MIME类型
     */
    private String detectContentTypeByFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return MIME_TYPE_BINARY;
        }
        
        String lowerFileName = fileName.toLowerCase();
        
        // 根据文件名模式检测类型
        if (lowerFileName.contains("resume") || lowerFileName.contains("cv") || 
            lowerFileName.contains("curriculum") || lowerFileName.contains("profile")) {
            // 简历相关文件，默认为PDF
            log.debug("Detected resume file, defaulting to PDF: {}", fileName);
            return MIME_TYPE_PDF;
        } else if (lowerFileName.contains("document") || lowerFileName.contains("doc")) {
            // 文档文件，默认为PDF
            log.debug("Detected document file, defaulting to PDF: {}", fileName);
            return MIME_TYPE_PDF;
        } else if (lowerFileName.contains("image") || lowerFileName.contains("photo") || 
                   lowerFileName.contains("picture") || lowerFileName.contains("img")) {
            // 图片文件，默认为JPEG
            log.debug("Detected image file, defaulting to JPEG: {}", fileName);
            return MIME_TYPE_JPEG;
        } else if (lowerFileName.contains("text") || lowerFileName.contains("readme") || 
                   lowerFileName.contains("note") || lowerFileName.contains("log")) {
            // 文本文件
            log.debug("Detected text file, defaulting to plain text: {}", fileName);
            return MIME_TYPE_PLAIN_TEXT;
        } else if (lowerFileName.contains("data") || lowerFileName.contains("export") || 
                   lowerFileName.contains("report") || lowerFileName.contains("csv")) {
            // 数据文件，默认为CSV
            log.debug("Detected data file, defaulting to CSV: {}", fileName);
            return MIME_TYPE_CSV;
        } else {
            // 无法识别的文件，默认为二进制类型
            log.debug("Unable to detect file type, defaulting to binary: {}", fileName);
            return MIME_TYPE_BINARY;
        }
    }
}
