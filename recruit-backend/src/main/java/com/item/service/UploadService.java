package com.item.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author : lh
 */
public interface UploadService {
    String upload(MultipartFile file);

    String uploadImage(MultipartFile file);

    String getFileUrl(String fileName);
}
