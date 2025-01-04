package com.uluckyxh.shardfileupload.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uluckyxh.shardfileupload.entity.ChunkInfo;

import java.util.List;

/**
 * 分片信息服务接口
 */
public interface ChunkInfoService extends IService<ChunkInfo> {
    
    /**
     * 根据上传ID查询所有分片信息
     * @param uploadId 上传ID
     * @return 分片信息列表
     */
    List<ChunkInfo> listByUploadId(String uploadId);
    
    /**
     * 更新分片状态
     * @param uploadId 上传ID
     * @param chunkNumber 分片号
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateStatus(String uploadId, Integer chunkNumber, String status);
    
    /**
     * 检查分片是否都已上传成功
     * @param uploadId 上传ID
     * @return 是否所有分片都上传成功
     */
    boolean checkChunksComplete(String uploadId);
} 