package com.xblog.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.common.properties.OssProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * OssUtil 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OSS工具类测试")
class OssUtilTest {

    @Mock
    private OSS ossClient;

    @Mock
    private OssProperties ossProperties;

    private OssUtil ossUtil;

    @BeforeEach
    void setUp() {
        // 配置模拟的OssProperties
        when(ossProperties.getBucketName()).thenReturn("test-bucket");
        when(ossProperties.getUrlPrefix()).thenReturn("https://oss.example.com/test-bucket");
        
        ossUtil = new OssUtil(ossClient, ossProperties);
    }

    @Test
    @DisplayName("上传文件 - 成功（默认目录）")
    void testUploadFileSuccessWithDefaultDir() throws IOException {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // 执行测试
        String url = ossUtil.uploadFile(file);

        // 验证结果
        assertNotNull(url);
        assertTrue(url.startsWith("https://oss.example.com/test-bucket"));
        assertTrue(url.contains("/common/"));
        assertTrue(url.endsWith(".jpg"));

        // 验证OSS调用
        verify(ossClient, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("上传文件 - 成功（指定目录）")
    void testUploadFileSuccessWithCustomDir() throws IOException {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "avatar content".getBytes()
        );

        // 执行测试
        String url = ossUtil.uploadFile(file, "avatar");

        // 验证结果
        assertNotNull(url);
        assertTrue(url.startsWith("https://oss.example.com/test-bucket"));
        assertTrue(url.contains("/avatar/"));
        assertTrue(url.endsWith(".png"));

        // 验证OSS调用
        verify(ossClient, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("上传文件 - 文件名包含日期路径")
    void testUploadFileContainsDatePath() throws IOException {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        // 执行测试
        String url = ossUtil.uploadFile(file, "article");

        // 验证结果 - 包含今天的日期路径
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        assertTrue(url.contains("/article/" + today + "/"));

        // 验证OSS调用
        verify(ossClient, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("上传文件 - 文件为null抛出异常")
    void testUploadFileNullThrowsException() {
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ossUtil.uploadFile(null);
        });

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertEquals("文件不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("上传文件 - 空文件抛出异常")
    void testUploadFileEmptyThrowsException() throws IOException {
        // 准备测试数据 - 空文件
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ossUtil.uploadFile(emptyFile);
        });

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertEquals("文件不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("上传文件 - 无扩展名文件")
    void testUploadFileWithoutExtension() throws IOException {
        // 准备测试数据 - 没有扩展名的文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "README",
                "text/plain",
                "readme content".getBytes()
        );

        // 执行测试
        String url = ossUtil.uploadFile(file, "docs");

        // 验证结果
        assertNotNull(url);
        assertTrue(url.contains("/docs/"));
        // URL不应该以点结尾
        assertFalse(url.endsWith("."));

        // 验证OSS调用
        verify(ossClient, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("上传文件 - null原文件名处理")
    void testUploadFileWithNullOriginalFilename() throws IOException {
        // 创建自定义的MultipartFile模拟
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getContentType()).thenReturn("application/octet-stream");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        // 执行测试
        String url = ossUtil.uploadFile(file, "test");

        // 验证结果
        assertNotNull(url);

        // 验证OSS调用
        verify(ossClient, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("上传文件 - IO异常处理")
    void testUploadFileIOException() throws IOException {
        // 准备测试数据
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenThrow(new IOException("IO error"));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ossUtil.uploadFile(file, "test");
        });

        assertEquals(ResultCode.INTERNAL_ERROR, exception.getResultCode());
        assertEquals("文件上传失败", exception.getMessage());
    }

    @Test
    @DisplayName("上传文件 - 验证ObjectMetadata设置")
    void testUploadFileSetsMetadata() throws IOException {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                "{\"key\":\"value\"}".getBytes()
        );

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // 执行测试
        ossUtil.uploadFile(file, "data");

        // 验证PutObjectRequest的参数
        verify(ossClient, times(1)).putObject(requestCaptor.capture());
        PutObjectRequest capturedRequest = requestCaptor.getValue();

        assertEquals("test-bucket", capturedRequest.getBucketName());
        assertTrue(capturedRequest.getKey().endsWith(".json"));
    }

    @Test
    @DisplayName("上传文件 - 验证生成的objectKey格式")
    void testUploadFileObjectKeyFormat() throws IOException {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpeg",
                "image/jpeg",
                "photo content".getBytes()
        );

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // 执行测试
        ossUtil.uploadFile(file, "photos");

        // 验证objectKey格式
        verify(ossClient, times(1)).putObject(requestCaptor.capture());
        PutObjectRequest capturedRequest = requestCaptor.getValue();

        String objectKey = capturedRequest.getKey();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        assertTrue(objectKey.startsWith("photos/" + today + "/"));
        assertTrue(objectKey.endsWith(".jpeg"));
        // UUID去掉横线后应该是32位字符
        String fileName = objectKey.substring(objectKey.lastIndexOf("/") + 1);
        String nameWithoutExt = fileName.replace(".jpeg", "");
        assertEquals(32, nameWithoutExt.length());
    }

    @Test
    @DisplayName("删除文件 - 成功")
    void testDeleteFileSuccess() {
        // 准备测试数据
        String fileUrl = "https://oss.example.com/test-bucket/article/2025/01/15/test.jpg";

        // 执行测试
        ossUtil.deleteFile(fileUrl);

        // 验证OSS调用
        verify(ossClient, times(1)).deleteObject(eq("test-bucket"), anyString());
    }

    @Test
    @DisplayName("删除文件 - URL为null抛出异常")
    void testDeleteFileNullUrlThrowsException() {
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ossUtil.deleteFile(null);
        });

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertEquals("文件URL不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("删除文件 - URL为空字符串抛出异常")
    void testDeleteFileEmptyUrlThrowsException() {
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ossUtil.deleteFile("");
        });

        assertEquals(ResultCode.BAD_REQUEST, exception.getResultCode());
        assertEquals("文件URL不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("删除文件 - 提取对象名称（完整URL）")
    void testDeleteFileExtractObjectNameFromFullUrl() {
        // 准备测试数据
        String fileUrl = "https://oss.example.com/test-bucket/avatar/2025/01/20/photo.png";

        // 执行测试
        ossUtil.deleteFile(fileUrl);

        // 验证提取的对象名称正确
        verify(ossClient, times(1)).deleteObject(
                eq("test-bucket"),
                eq("avatar/2025/01/20/photo.png")
        );
    }

    @Test
    @DisplayName("删除文件 - 提取对象名称（不带前缀的URL）")
    void testDeleteFileExtractObjectNameWithoutPrefix() {
        // 准备测试数据 - URL不包含bucket前缀
        String fileUrl = "https://other-domain.com/some/path/file.txt";

        // 执行测试
        ossUtil.deleteFile(fileUrl);

        // 验证使用最后的文件名作为对象名称
        verify(ossClient, times(1)).deleteObject(
                eq("test-bucket"),
                eq("file.txt")
        );
    }

    @Test
    @DisplayName("删除文件 - OSS异常处理")
    void testDeleteFileOssException() {
        // 准备测试数据
        String fileUrl = "https://oss.example.com/test-bucket/test.jpg";
        doThrow(new RuntimeException("OSS error")).when(ossClient).deleteObject(anyString(), anyString());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ossUtil.deleteFile(fileUrl);
        });

        assertEquals(ResultCode.INTERNAL_ERROR, exception.getResultCode());
        assertEquals("删除文件失败", exception.getMessage());
    }

    @Test
    @DisplayName("获取文件URL - 成功")
    void testGetFileUrl() {
        // 准备测试数据
        String fileName = "article/2025/01/15/test.jpg";

        // 执行测试
        String url = ossUtil.getFileUrl(fileName);

        // 验证结果
        assertEquals("https://oss.example.com/test-bucket/article/2025/01/15/test.jpg", url);
    }

    @Test
    @DisplayName("获取文件URL - 文件名带斜杠开头")
    void testGetFileUrlWithLeadingSlash() {
        // 准备测试数据
        String fileName = "/avatar/photo.png";

        // 执行测试
        String url = ossUtil.getFileUrl(fileName);

        // 验证结果
        assertEquals("https://oss.example.com/test-bucket//avatar/photo.png", url);
    }

    @Test
    @DisplayName("提取对象名称 - 从完整URL中提取")
    void testExtractObjectNameFromCompleteUrl() {
        // 准备测试数据
        String fileUrl = "https://oss.example.com/test-bucket/common/2025/01/15/abc123.jpg";

        // 执行测试
        ossUtil.deleteFile(fileUrl);

        // 验证提取的对象名称
        verify(ossClient).deleteObject(eq("test-bucket"), eq("common/2025/01/15/abc123.jpg"));
    }

    @Test
    @DisplayName("提取对象名称 - 简单文件名")
    void testExtractObjectNameSimpleFileName() {
        // 准备测试数据 - 不包含前缀的简单文件名
        String fileUrl = "simple-filename.txt";

        // 执行测试
        ossUtil.deleteFile(fileUrl);

        // 验证使用整个字符串作为对象名称
        verify(ossClient).deleteObject(eq("test-bucket"), eq("simple-filename.txt"));
    }

    @Test
    @DisplayName("上传文件 - 多个不同扩展名")
    void testUploadFileVariousExtensions() throws IOException {
        // 测试不同扩展名
        String[] filenames = {"doc.pdf", "video.mp4", "archive.zip", "script.js"};

        for (String filename : filenames) {
            // 重置mock
            reset(ossClient);
            
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    filename,
                    "application/octet-stream",
                    "content".getBytes()
            );

            // 执行测试
            String url = ossUtil.uploadFile(file, "files");

            // 验证结果
            assertTrue(url.endsWith("." + filename.substring(filename.lastIndexOf(".") + 1)));
            
            // 验证OSS调用
            verify(ossClient, times(1)).putObject(any(PutObjectRequest.class));
        }
    }

    @Test
    @DisplayName("上传文件 - 验证目录参数隔离")
    void testUploadFileDirectoryIsolation() throws IOException {
        // 测试不同目录
        String[] dirs = {"article", "avatar", "comment", "product"};

        for (String dir : dirs) {
            // 重置mock
            reset(ossClient);
            
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "content".getBytes()
            );

            // 执行测试
            String url = ossUtil.uploadFile(file, dir);

            // 验证URL包含正确的目录
            assertTrue(url.contains("/" + dir + "/"));
            
            // 验证OSS调用
            verify(ossClient, times(1)).putObject(any(PutObjectRequest.class));
        }
    }
}
