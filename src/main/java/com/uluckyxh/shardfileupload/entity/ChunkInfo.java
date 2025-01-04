package com.uluckyxh.shardfileupload.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 分片信息实体类
 */
@Data
@Accessors(chain = true)
@TableName("chunk_info")
public class ChunkInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 分片上传的唯一标识，关联file_info表的upload_id
     */
    private String uploadId;
    
    /**
     * 存储空间名称（本地存储为目录名，OSS为Bucket名称）
     */
    private String bucketName;
    
    /**
     * 当前分片号，从1开始
     */
    private Integer chunkNumber;
    
    /**
     * 分片上传状态（UPLOADING：上传中，SUCCESS：成功，FAILED：失败）
     */
    private String uploadStatus;
    
    /**
     * 文件名称，冗余存储，方便查询
     */
    private String fileName;
    
    /**
     * 分片MD5值，用于校验分片完整性
     */
    private String md5;
    
    /**
     * 存储类型（LOCAL：本地存储，OSS：阿里云存储）
     */
    private String storageType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 