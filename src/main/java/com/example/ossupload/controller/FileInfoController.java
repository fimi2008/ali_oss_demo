package com.example.ossupload.controller;

import com.example.ossupload.dto.ApiResponse;
import com.example.ossupload.entity.FileInfo;
import com.example.ossupload.service.FileInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文件信息管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileInfoController {
    
    private final FileInfoService fileInfoService;
    
    /**
     * 分页查询文件信息
     */
    @GetMapping
    public ApiResponse<Page<FileInfo>> getFileInfoPage(
            @RequestParam(required = false) String originalName,
            @RequestParam(required = false) Integer uploadStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<FileInfo> result = fileInfoService.getFileInfoPage(
                    originalName, uploadStatus, page, size, sortBy, sortDir);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询文件信息失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID获取文件信息
     */
    @GetMapping("/{id}")
    public ApiResponse<FileInfo> getFileInfoById(@PathVariable Long id) {
        try {
            Optional<FileInfo> fileInfo = fileInfoService.getFileInfoById(id);
            if (fileInfo.isPresent()) {
                return ApiResponse.success(fileInfo.get());
            } else {
                return ApiResponse.error(404, "文件信息不存在");
            }
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getFileStatistics() {
        try {
            Map<String, Object> statistics = fileInfoService.getFileStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取文件统计信息失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除文件信息
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFileInfo(@PathVariable Long id) {
        try {
            fileInfoService.deleteFileInfo(id);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("删除文件信息失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除文件信息
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> deleteFileInfoBatch(@RequestBody List<Long> ids) {
        try {
            fileInfoService.deleteFileInfoBatch(ids);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("批量删除文件信息失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}