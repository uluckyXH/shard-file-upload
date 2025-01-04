package com.uluckyxh.shardfileupload.manage;

import com.uluckyxh.shardfileupload.constant.FileConstant;
import com.uluckyxh.shardfileupload.manage.impl.LocalFileManageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileManageFactory {

    @Autowired
    private LocalFileManageImpl localFileManage;

    public FileManage getFileManage(String storageType) {
        if (FileConstant.LOCAL.equals(storageType)) {
            return localFileManage;
        } else {
            throw new IllegalArgumentException("暂不支持该存储类型，请检查配置");
        }
    }

}
