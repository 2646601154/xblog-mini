package com.xblog.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.common.properties.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Slf4j
public class OssUtil {

    private final OSS ossClient;
    private final OssProperties ossProperties;

    public OssUtil(OSS ossClient, OssProperties ossProperties) {
        this.ossClient = ossClient;
        this.ossProperties = ossProperties;
    }

    /**
     * 上传文件到 OSS（默认 common 目录）
     *
     * @param file 文件
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "common");
    }

    /**
     * 上传文件到 OSS 指定目录
     *
     * @param file 文件
     * @param dir  存储目录（如 article、avatar）
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectKey = dir + "/" + today + "/" + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            PutObjectRequest request = new PutObjectRequest(
                    ossProperties.getBucketName(), objectKey, inputStream, metadata);
            ossClient.putObject(request);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败");
        }

        return ossProperties.getUrlPrefix() + "/" + objectKey;
    }

    /**
     * 删除 OSS 文件
     *
     * @param fileUrl 文件 URL
     */
    public void deleteFile(String fileUrl) {
        String objectName = extractObjectName(fileUrl);
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), objectName);
        } catch (Exception e) {
            log.error("删除OSS文件失败: {}", fileUrl, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除文件失败");
        }
    }

    /**
     * 获取文件访问 URL
     *
     * @param fileName 文件名
     * @return 文件访问 URL
     */
    public String getFileUrl(String fileName) {
        return ossProperties.getUrlPrefix() + "/" + fileName;
    }

    /**
     * 从 URL 中提取对象名称
     *
     * @param fileUrl 文件 URL
     * @return 对象名称
     */
    private String extractObjectName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件URL不能为空");
        }
        String urlPrefix = ossProperties.getUrlPrefix();
        if (fileUrl.startsWith(urlPrefix + "/")) {
            return fileUrl.substring(urlPrefix.length() + 1);
        }
        int idx = fileUrl.lastIndexOf("/");
        return idx >= 0 ? fileUrl.substring(idx + 1) : fileUrl;
    }
}

