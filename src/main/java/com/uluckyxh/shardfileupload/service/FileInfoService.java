package com.uluckyxh.shardfileupload.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uluckyxh.shardfileupload.entity.FileInfo;

/**
 * 文件信息服务接口
 */
public interface FileInfoService extends IService<FileInfo> {
    
    /**
     * 根据上传ID查询文件信息
     * @param uploadId 上传ID
     * @return 文件信息
     */
    FileInfo getByUploadId(String uploadId);
    
    /**
     * 更新文件状态
     * @param uploadId 上传ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateStatus(String uploadId, String status);
} 