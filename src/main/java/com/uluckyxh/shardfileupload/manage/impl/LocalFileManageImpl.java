package com.uluckyxh.shardfileupload.manage.impl;

import com.uluckyxh.shardfileupload.config.excepition.FileOperationException;
import com.uluckyxh.shardfileupload.entity.ChunkInfo;
import com.uluckyxh.shardfileupload.manage.FileManage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 本地文件管理实现
 */
@Slf4j
@Component
public class LocalFileManageImpl implements FileManage {

    /**
     * 上传目录
     */
    private final String uploadDir;

    /**
     * 分片临时目录
     */
    private final String tempDir;

    /**
     * chunkSize
     */
    private final Integer chunkSize;

    public LocalFileManageImpl(@Value("${file.upload.uploadDir}") String uploadDir,
                               @Value("${file.upload.tempDir}") String tempDir,
                               @Value("${file.upload.chunkSize}") Integer chunkSize) {
        this.uploadDir = uploadDir;
        this.tempDir = tempDir;
        this.chunkSize = chunkSize;
        // 确保上传目录存在
        createDirIfNotExists(uploadDir);
        log.info("初始化本地文件管理 - 上传目录: {}", uploadDir);
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 生成按日期组织的文件路径
     * 格式：上传目录/年/月/日/文件名
     *
     * @param fileName 文件名
     * @return 完整的文件路径
     */
    private String generateDatePath(String fileName) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%d/%02d/%02d/%s",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                fileName);
    }

    /**
     * 生成分片文件路径
     * 格式：临时目录/uploadId/chunk_序号
     */
    private String generateChunkPath(String chunkFileName) {
        // 从文件名中提取uploadId（假设格式为：uploadId_序号）
        String uploadId = chunkFileName.substring(0, chunkFileName.lastIndexOf('_'));
        return uploadId + "/" + chunkFileName;
    }

    @Override
    public String upload(InputStream inputStream, String fileName) {
        try {
            // 生成日期路径
            String relativePath = generateDatePath(fileName);

            // 使用 Paths 处理路径，确保跨平台兼容
            Path targetPath = Paths.get(uploadDir, relativePath);

            // 确保目录存在
            Files.createDirectories(targetPath.getParent());

            // 写入文件
            // StandardCopyOption.REPLACE_EXISTING 表示如果文件已存在则覆盖
            Files.copy(inputStream, targetPath);

            // 获取当前运行目录并规范化路径分隔符
            String userDir = Paths.get(System.getProperty("user.dir"))
                    .toString()
                    .replace('\\', '/');

            // 返回完整的访问URL（用正斜杠拼接路径）
            return userDir + "/" + uploadDir + "/" + relativePath;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }


    @Override
    public boolean delete(String path) {
        try {
            Path filePath = Paths.get(uploadDir, path);
            Files.deleteIfExists(filePath);
            return true;
        } catch (IOException e) {
            log.error("文件删除失败", e);
            return false;
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = Paths.get(uploadDir, path);
        return Files.exists(filePath);
    }

    @Override
    public long getSize(String path) {
        try {
            Path filePath = Paths.get(uploadDir, path);
            return Files.size(filePath);
        } catch (IOException e) {
            log.error("获取文件大小失败", e);
            return -1;
        }
    }

    @Override
    public String getBucketName() {
        return uploadDir;
    }

    /**
     * 上传分片
     */
    @Override
    public String uploadChunk(InputStream inputStream, String chunkFileName) {
        try {
            // 生成分片文件的临时存储路径（在临时目录中按uploadId组织）
            String relativePath = generateChunkPath(chunkFileName);
            Path targetPath = Paths.get(tempDir, relativePath);

            // 确保目录存在
            Files.createDirectories(targetPath.getParent());

            // 写入分片文件
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回分片文件的完整路径
            return targetPath.toString();
        } catch (IOException e) {
            log.error("分片上传失败", e);
            throw new RuntimeException("分片上传失败");
        }
    }

    /**
     * 合并分片
     */
    @Override
    public String mergeChunks(List<ChunkInfo> chunks, String targetFileName) {
        try {
            // 生成最终文件路径（按日期组织）
            String relativePath = generateDatePath(targetFileName);
            Path targetPath = Paths.get(uploadDir, relativePath);

            // 确保目录存在
            Files.createDirectories(targetPath.getParent());

            // 合并文件
            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                // 按分片序号排序
                chunks.sort(Comparator.comparing(ChunkInfo::getChunkNumber));

                // 缓冲区大小设置为5MB
                byte[] buffer = new byte[5 * 1024 * 1024];

                // 合并所有分片
                for (ChunkInfo chunk : chunks) {
                    try (FileInputStream fis = new FileInputStream(chunk.getChunkPath())) {
                        int len;
                        while ((len = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                fos.flush();
            }

            // 获取当前运行目录
            String userDir = Paths.get(System.getProperty("user.dir"))
                    .toString()
                    .replace('\\', '/');

            // 返回完整的访问URL
            return userDir + "/" + uploadDir + "/" + relativePath;
        } catch (IOException e) {
            log.error("文件合并失败", e);
            throw new RuntimeException("文件合并失败");
        }
    }

    /**
     * 清理分片文件
     */
    @Override
    public void cleanupChunks(List<ChunkInfo> chunks) {
        for (ChunkInfo chunk : chunks) {
            try {
                // 删除分片文件
                Files.deleteIfExists(Paths.get(chunk.getChunkPath()));

                // 尝试删除分片所在的空目录
                Path chunkDir = Paths.get(chunk.getChunkPath()).getParent();
                if (Files.exists(chunkDir) && Files.isDirectory(chunkDir)) {
                    try (Stream<Path> files = Files.list(chunkDir)) {
                        if (files.findFirst().isEmpty()) {
                            Files.delete(chunkDir);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("清理分片文件失败: {}", chunk.getChunkPath(), e);
            }
        }
    }


    /**
     * 输出文件内容到HTTP响应流
     * @param url 文件路径
     * @param response HTTP响应对象
     */
    public static void view(String url, HttpServletResponse response) {
        // 1. 使用Path替代File，更好的跨平台兼容性
        Path filePath = Paths.get(url).normalize();

        // 2. 检查文件是否存在
        if (!Files.exists(filePath)) {
            throw new FileOperationException("文件不存在");
        }

        try {
            // 3. 使用try-with-resources自动关闭流
            // 使用Files.newInputStream替代FileInputStream，更现代的API
            // 使用8KB的缓冲区大小，这是一个比较好的默认值
            try (InputStream is = Files.newInputStream(filePath);
                 BufferedInputStream bis = new BufferedInputStream(is, 8192);
                 OutputStream out = response.getOutputStream()) {

                // 4. 使用较大的缓冲区提高传输效率（64KB）
                byte[] buffer = new byte[64 * 1024];
                int bytesRead;

                // 5. 循环读取并写入响应流
                while ((bytesRead = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);

                    // 6. 定期刷新缓冲区，避免内存占用过大
                    if (bytesRead == buffer.length) {
                        out.flush();
                    }
                }

                // 7. 最后确保所有数据都已写入
                out.flush();

            } // try-with-resources 会自动关闭所有流

        } catch (IOException e) {
            log.error("文件操作失败: {}", e.getMessage(), e);
            throw new FileOperationException("读取/下载文件出错");
        }
    }
}