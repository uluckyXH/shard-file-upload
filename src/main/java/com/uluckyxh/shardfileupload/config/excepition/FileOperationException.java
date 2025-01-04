package com.uluckyxh.shardfileupload.config.excepition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileOperationException extends RuntimeException{

    private String msg;

    public FileOperationException(String msg) {
        super(msg);
        this.msg = msg;
    }

}
