package com.uluckyxh.shardfileupload.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uluckyxh.shardfileupload.entity.ChunkInfo;
import com.uluckyxh.shardfileupload.mapper.ChunkInfoMapper;
import com.uluckyxh.shardfileupload.service.ChunkInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分片信息服务实现类
 */
@Service
public class ChunkInfoServiceImpl extends ServiceImpl<ChunkInfoMapper, ChunkInfo> implements ChunkInfoService {

    @Override
    public List<ChunkInfo> listByUploadId(String uploadId) {
        return list(new LambdaQueryWrapper<ChunkInfo>()
                .eq(ChunkInfo::getUploadId, uploadId)
                .orderByAsc(ChunkInfo::getChunkNumber));
    }

    @Override
    public boolean updateStatus(String uploadId, Integer chunkNumber, String status) {
        return update(new LambdaUpdateWrapper<ChunkInfo>()
                .eq(ChunkInfo::getUploadId, uploadId)
                .eq(ChunkInfo::getChunkNumber, chunkNumber)
                .set(ChunkInfo::getUploadStatus, status));
    }

    @Override
    public boolean checkChunksComplete(String uploadId) {
        // 查询所有未成功的分片
        Long count = count(new LambdaQueryWrapper<ChunkInfo>()
                .eq(ChunkInfo::getUploadId, uploadId)
                .ne(ChunkInfo::getUploadStatus, "SUCCESS"));
        // 如果没有未成功的分片，说明所有分片都上传成功
        return count == 0;
    }
} 