package com.uluckyxh.shardfileupload.util;

/**
 * 文件大小单位转换工具类
 */
public class FileSizeUtil {
    // 1KB = 1024 bytes
    private static final long KB = 1024;
    // 1MB = 1024 KB
    private static final long MB = KB * 1024;
    // 1GB = 1024 MB
    private static final long GB = MB * 1024;

    /**
     * 将MB转换为bytes
     * @param mb 兆字节数
     * @return 字节数
     */
    public static long mbToBytes(int mb) {
        return (long) mb * MB;
    }

    /**
     * 将bytes转换为MB
     * @param bytes 字节数
     * @return 兆字节数
     */
    public static double bytesToMB(long bytes) {
        return (double) bytes / MB;
    }

    /**
     * 格式化文件大小显示
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes < KB) {
            return bytes + " B";
        } else if (bytes < MB) {
            return String.format("%.2f KB", (double) bytes / KB);
        } else if (bytes < GB) {
            return String.format("%.2f MB", (double) bytes / MB);
        } else {
            return String.format("%.2f GB", (double) bytes / GB);
        }
    }
}
