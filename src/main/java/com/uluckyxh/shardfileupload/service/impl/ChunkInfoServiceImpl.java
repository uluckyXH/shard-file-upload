package com.uluckyxh.shardfileupload.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uluckyxh.shardfileupload.config.excepition.FileOperationException;
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
    public List<ChunkInfo> getByUploadId(String uploadId) {
        if (StrUtil.isBlank(uploadId)) {
            throw new FileOperationException("uploadId不能为空");
        }

        LambdaQueryWrapper<ChunkInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChunkInfo::getUploadId, uploadId);
        return list(queryWrapper);
    }

    @Override
    public void removeByUploadId(String uploadId) {
        if (StrUtil.isBlank(uploadId)) {
            throw new FileOperationException("uploadId不能为空");
        }

        LambdaUpdateWrapper<ChunkInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChunkInfo::getUploadId, uploadId);
        remove(updateWrapper);
    }
}