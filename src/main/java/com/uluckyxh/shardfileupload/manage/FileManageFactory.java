package com.uluckyxh.shardfileupload.manage;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.uluckyxh.shardfileupload.annotation.FileManageStorageType;
import com.uluckyxh.shardfileupload.constant.FileConstant;
import com.uluckyxh.shardfileupload.enums.StorageType;
import com.uluckyxh.shardfileupload.manage.impl.LocalFileManageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FileManageFactory {

    public static <T> T getFileManage(StorageType storageType, Class<T> tClass) {
        // 获取指定类型的所有bean示例
        Map<String, T> beans = SpringUtil.getBeansOfType(tClass);
        // 遍历所有bean
        for (Map.Entry<String, T> stringTEntry : beans.entrySet()) {
            // 获取bean上的FileManageStorageType注解
            FileManageStorageType fileManageStorageType = stringTEntry.getValue().getClass().getAnnotation(FileManageStorageType.class);
            // 判断是否存在，且类型是否匹配
            if (ObjectUtil.isNotEmpty(fileManageStorageType) && ObjectUtil.equal(storageType, fileManageStorageType.type())) {
                // 返回匹配的bean
                return stringTEntry.getValue();
            }
        }
        throw new RuntimeException("未找到对应的文件管理配置");
    }

}
