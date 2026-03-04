package com.item.controller;

import com.item.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取文件
 *
 * @author : lh
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final UploadService uploadService;

    @GetMapping("/url/recruit/{fileName}")
    public String uploadCompanyLogo(@PathVariable String fileName) {
        return /*uploadService.getFileUrl("recruit/"+fileName)*/null;
    }
}
