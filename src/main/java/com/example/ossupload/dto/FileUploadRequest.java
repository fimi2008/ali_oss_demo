package com.example.ossupload.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 文件上传请求DTO
 */
@Data
public class FileUploadRequest {
    
    /**
     * 原始文件名
     */
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String contentType;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 备注
     */
    private String remark;
}