package com.example.ossupload.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件信息实体类
 */
@Data
@Entity
@Table(name = "file_info")
public class FileInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false)
    private String originalName;
    
    /**
     * OSS中的文件名（包含路径）
     */
    @Column(name = "oss_key", nullable = false, unique = true)
    private String ossKey;
    
    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;
    
    /**
     * 文件类型
     */
    @Column(name = "content_type")
    private String contentType;
    
    /**
     * 文件扩展名
     */
    @Column(name = "file_extension")
    private String fileExtension;
    
    /**
     * OSS访问URL
     */
    @Column(name = "oss_url", length = 500)
    private String ossUrl;
    
    /**
     * 上传状态：0-待上传，1-上传成功，2-上传失败
     */
    @Column(name = "upload_status")
    private Integer uploadStatus = 0;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;
}