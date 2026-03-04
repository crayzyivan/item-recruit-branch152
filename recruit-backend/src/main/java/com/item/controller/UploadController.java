package com.item.controller;

import com.item.framework.constant.CommonResponseCode;
import com.item.framework.error.BusinessException;
import com.item.service.UploadService;
import com.item.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : lh
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/company-logo")
    public String uploadCompanyLogo(@RequestPart("file") MultipartFile file) {
        CommonUtils.validateFileFormatAndSize(file);
        try {
            return uploadService.upload(file);
        } catch (Exception e) {
            throw BusinessException.of(CommonResponseCode.COMMON_COMPANY_LOGO_UPLOAD_FILE_FAIL);
        }
    }
}
