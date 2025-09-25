package com.example.ossupload.controller;

import com.example.ossupload.dto.ApiResponse;
import com.example.ossupload.dto.FileUploadCallbackRequest;
import com.example.ossupload.dto.FileUploadRequest;
import com.example.ossupload.dto.OssSignatureResponse;
import com.example.ossupload.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * OSS文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OssController {
    
    private final OssService ossService;
    
    /**
     * 获取OSS上传签名
     */
    @PostMapping("/signature")
    public ApiResponse<OssSignatureResponse> getUploadSignature(@Valid @RequestBody FileUploadRequest request) {
        try {
            log.info("获取OSS上传签名请求: {}", request.getFileName());
            OssSignatureResponse signature = ossService.generateSignature(request);
            return ApiResponse.success(signature);
        } catch (Exception e) {
            log.error("获取OSS上传签名失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 文件上传回调
     */
    @PostMapping("/callback")
    public ApiResponse<Void> uploadCallback(@RequestBody FileUploadCallbackRequest request) {
        try {
            log.info("文件上传回调: fileInfoId={}, status={}", 
                    request.getFileInfoId(), request.getUploadStatus());
            ossService.handleUploadCallback(request);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("处理文件上传回调失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}