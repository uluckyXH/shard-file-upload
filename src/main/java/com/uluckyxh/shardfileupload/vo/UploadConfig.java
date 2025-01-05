package com.uluckyxh.shardfileupload.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadConfig {

    // 单个分片大小（MB）
    private Integer chunkSize;
    // 单文件大小限制（MB）
    private Integer maxFileSize;
    // 存储类型
    private String storageType;

}
