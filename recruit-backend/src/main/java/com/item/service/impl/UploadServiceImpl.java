package com.item.service.impl;

import com.item.framework.config.AyrShareConfig;
import com.item.service.UploadService;
import com.item.util.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {
    private final S3Utils s3Utils;
    private final AyrShareConfig ayrShareConfig;
    @Override
    public String upload(MultipartFile file) {
        return s3Utils.uploadFile(file);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return s3Utils.uploadFileFixedFileName(file);
    }

    @Override
    public String getFileUrl(String fileName) {
        try {
            String url = s3Utils.generateDownloadUrl(fileName, ayrShareConfig.getImageUrlExpireSeconds());
            log.info("getFileUrl file name {} {}", fileName, url);
            return url;
        } catch (Exception e) {
            log.error("getFileUrl error {}", fileName, e);
        }
        return null;
    }
}
