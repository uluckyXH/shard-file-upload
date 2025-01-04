package com.uluckyxh.shardfileupload.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 文件上传配置
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    
    /**
     * 存储类型：LOCAL-本地存储
     */
    private String storageType;
    
    /**
     * 上传目录
     */
    private String uploadDir;
    
    /**
     * 临时文件目录
     */
    private String tempDir;
    
    /**
     * 单个分片大小（MB）
     */
    private Integer chunkSize;
    
    /**
     * 单文件大小限制（MB）
     */
    private Integer maxFileSize;
    
    @PostConstruct
    public void init() {
        // 如果是本地存储，确保目录存在
        if ("LOCAL".equals(storageType)) {
            createDirIfNotExists(uploadDir);
            createDirIfNotExists(tempDir);
            log.info("初始化本地存储目录 - 上传目录: {}, 临时目录: {}", uploadDir, tempDir);
            log.info("文件上传配置 - 分片大小: {}MB, 最大文件大小: {}MB", chunkSize, maxFileSize);
        }
    }
    
    private void createDirIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
} 