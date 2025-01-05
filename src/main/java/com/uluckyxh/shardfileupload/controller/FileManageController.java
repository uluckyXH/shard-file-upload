package com.uluckyxh.shardfileupload.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.uluckyxh.shardfileupload.config.excepition.FileOperationException;
import com.uluckyxh.shardfileupload.constant.FileConstant;
import com.uluckyxh.shardfileupload.entity.ChunkInfo;
import com.uluckyxh.shardfileupload.entity.FileInfo;
import com.uluckyxh.shardfileupload.enums.FileStatus;
import com.uluckyxh.shardfileupload.manage.FileManage;
import com.uluckyxh.shardfileupload.manage.FileManageFactory;
import com.uluckyxh.shardfileupload.manage.impl.LocalFileManageImpl;
import com.uluckyxh.shardfileupload.service.ChunkInfoService;
import com.uluckyxh.shardfileupload.service.FileInfoService;
import com.uluckyxh.shardfileupload.util.FileSizeUtil;
import com.uluckyxh.shardfileupload.util.IdGeneratorUtil;
import com.uluckyxh.shardfileupload.vo.UploadConfig;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.uluckyxh.shardfileupload.common.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@Transactional
@RequestMapping("/file")
public class FileManageController {

    // 存储类型，本地还是OSS
    @Value("${file.upload.storageType}")
    private String storageType;

    // 单文件大小
    @Value("${file.upload.maxFileSize}")
    private Integer maxFileSize;

    // 本地预览地址
    @Value("${file.upload.previewUrl}")
    private String previewUrl;

    // 单个分片文件大小chunkSize
    @Value("${file.upload.chunkSize}")
    private Integer chunkSize;

    @Autowired
    private ChunkInfoService chunkInfoService;

    @Autowired
    private FileInfoService fileInfoService;

    @Autowired
    private FileManageFactory fileManageFactory;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result<Object> upload(@RequestParam MultipartFile file,
                                 @RequestParam(required = false) String md5) {
        // 判断文件是否为空
        if (file.isEmpty()) {
            return Result.error("文件数据为空");
        }

        long fileSize = file.getSize();
        long mbToBytes = FileSizeUtil.mbToBytes(maxFileSize);
        // 判断文件大小
        if (fileSize > mbToBytes) {
            return Result.error("文件过大，超出" + maxFileSize + "MB限制");
        }

        // 给文件名重新命名
        String fileName = file.getOriginalFilename();
        if (StrUtil.isBlank(fileName)) {
            throw new FileOperationException("文件名为空");
        }

        // 文件名后缀
        String fileExt = fileName.substring(fileName.lastIndexOf("."));
        if (StrUtil.isBlank(fileExt)) {
            throw new FileOperationException("无法获取文件后缀，请确认文件是否有后缀");
        }
        // 去除后缀前面的.
        fileExt = fileExt.substring(1);

        // 重新命名文件，前面加上ID + 原本文件名
        String newFileName = IdGeneratorUtil.simpleUUID() + "_" + fileName;
        // 构建文件信息
        FileInfo fileInfo = new FileInfo();
        // 定义返回的URL
        String resultUrl = "";
        // 文件上传
        try {
            // 直接用inputStream
            InputStream inputStream = file.getInputStream();
            // 文件上传
            FileManage fileManage = fileManageFactory.getFileManage(storageType);
            resultUrl = fileManage.upload(inputStream, newFileName);
            // 源文件名
            fileInfo.setOriginalFileName(fileName);
            // 重命名后的文件名
            fileInfo.setFileName(newFileName);
            // 文件后缀名
            fileInfo.setFileExt(fileExt);
            // 文件MD5
            fileInfo.setMd5(md5);
            // 文件状态，上传成功
            fileInfo.setStatus(FileStatus.SUCCESS.getCode());
            // 存储类型
            fileInfo.setStorageType(storageType);
            // 文件访问路径
            fileInfo.setAccessUrl(resultUrl);
            // backetName
            fileInfo.setBucketName(fileManage.getBucketName());
            // 文件大小
            fileInfo.setFileSize(fileSize);

            // 保存文件信息
            boolean save = fileInfoService.save(fileInfo);
            if (!save) {
                throw new FileOperationException("文件信息保存失败");
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Result.error("文件上传异常");
        }

        // 判断如果是本地上传
        if (FileConstant.LOCAL.equals(storageType)) {
            // 如果是本地上传，拼接一个预览地址
            resultUrl = previewUrl + fileInfo.getId();
        }

        return Result.success("文件上传成功", resultUrl);
    }

    /**
     * 文件预览/下载接口
     *
     * @param id       文件ID
     * @param filename 自定义下载文件名（可选）
     * @param preview  是否为预览模式（默认false表示下载模式）
     * @param charset  文件编码（默认UTF-8）
     */
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public void view(@PathVariable String id,
                     @RequestParam(required = false) String filename,
                     @RequestParam(required = false, defaultValue = "true") Boolean preview,
                     @RequestParam(required = false, defaultValue = "UTF-8") String charset,
                     HttpServletResponse response) throws IOException {

        // 1. 获取文件信息
        FileInfo file = fileInfoService.getById(id);
        if (file == null) {
            throw new FileOperationException("文件ID：" + id + "不存在");
        }

        // 只允许本地文件能预览
        if (!FileConstant.LOCAL.equals(file.getStorageType())) {
            throw new FileOperationException("暂不支持该存储类型的文件预览");
        }

        // 2. 如果未指定下载文件名，使用原始文件名
        if (StrUtil.isBlank(filename)) {
            filename = file.getOriginalFileName();
        }

        // 3. 设置响应头
        if (!preview) {
            // 下载模式：设置文件下载头，filename需要URL编码防止中文乱码
            response.addHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(filename, StandardCharsets.UTF_8));
        }
        // 设置文件大小
        response.setContentLengthLong(file.getFileSize());
        // 设置文件类型和编码
        response.setContentType(file.getFileExt() + ";charset=" + charset);

        // 4. 支持断点续传的响应头设置
        response.addHeader("Accept-Ranges", "bytes");
        if (file.getFileSize() != null && file.getFileSize() > 0) {
            // 设置内容范围，format: bytes start-end/total
            response.addHeader("Content-Range", "bytes " + 0 + "-" +
                    (file.getFileSize() - 1) + "/" + file.getFileSize());
        }

        // 5. 设置响应缓冲区大小为10MB
        response.setBufferSize(10 * 1024 * 1024);

        // 6. 调用文件管理服务输出文件内容到响应流
        LocalFileManageImpl.view(file.getAccessUrl(), response);
    }

