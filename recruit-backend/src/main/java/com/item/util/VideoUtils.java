package com.item.util;

import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 视频处理工具类
 *
 * 提供m3u8视频下载、合并、转换等功能
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-29
 */
@Slf4j
@Component
public class VideoUtils {

    private static final int DOWNLOAD_TIMEOUT_MINUTES = 30;
    private static final int MAX_CONCURRENT_DOWNLOADS = 16;
    private static final int BUFFER_SIZE = 8192;

    // FFmpeg 可执行文件路径 - 需要根据实际部署环境配置
    private static final String FFMPEG_PATH = "D:\\Program Files (x86)\\ffmpeg\\bin\\ffmpeg.exe"; // Windows
    private static final String FFPROBE_PATH = "D:\\Program Files (x86)\\ffmpeg\\bin\\ffprobe.exe"; //

    /**
     * 下载m3u8视频并合并为mp4格式
     *
     * @param m3u8Url m3u8播放列表URL
     * @param candidateJobId 候选人职位ID（用于生成唯一文件名）
     * @return 合并后的视频文件字节数组
     * @throws BusinessException 如果下载或合并失败
     */
    public byte[] downloadAndMergeM3u8ToMp4(String m3u8Url, Long candidateJobId) {
        if (!StringUtils.hasText(m3u8Url)) {
            return null;
        }
        Path tempDir = null;
        try {
            log.info("Starting M3U8 video download and merge: candidateJobId={}, url={}", candidateJobId, m3u8Url);
            // 创建临时目录
            tempDir = Files.createTempDirectory("m3u8-download-" + candidateJobId + "-");
            log.debug("Created temp directory: {}", tempDir);

            // 1. 解析m3u8播放列表，获取所有视频片段URL
            List<String> segmentUrls = parseM3u8Playlist(m3u8Url);
            log.info("Found {} video segments for candidateJobId={}", segmentUrls.size(), candidateJobId);

            if (segmentUrls.isEmpty()) {
                throw new BusinessException(GlobalStatusCode.FAIL, "No video segments found in M3U8 playlist");
            }

            // 2. 并发下载所有视频片段
//            List<Path> downloadedSegments = new ArrayList<>();
//            for (int i = 0; i < segmentUrls.size(); i++){
//                Path path = downloadSingleSegment(segmentUrls.get(i), tempDir, i);
//                downloadedSegments.add(path);
//            }
            List<Path> downloadedSegments=getPathList(segmentUrls,tempDir);
            log.info("Downloaded {} segments for candidateJobId={}", downloadedSegments.size(), candidateJobId);

            // 3. 合并视频片段为单个文件
            byte[] mergedVideo = mergeVideoSegments(downloadedSegments);
            log.info("Successfully processed M3U8 video: candidateJobId={}, segments={}, size={} bytes",
                    candidateJobId, segmentUrls.size(), mergedVideo.length);
            return mergedVideo;
        }catch (Exception e) {
            log.error("Failed to download and merge M3U8 video: candidateJobId={}, url={}",
                    candidateJobId, m3u8Url, e);
            throw new BusinessException(GlobalStatusCode.FAIL, "Failed to process M3U8 video: " + e.getMessage());
        } finally {
            // 清理临时目录
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }

    private List<Path> getPathList(List<String> segmentUrls,Path tempDir){
        // 2. 并发下载所有视频片段
        ConcurrentHashMap<Integer, Path> downloadedSegmentsMap = new ConcurrentHashMap<>();
        Semaphore downloadSemaphore = new Semaphore(MAX_CONCURRENT_DOWNLOADS);
        List<CompletableFuture<Void>> downloadFutures = new ArrayList<>();

        for (int i = 0; i < segmentUrls.size(); i++) {
            final int segmentIndex = i;
            final String segmentUrl = segmentUrls.get(i);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 获取许可证，限制并发数
                    downloadSemaphore.acquire();
                    Path downloadedPath = downloadSingleSegment(segmentUrl, tempDir, segmentIndex);

                    // 直接放入ConcurrentHashMap，线程安全
                    downloadedSegmentsMap.put(segmentIndex, downloadedPath);
                } catch (Exception e) {
                    log.error("Failed to download segment {}: {}", segmentIndex, e.getMessage(), e);
                    throw new RuntimeException("Download segment failed: " + segmentIndex, e);
                } finally {
                    // 释放许可证
                    downloadSemaphore.release();
                }
            });

