package com.example.ossupload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS签名响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssSignatureResponse {
    
    /**
     * OSS访问密钥ID
     */
    private String accessKeyId;
    
    /**
     * 签名策略
     */
    private String policy;
    
    /**
     * 签名
     */
    private String signature;
    
    /**
     * OSS上传地址
     */
    private String host;
    
    /**
     * 文件在OSS中的键（路径）
     */
    private String key;
    
    /**
     * 签名过期时间（时间戳）
     */
    private Long expire;
    
    /**
     * 回调URL（可选）
     */
    private String callback;
    
    /**
     * 文件信息ID（用于后续更新文件状态）
     */
    private Long fileInfoId;
}