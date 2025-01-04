package com.uluckyxh.shardfileupload.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 文件信息实体类
 */
@Data
@Accessors(chain = true) // 开启链式调用
@TableName("file_info")
public class FileInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 文件后缀名（例如：.jpg、.png）
     */
    private String fileExt;
    
    /**
     * 存储类型（LOCAL：本地存储，OSS：阿里云存储）
     */
    private String storageType;
    
    /**
     * 文件访问路径（本地存储为相对路径，OSS为完整的访问URL）
     */
    private String accessUrl;
    
    /**
     * 文件MD5值，用于校验文件完整性
     */
    private String md5;
    
    /**
     * 分片上传的唯一标识
     */
    private String uploadId;
    
    /**
     * 文件状态（UPLOADING：上传中，SUCCESS：成功，FAILED：失败）
     */
    private String status;
    
    /**
     * 存储空间名称（本地存储为目录名，OSS为Bucket名称）
     */
    private String bucketName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 