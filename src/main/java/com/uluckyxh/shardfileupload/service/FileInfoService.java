package com.uluckyxh.shardfileupload.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.uluckyxh.shardfileupload.entity.FileInfo;

/**
 * 文件信息服务接口
 */
public interface FileInfoService extends IService<FileInfo> {


    IPage<FileInfo> getByPage(Integer page, Integer size,String fileName);

}