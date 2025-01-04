package com.uluckyxh.shardfileupload.manage.impl;

import com.uluckyxh.shardfileupload.manage.FileManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 本地文件管理实现
 */
@Slf4j
@Component
public class LocalFileManageImpl implements FileManage {

    /**
     * 上传目录
     */
    private final String uploadDir;

    public LocalFileManageImpl(@Value("${file.upload.uploadDir}") String uploadDir) {
        this.uploadDir = uploadDir;
        // 确保上传目录存在
        createDirIfNotExists(uploadDir);
        log.info("初始化本地文件管理 - 上传目录: {}", uploadDir);
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private void createDirIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 生成按日期组织的文件路径
     * 格式：上传目录/年/月/日/文件名
     * @param fileName 文件名
     * @return 完整的文件路径
     */
    private String generateDatePath(String fileName) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%d/%02d/%02d/%s",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                fileName);
    }

    @Override
    public String upload(InputStream inputStream, String fileName) {
        try {
            // 生成日期路径
            String relativePath = generateDatePath(fileName);

            // 使用 Paths 处理路径，确保跨平台兼容
            Path targetPath = Paths.get(uploadDir, relativePath);

            // 确保目录存在
            Files.createDirectories(targetPath.getParent());

            // 写入文件
            // StandardCopyOption.REPLACE_EXISTING 表示如果文件已存在则覆盖
            Files.copy(inputStream, targetPath);

            // 获取当前运行目录并规范化路径分隔符
            String userDir = Paths.get(System.getProperty("user.dir"))
                    .toString()
                    .replace('\\', '/');

            // 返回完整的访问URL（用正斜杠拼接路径）
            return userDir + "/" + uploadDir + "/" + relativePath;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }


    @Override
    public boolean delete(String path) {
        try {
            Path filePath = Paths.get(uploadDir, path);
            Files.deleteIfExists(filePath);
            return true;
        } catch (IOException e) {
            log.error("文件删除失败", e);
            return false;
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = Paths.get(uploadDir, path);
        return Files.exists(filePath);
    }

    @Override
    public long getSize(String path) {
        try {
            Path filePath = Paths.get(uploadDir, path);
            return Files.size(filePath);
        } catch (IOException e) {
            log.error("获取文件大小失败", e);
            return -1;
        }
    }

    @Override
    public String getBucketName() {
        return uploadDir;
    }
} 