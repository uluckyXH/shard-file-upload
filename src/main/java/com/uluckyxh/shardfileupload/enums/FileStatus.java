package com.uluckyxh.shardfileupload.enums;

import lombok.Getter;

/**
 * 文件状态枚举
 */
@Getter
public enum FileStatus {
    
    /**
     * 上传中
     */
    UPLOADING("UPLOADING", "上传中"),
    
    /**
     * 上传成功
     */
    SUCCESS("SUCCESS", "上传成功"),
    
    /**
     * 上传失败
     */
    FAILED("FAILED", "上传失败");
    
    /**
     * 状态编码
     */
    private final String code;
    
    /**
     * 状态描述
     */
    private final String description;
    
    FileStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据编码获取状态
     */
    public static FileStatus getByCode(String code) {
        for (FileStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return FAILED; // 默认失败状态
    }
} 