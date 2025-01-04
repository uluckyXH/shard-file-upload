package com.uluckyxh.shardfileupload.enums;

import lombok.Getter;

/**
 * 存储类型枚举
 */
@Getter
public enum StorageType {
    
    /**
     * 本地存储
     */
    LOCAL("LOCAL", "本地存储"),
    
    /**
     * 阿里云OSS存储（预留）
     */
    OSS("OSS", "阿里云OSS存储");
    
    /**
     * 存储类型编码
     */
    private final String code;
    
    /**
     * 存储类型描述
     */
    private final String description;
    
    StorageType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据编码获取存储类型
     */
    public static StorageType getByCode(String code) {
        for (StorageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return LOCAL; // 默认使用本地存储
    }
} 