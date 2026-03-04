package com.item.framework.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型与 MIME 类型（response-content-type）映射枚举类
 * 用于 AWS S3 预签名 URL 生成时设置正确的内容类型
 */
public enum ContentTypeEnum {
    PDF("pdf", "application/pdf"),
    JPG("jpg", "image/jpeg"),
    JPEG("jpeg", "image/jpeg"),
    PNG("png", "image/png"),
    GIF("gif", "image/gif"),
    SVG("svg", "image/svg+xml"),
    WEBP("webp", "image/webp"),
    TXT("txt", "text/plain; charset=utf-8"),
    CSV("csv", "text/csv; charset=utf-8"),
    JSON("json", "application/json; charset=utf-8"),
    MARKDOWN("md", "text/markdown; charset=utf-8"),
    ZIP("zip", "application/zip"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    MP4("mp4", "video/mp4"),
    MP3("mp3", "audio/mpeg"),
    UNKNOWN("", "application/octet-stream"); // 默认未知类型

    private final String extension;
    private final String mimeType;

    // 用于快速查找的映射表
    private static final Map<String, ContentTypeEnum> EXTENSION_MAP = new HashMap<>();

    static {
        // 初始化映射表
        for (ContentTypeEnum type : values()) {
            EXTENSION_MAP.put(type.extension.toLowerCase(), type);
        }
    }

    ContentTypeEnum(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    /**
     * 获取文件扩展名对应的 MIME 类型
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 根据文件扩展名获取对应的 ContentType 枚举
     * @param extension 文件扩展名（如 "pdf"、"jpg"）
     * @return 对应的 ContentType 枚举，若未找到则返回 UNKNOWN
     */
    public static ContentTypeEnum fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return UNKNOWN;
        }
        // 移除可能的前缀点（如 ".pdf" → "pdf"）
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        return EXTENSION_MAP.getOrDefault(ext.toLowerCase(), UNKNOWN);
    }

    /**
     * 根据完整文件名获取对应的 ContentType 枚举
     * @param fileName 完整文件名（如 "简历.pdf"、"image.jpg"）
     * @return 对应的 ContentType 枚举，若未找到则返回 UNKNOWN
     */
    public static ContentTypeEnum fromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return UNKNOWN;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return UNKNOWN;
        }
        return fromExtension(fileName.substring(lastDotIndex + 1));
    }
}