    /**
     * 初始化分片上传
     */
    @PostMapping("/initiateMultipartUpload")
    public Result<FileInfo> initiateMultipartUpload(@Valid @RequestBody FileInfo fileInfo) {
        if (null == fileInfo) {
            throw new FileOperationException("文件信息为空");
        }

        // 拿到源文件名
        String originalFileName = fileInfo.getOriginalFileName();
        // 拿到后缀
        String fileExt = originalFileName.substring(originalFileName.lastIndexOf("."));
        if (StrUtil.isBlank(fileExt)) {
            throw new FileOperationException("无法获取文件后缀，请确认文件是否有后缀");
        }

        // 拿到文件大小
        long fileSize = fileInfo.getFileSize();
        // 转换maxFileSize从MB到bytes
        long maxFileSizeBytes = FileSizeUtil.mbToBytes(maxFileSize);

        // 判断文件大小
        if (fileSize > maxFileSizeBytes) {
            throw new FileOperationException("文件过大，超出" + maxFileSize + "MB限制" +
                    "（当前文件大小：" + FileSizeUtil.formatFileSize(fileSize) + "）");
        }

        // 重命名文件名
        String newFileName = IdGeneratorUtil.simpleUUID() + "_" + originalFileName;
        fileInfo.setFileName(newFileName);
        // 去掉后缀前面的.
        fileExt = fileExt.substring(1);
        // 文件后缀名
        fileInfo.setFileExt(fileExt);
        // 存储类型
        fileInfo.setStorageType(storageType);
        // 生成分片上传的唯一标识
        fileInfo.setUploadId(IdGeneratorUtil.simpleUUID());
        // 文件状态，上传中
        fileInfo.setStatus(FileStatus.UPLOADING.getCode());
        // 存储空间名称
        fileInfo.setBucketName(fileManageFactory.getFileManage(storageType).getBucketName());


        // 保存文件信息
        boolean save = fileInfoService.save(fileInfo);
        if (!save) {
            throw new FileOperationException("文件信息保存失败");
        }

        return Result.success(fileInfo);
    }

