package com.uluckyxh.shardfileupload.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uluckyxh.shardfileupload.entity.FileInfo;
import com.uluckyxh.shardfileupload.mapper.FileInfoMapper;
import com.uluckyxh.shardfileupload.service.FileInfoService;
import org.springframework.stereotype.Service;

/**
 * 文件信息服务实现类
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

} 