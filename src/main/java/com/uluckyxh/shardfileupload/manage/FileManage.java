package com.uluckyxh.shardfileupload.manage;

import java.io.InputStream;

/**
 * 文件管理接口
 * 定义文件操作的统一接口，不同的存储实现（本地、OSS等）都需要实现这个接口
 */
public interface FileManage {
    
    /**
     * 上传文件
     * @param inputStream 文件输入流
     * @param path 存储路径
     * @return 访问URL
     */
    String upload(InputStream inputStream, String path);

    /**
     * 删除文件
     * @param path 文件路径
     * @return 是否成功
     */
    boolean delete(String path);
    
    /**
     * 文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    boolean exists(String path);
    
    /**
     * 获取文件大小
     * @param path 文件路径
     * @return 文件大小（字节）
     */
    long getSize(String path);


    /**
     * 获取bucket名称
     */
    String getBucketName();
} 