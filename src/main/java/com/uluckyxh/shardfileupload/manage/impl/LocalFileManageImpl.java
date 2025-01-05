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

            // 获取当前运行目录并规范化路径分隔符
            String userDir = Paths.get(System.getProperty("user.dir"))
                    .toString()
                    .replace('\\', '/');

            // 返回完整的访问URL（用正斜杠拼接路径）
            return userDir + "/" + tempDir + "/" + relativePath;
        } catch (IOException e) {
            log.error("分片上传失败", e);
            throw new RuntimeException("分片上传失败");
        }
    }

    /**
     * 合并分片文件为完整文件
     *
     * @param chunks 分片信息列表，包含每个分片的序号和存储路径等信息
     * @param targetFileName 目标文件名，合并后的完整文件名
     * @return 合并后文件的完整访问路径
     */
    @Override
    public String mergeChunks(List<ChunkInfo> chunks, String targetFileName) {
        try {
            // 1. 按日期生成最终文件的相对存储路径
            // 如: 2025/01/05/fileName.txt
            String relativePath = generateDatePath(targetFileName);

            // 2. 拼接上传目录和相对路径，得到最终文件的完整路径
            // 如: D:/uploads/2025/01/05/fileName.txt
            Path targetPath = Paths.get(uploadDir, relativePath);

            // 3. 创建目标文件所在的目录结构
            // 如果 D:/uploads/2025/01/05 不存在，则创建这些目录
            Files.createDirectories(targetPath.getParent());

            // 4. 开始合并文件
            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                // 5. 将分片按序号排序，确保按正确顺序合并
                // 如: chunk_1, chunk_2, chunk_3...
                chunks.sort(Comparator.comparing(ChunkInfo::getChunkNumber));

                // 6. 创建与配置的分片大小相同的缓冲区
                // 如果配置的分片大小为5MB，则缓冲区也为5MB
                byte[] buffer = new byte[chunkSize * 1024 * 1024];

                // 7. 依次读取每个分片文件，写入目标文件
                for (ChunkInfo chunk : chunks) {
                    try (FileInputStream fis = new FileInputStream(chunk.getChunkPath())) {
                        int len;
                        // 循环读取分片内容并写入
                        while ((len = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                // 8. 确保所有数据都写入磁盘
                fos.flush();
            }

            // 9. 获取当前应用运行目录，并统一使用正斜杠
            // 如: D:/myapp
            String userDir = Paths.get(System.getProperty("user.dir"))
                    .toString()
                    .replace('\\', '/');

            // 10. 返回可访问的完整URL路径
            // 如: D:/myapp/uploads/2025/01/05/fileName.txt
            return userDir + "/" + uploadDir + "/" + relativePath;

        } catch (IOException e) {
            log.error("文件合并失败", e);
            throw new RuntimeException("文件合并失败");
        }
    }

    /**
     * 清理分片文件和相关目录
     * 在文件合并完成后调用，用于清理临时的分片文件和空目录
     *
     * @param chunks 需要清理的分片信息列表
     */
    @Override
    public void cleanupChunks(List<ChunkInfo> chunks) {
        for (ChunkInfo chunk : chunks) {
            try {
                // 1. 删除分片文件
                // 如果文件存在则删除，不存在则忽略
                // 路径示例: temp/{uploadId}/chunk_1
                Files.deleteIfExists(Paths.get(chunk.getChunkPath()));

                // 2. 获取分片文件所在的目录
                // 路径示例: temp/{uploadId}
                Path chunkDir = Paths.get(chunk.getChunkPath()).getParent();

                // 3. 如果目录存在且是一个目录，则尝试删除
                if (Files.exists(chunkDir) && Files.isDirectory(chunkDir)) {
                    // 4. 使用 try-with-resources 确保流被正确关闭
                    try (Stream<Path> files = Files.list(chunkDir)) {
                        // 5. 检查目录是否为空
                        // files.findFirst().isEmpty() 返回 true 表示目录为空
                        if (files.findFirst().isEmpty()) {
                            // 6. 删除空目录
                            Files.delete(chunkDir);
                        }
                    }
                }
            } catch (IOException e) {
                // 7. 记录删除失败的情况，但不中断整个清理过程
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