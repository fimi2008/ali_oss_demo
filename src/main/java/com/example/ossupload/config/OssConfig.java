package com.example.ossupload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OSS配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssConfig {
    
    /**
     * OSS服务端点
     */
    private String endpoint;
    
    /**
     * 访问密钥ID
     */
    private String accessKeyId;
    
    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;
    
    /**
     * 存储桶名称
     */
    private String bucketName;
    
    /**
     * 签名有效期（秒）
     */
    private Long signatureExpireTime = 3600L;
    
    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize = 104857600L; // 100MB
    
    /**
     * 允许的文件类型
     */
    private String allowedFileTypes;
}