    /**
     * 上传分片
     */
    @PostMapping("/uploadChunk")
    public Result<?> uploadChunk(@RequestParam String uploadId,
                                 @RequestParam Integer chunkNumber,
                                 @RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("分片文件为空");
        }

        // 获取文件信息
        FileInfo fileInfo = fileInfoService.getByUploadId(uploadId);
        if (null == fileInfo) {
            throw new FileOperationException("文件记录不存在");
        }

        // 单个分片文件大小
        long fileSize = file.getSize();

        // 计算限制大小
        long mbToBytes = FileSizeUtil.mbToBytes(chunkSize);
        if (fileSize > mbToBytes) {
            return Result.error("分片文件过大，超出" + chunkSize + "MB限制");
        }

        try {
            // 分片文件路径
            String chunkFileName = uploadId + "_" + chunkNumber;

            // 保存分片文件
            FileManage fileManage = fileManageFactory.getFileManage(storageType);
            String chunkUrl = fileManage.uploadChunk(file.getInputStream(), chunkFileName);

            // 记录分片信息
            ChunkInfo chunkInfo = new ChunkInfo();
            chunkInfo.setUploadId(uploadId); // 上传ID
            chunkInfo.setChunkNumber(chunkNumber); // 分片序号
            chunkInfo.setChunkPath(chunkUrl); // 分片路径
            chunkInfo.setUploadStatus(FileStatus.SUCCESS.getCode()); // 上传成功
            chunkInfo.setFileName(fileInfo.getFileName()); // 文件名
            chunkInfo.setOriginalFileName(fileInfo.getOriginalFileName()); // 源文件名
            chunkInfo.setStorageType(storageType); // 存储类型
            chunkInfo.setFileSize(file.getSize()); // 文件大小
            chunkInfo.setBucketName(fileInfo.getBucketName()); // 存储空间名称

            // 保存分片信息
            boolean save = chunkInfoService.save(chunkInfo);
            if (!save) {
                throw new FileOperationException("分片信息保存失败");
            }

            return Result.success();
        } catch (IOException e) {
            log.error("分片上传失败", e);
            return Result.error("分片上传失败");
        }
    }

    /**
     * 合并分片
     */
    @PostMapping("/mergeChunks")
    public Result<?> mergeChunks(@RequestParam String uploadId) {
        // 获取文件信息
        FileInfo fileInfo = fileInfoService.getByUploadId(uploadId);
        if (null == fileInfo) {
            throw new FileOperationException("文件记录不存在");
        }

        // 获取所有分片信息
        List<ChunkInfo> chunks = chunkInfoService.getByUploadId(uploadId);
        if (chunks.isEmpty()) {
            throw new FileOperationException("没有找到分片文件");
        }

        try {
            // 合并文件
            FileManage fileManage = fileManageFactory.getFileManage(storageType);
            String resultUrl = fileManage.mergeChunks(chunks, fileInfo.getFileName());

            // 更新文件信息
            fileInfo.setStatus(FileStatus.SUCCESS.getCode());
            fileInfo.setAccessUrl(resultUrl);
            fileInfoService.updateById(fileInfo);

            // 清理分片文件和记录
            fileManage.cleanupChunks(chunks);
            chunkInfoService.removeByUploadId(uploadId);

            // 如果是本地存储，需要拼接预览地址
            if (FileConstant.LOCAL.equals(storageType)) {
                resultUrl = previewUrl + fileInfo.getId();
            }

            return Result.success("文件合并成功", resultUrl);
        } catch (Exception e) {
            // 合并失败，更新文件状态
            fileInfo.setStatus(FileStatus.FAILED.getCode());
            fileInfoService.updateById(fileInfo);

            log.error("文件合并失败", e);
            return Result.error("文件合并失败");
        }
    }

    /**
     * 获取配置
     */
    @GetMapping("/config")
    public Result<UploadConfig> getUploadConfig() {
        return Result.success(UploadConfig.builder()
                .chunkSize(chunkSize)
                .maxFileSize(maxFileSize)
                .storageType(storageType)
                .build());
    }

    /**
     * 分页查询文件列表
     */
    @GetMapping("/getByPage")
    public Result<IPage<FileInfo>> getByPage(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String fileName) {
        return Result.success(fileInfoService.getByPage(page, size, fileName));
    }

    /**
     * detail
     */
    @GetMapping("/detail/{id}")
    public Result<FileInfo> detail(@PathVariable String id) {
        return Result.success(fileInfoService.getById(id));
    }

}
