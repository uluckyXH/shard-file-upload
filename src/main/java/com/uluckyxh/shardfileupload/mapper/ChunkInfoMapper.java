package com.uluckyxh.shardfileupload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uluckyxh.shardfileupload.entity.ChunkInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分片信息Mapper接口
 */
@Mapper
public interface ChunkInfoMapper extends BaseMapper<ChunkInfo> {
    // 继承BaseMapper后已经有基础的CRUD方法
    // 可以在这里添加自定义的查询方法
} 