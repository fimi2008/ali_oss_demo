package com.example.ossupload.repository;

import com.example.ossupload.entity.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文件信息数据访问层
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    
    /**
     * 根据OSS键查找文件信息
     */
    Optional<FileInfo> findByOssKey(String ossKey);
    
    /**
     * 根据上传状态查找文件列表
     */
    List<FileInfo> findByUploadStatus(Integer uploadStatus);
    
    /**
     * 分页查询文件信息
     */
    @Query("SELECT f FROM FileInfo f WHERE " +
           "(:originalName IS NULL OR f.originalName LIKE %:originalName%) AND " +
           "(:uploadStatus IS NULL OR f.uploadStatus = :uploadStatus)")
    Page<FileInfo> findByConditions(@Param("originalName") String originalName,
                                   @Param("uploadStatus") Integer uploadStatus,
                                   Pageable pageable);
    
    /**
     * 统计各状态文件数量
     */
    @Query("SELECT f.uploadStatus, COUNT(f) FROM FileInfo f GROUP BY f.uploadStatus")
    List<Object[]> countByUploadStatus();
}