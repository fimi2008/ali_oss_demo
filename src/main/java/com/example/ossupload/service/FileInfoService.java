package com.example.ossupload.service;

import com.example.ossupload.entity.FileInfo;
import com.example.ossupload.repository.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文件信息服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileInfoService {
    
    private final FileInfoRepository fileInfoRepository;
    
    /**
     * 分页查询文件信息
     */
    public Page<FileInfo> getFileInfoPage(String originalName, Integer uploadStatus, 
                                         int page, int size, String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // 查询条件处理
        String nameCondition = StringUtils.hasText(originalName) ? originalName.trim() : null;
        
        return fileInfoRepository.findByConditions(nameCondition, uploadStatus, pageable);
    }
    
    /**
     * 根据ID获取文件信息
     */
    public Optional<FileInfo> getFileInfoById(Long id) {
        return fileInfoRepository.findById(id);
    }
    
    /**
     * 根据OSS键获取文件信息
     */
    public Optional<FileInfo> getFileInfoByOssKey(String ossKey) {
        return fileInfoRepository.findByOssKey(ossKey);
    }
    
    /**
     * 获取文件统计信息
     */
    public Map<String, Object> getFileStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总文件数
        long totalCount = fileInfoRepository.count();
        statistics.put("totalCount", totalCount);
        
        // 各状态文件数量
        List<Object[]> statusCounts = fileInfoRepository.countByUploadStatus();
        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("pending", 0L);    // 待上传
        statusMap.put("success", 0L);    // 上传成功
        statusMap.put("failed", 0L);     // 上传失败
        
        for (Object[] row : statusCounts) {
            Integer status = (Integer) row[0];
            Long count = (Long) row[1];
            
            switch (status) {
                case 0:
                    statusMap.put("pending", count);
                    break;
                case 1:
                    statusMap.put("success", count);
                    break;
                case 2:
                    statusMap.put("failed", count);
                    break;
            }
        }
        
        statistics.put("statusCounts", statusMap);
        
        return statistics;
    }
    
    /**
     * 删除文件信息
     */
    public void deleteFileInfo(Long id) {
        if (fileInfoRepository.existsById(id)) {
            fileInfoRepository.deleteById(id);
            log.info("删除文件信息成功，ID: {}", id);
        } else {
            throw new RuntimeException("文件信息不存在，ID: " + id);
        }
    }
    
    /**
     * 批量删除文件信息
     */
    public void deleteFileInfoBatch(List<Long> ids) {
        List<FileInfo> fileInfos = fileInfoRepository.findAllById(ids);
        if (fileInfos.size() != ids.size()) {
            throw new RuntimeException("部分文件信息不存在");
        }
        
        fileInfoRepository.deleteAll(fileInfos);
        log.info("批量删除文件信息成功，数量: {}", ids.size());
    }
}