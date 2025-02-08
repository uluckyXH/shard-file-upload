package com.uluckyxh.shardfileupload.annotation;

import com.uluckyxh.shardfileupload.enums.StorageType;

import java.lang.annotation.*;

@Target(ElementType.TYPE) // 注解作用在类上
@Retention(RetentionPolicy.RUNTIME) // 注解保留到运行时
@Documented // 标记注解
public @interface FileManageStorageType {

    StorageType type();

}
