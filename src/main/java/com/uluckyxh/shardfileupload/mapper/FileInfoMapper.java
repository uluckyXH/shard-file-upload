package com.uluckyxh.shardfileupload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uluckyxh.shardfileupload.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件信息Mapper接口
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {
    // 继承BaseMapper后已经有基础的CRUD方法
    // 可以在这里添加自定义的查询方法
} 