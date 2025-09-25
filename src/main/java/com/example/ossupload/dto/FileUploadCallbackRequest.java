package com.example.ossupload.dto;

import lombok.Data;

/**
 * 文件上传回调请求DTO
 */
@Data
public class FileUploadCallbackRequest {
    
    /**
     * 文件信息ID
     */
    private Long fileInfoId;
    
    /**
     * OSS文件键
     */
    private String ossKey;
    
    /**
     * 实际文件大小
     */
    private Long actualFileSize;
    
    /**
     * ETag
     */
    private String etag;
    
    /**
     * 上传状态：1-成功，2-失败
     */
    private Integer uploadStatus;
    
    /**
     * 错误信息（上传失败时）
     */
    private String errorMessage;
}