            downloadFutures.add(future);
        }

        // 按索引顺序构建最终列表
        List<Path> downloadedSegments = new ArrayList<>();
        try {
            // 等待所有下载完成
            CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0]))
                    .get(DOWNLOAD_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            for (int i = 0; i < segmentUrls.size(); i++) {
                Path path = downloadedSegmentsMap.get(i);
                if (path == null) {
                    throw new BusinessException(GlobalStatusCode.FAIL,
                            "Missing downloaded segment at index: " + i);
                }
                downloadedSegments.add(path);
            }
        } catch (Exception e) {
            log.error("Failed to download video segments: {}", e.getMessage(), e);
            throw new BusinessException(GlobalStatusCode.FAIL, "Video segments download failed");
        }
        return downloadedSegments;
    }


    /**
     * 下载m3u8视频并使用ffmpeg转换为标准mp4格式
     *
     * @param m3u8Url m3u8播放列表URL
     * @param candidateJobId 候选人职位ID（用于生成唯一文件名）
     * @return 转换后的标准MP4视频文件字节数组
     * @throws BusinessException 如果下载或转换失败
     */
    public byte[] downloadAndMergeFFmpegM3u8ToMp4(String m3u8Url, Long candidateJobId) {
        if (!StringUtils.hasText(m3u8Url)) {
            return null;
        }
        Path tempDir = null;
        Path outputMp4File = null;
        try {
            log.info("Starting M3U8 video concurrent download and FFmpeg conversion: candidateJobId={}, url={}", candidateJobId, m3u8Url);
            // 创建临时目录
            tempDir = Files.createTempDirectory("m3u8-download-" + candidateJobId + "-");
            log.info("Created temp directory: {}", tempDir);

            // 1. 解析m3u8播放列表，获取所有视频片段URL
            List<String> segmentUrls = parseM3u8Playlist(m3u8Url);
            log.info("Found {} video segments for candidateJobId={}", segmentUrls.size(), candidateJobId);

            if (segmentUrls.isEmpty()) {
                throw new BusinessException(GlobalStatusCode.FAIL, "No video segments found in M3U8 playlist");
            }

            // 2. 并发下载所有视频片段
            List<Path> downloadedSegments = getPathList(segmentUrls, tempDir);
            log.info("Downloaded {} segments for candidateJobId={}", downloadedSegments.size(), candidateJobId);

            // 3. 创建临时合并的TS文件
            Path mergedTsFile = tempDir.resolve("merged_" + candidateJobId + ".ts");
            mergeVideoSegmentsToFile(downloadedSegments, mergedTsFile);

            // 4. 使用FFmpeg将合并的TS文件转换为标准MP4
            outputMp4File = tempDir.resolve("output_" + candidateJobId + ".mp4");
            convertTsToMp4WithFFmpeg(mergedTsFile, outputMp4File);

            // 5. 读取转换后的MP4文件
            byte[] mp4Content = Files.readAllBytes(outputMp4File);
            log.info("Successfully processed M3U8 video with concurrent download and FFmpeg: candidateJobId={}, segments={}, size={} bytes",
                    candidateJobId, segmentUrls.size(), mp4Content.length);
            return mp4Content;

        } catch (Exception e) {
            log.error("Failed to download and convert M3U8 video with FFmpeg: candidateJobId={}, url={}",
                    candidateJobId, m3u8Url, e);
            throw new BusinessException(GlobalStatusCode.FAIL, "Failed to process M3U8 video with FFmpeg: " + e.getMessage());
        } finally {
            // 清理临时目录
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }

    /**
     * 将视频片段合并到单个TS文件
     *
     * @param segmentPaths 视频片段文件路径列表
     * @param outputTsFile 输出TS文件路径
     * @throws Exception 如果合并失败
     */
    private void mergeVideoSegmentsToFile(List<Path> segmentPaths, Path outputTsFile) throws Exception {
        try (FileOutputStream outputStream = new FileOutputStream(outputTsFile.toFile())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (Path segmentPath : segmentPaths) {
                try (InputStream inputStream = Files.newInputStream(segmentPath)) {
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        log.info("Successfully merged video segments to TS file: {} bytes", Files.size(outputTsFile));
    }

    /**
     * 使用FFmpeg将TS文件转换为标准MP4格式
     *
     * @param inputTsFile 输入TS文件路径
     * @param outputMp4File 输出MP4文件路径
     * @throws Exception 如果转换失败
     */
    private void convertTsToMp4WithFFmpeg(Path inputTsFile, Path outputMp4File) throws Exception {
        try {
            // 初始化FFmpeg和FFprobe
            FFmpeg ffmpeg = new FFmpeg(FFMPEG_PATH);
            FFprobe ffprobe = new FFprobe(FFPROBE_PATH);

            // 构建FFmpeg命令
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputTsFile.toString())     // 输入TS文件
                    .overrideOutputFiles(true)            // 覆盖输出文件
                    .addOutput(outputMp4File.toString())  // 输出MP4文件
                    .setFormat("mp4")                     // 设置输出格式为MP4
//                    .setVideoCodec("libx264")             // 使用H.264编码
//                    .setAudioCodec("aac")                 // 使用AAC音频编码
                    .setVideoCodec("copy")
                    .setAudioCodec("copy")
//                    .setVideoPixelFormat("yuv420p")       // 设置像素格式，确保兼容性
//                    .setPreset("medium")                  // 设置编码预设，平衡质量和速度
                    .addExtraArgs("-movflags", "faststart") // 优化MP4文件结构，支持流式播放
                    .addExtraArgs("-avoid_negative_ts", "make_zero") // 避免负时间戳
                    .done();

            // 执行FFmpeg转换
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            log.info("FFmpeg TS to MP4 conversion completed successfully: output={}", outputMp4File);
            // 验证输出文件是否存在且有内容
            if (!Files.exists(outputMp4File) || Files.size(outputMp4File) == 0) {
                throw new Exception("FFmpeg conversion failed: output file is empty or does not exist");
            }

        } catch (Exception e) {
            log.error("FFmpeg TS to MP4 conversion failed: input={}, output={}, error={}",
                    inputTsFile, outputMp4File, e.getMessage(), e);
            throw new Exception("FFmpeg TS to MP4 conversion failed: " + e.getMessage(), e);
        }
    }

    /**
     * 解析m3u8播放列表，提取所有视频片段URL
     *
     * @param m3u8Url m3u8播放列表URL
     * @return 视频片段URL列表
     * @throws Exception 如果解析失败
     */
    private List<String> parseM3u8Playlist(String m3u8Url) throws Exception {
        URL playlistUrl = new URL(m3u8Url);
        List<String> segmentUrls = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(playlistUrl.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 跳过空行和注释行
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // 处理相对路径和绝对路径
                URL segmentUrl = new URL(playlistUrl, line);
                segmentUrls.add(segmentUrl.toString());
            }
        }
        log.debug("Parsed M3U8 playlist: {} segments found", segmentUrls.size());
        return segmentUrls;
    }

    /**
     * 下载单个视频片段
     *
     * @param segmentUrl 片段URL
     * @param tempDir 临时目录
     * @param index 片段索引
     * @return 下载的文件路径
     * @throws Exception 如果下载失败
     */
    private Path downloadSingleSegment(String segmentUrl, Path tempDir, int index) throws Exception {
        URL url = new URL(segmentUrl);
        Path outputPath = tempDir.resolve(String.format("%05d.ts", index));
        try (InputStream inputStream = url.openStream()) {
            Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return outputPath;
    }

    /**
     * 合并视频片段为单个文件
     *
     * @param segmentPaths 视频片段文件路径列表
     * @return 合并后的视频文件字节数组
     * @throws Exception 如果合并失败
     */
    private byte[] mergeVideoSegments(List<Path> segmentPaths) throws Exception {
        log.debug("Merging {} video segments", segmentPaths.size());
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (Path segmentPath : segmentPaths) {
                try (InputStream inputStream = Files.newInputStream(segmentPath)) {
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
            byte[] mergedData = outputStream.toByteArray();
            log.debug("Successfully merged video segments: {} bytes", mergedData.length);
            return mergedData;
        }
    }

    /**
     * 清理临时目录
     *
     * @param tempDir 临时目录路径
     */
    private void cleanupTempDirectory(Path tempDir) {
        try {
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (!file.delete()) {
                                log.warn("Failed to delete temp file: {}", file.getAbsolutePath());
                            }
                        });
                log.debug("Cleaned up temp directory: {}", tempDir);
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup temp directory: {}", tempDir, e);
        }
    }

    /**
     * 生成视频文件名
     *
     * @param applicationId 菲律宾关联表id
     * @return 生成的文件名
     */
    public String generateVideoFileName(String applicationId) {
        return String.format("PHL_interview_video_%s.mp4", applicationId);
    }
}