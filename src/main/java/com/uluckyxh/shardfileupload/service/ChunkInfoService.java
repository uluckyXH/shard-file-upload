package com.uluckyxh.shardfileupload.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uluckyxh.shardfileupload.entity.ChunkInfo;

import java.util.List;

/**
 * 分片信息服务接口
 */
public interface ChunkInfoService extends IService<ChunkInfo> {


    List<ChunkInfo> getByUploadId(String uploadId);

    void removeByUploadId(String uploadId);
}