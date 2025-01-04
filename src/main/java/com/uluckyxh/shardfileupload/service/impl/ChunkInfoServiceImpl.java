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

} 