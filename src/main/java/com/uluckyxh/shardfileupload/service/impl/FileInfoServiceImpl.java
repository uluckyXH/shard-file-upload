package com.uluckyxh.shardfileupload.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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


    @Override
    public IPage<FileInfo> getByPage(Integer page, Integer size,String fileName) {
        Page<FileInfo> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(fileName)) {
            queryWrapper.like(FileInfo::getFileName, fileName);
        }
        queryWrapper.orderByDesc(FileInfo::getCreateTime);
        return baseMapper.selectPage(pageInfo, queryWrapper);
    }
}