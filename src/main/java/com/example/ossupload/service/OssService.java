package com.example.ossupload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.example.ossupload.config.OssConfig;
import com.example.ossupload.dto.FileUploadCallbackRequest;
import com.example.ossupload.dto.FileUploadRequest;
import com.example.ossupload.dto.OssSignatureResponse;
import com.example.ossupload.entity.FileInfo;
import com.example.ossupload.repository.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * OSS服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {
    
    private final OssConfig ossConfig;
    private final FileInfoRepository fileInfoRepository;
    
    /**
     * 生成OSS上传签名
     */
    @Transactional
    public OssSignatureResponse generateSignature(FileUploadRequest request) {
        try {
            // 验证文件
            validateFile(request);
            
            // 生成文件键
            String fileKey = generateFileKey(request.getFileName());
            
            // 保存文件信息到数据库
            FileInfo fileInfo = createFileInfo(request, fileKey);
            fileInfo = fileInfoRepository.save(fileInfo);
            
            // 创建OSS客户端
            OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(), 
                ossConfig.getAccessKeyId(), 
                ossConfig.getAccessKeySecret()
            );
            
            try {
                // 设置过期时间
                long expireTime = System.currentTimeMillis() + ossConfig.getSignatureExpireTime() * 1000;
                Date expiration = new Date(expireTime);
                
                // 创建PostObject请求的Policy
                PolicyConditions policyConds = new PolicyConditions();
                policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, ossConfig.getMaxFileSize());
                policyConds.addConditionItem(MatchMode.Exact, PolicyConditions.COND_KEY, fileKey);
                
                String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
                byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
                String encodedPolicy = BinaryUtil.toBase64String(binaryData);
                String postSignature = ossClient.calculatePostSignature(postPolicy);
                
                // 构建响应
                return OssSignatureResponse.builder()
                        .accessKeyId(ossConfig.getAccessKeyId())
                        .policy(encodedPolicy)
                        .signature(postSignature)
                        .host(ossConfig.getEndpoint().replace("https://", "https://" + ossConfig.getBucketName() + "."))
                        .key(fileKey)
                        .expire(expireTime / 1000)
                        .fileInfoId(fileInfo.getId())
                        .build();
                        
            } finally {
                ossClient.shutdown();
            }
            
        } catch (Exception e) {
            log.error("生成OSS签名失败", e);
            throw new RuntimeException("生成OSS签名失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理文件上传回调
     */
    @Transactional
    public void handleUploadCallback(FileUploadCallbackRequest request) {
        try {
            FileInfo fileInfo = fileInfoRepository.findById(request.getFileInfoId())
                    .orElseThrow(() -> new RuntimeException("文件信息不存在"));
            
            // 更新文件信息
            fileInfo.setUploadStatus(request.getUploadStatus());
            if (request.getActualFileSize() != null) {
                fileInfo.setFileSize(request.getActualFileSize());
            }
            
            if (request.getUploadStatus() == 1) {
                // 上传成功，生成访问URL
                String ossUrl = generateOssUrl(fileInfo.getOssKey());
                fileInfo.setOssUrl(ossUrl);
            }
            
            fileInfoRepository.save(fileInfo);
            
            log.info("文件上传回调处理完成，文件ID: {}, 状态: {}", 
                    request.getFileInfoId(), request.getUploadStatus());
                    
        } catch (Exception e) {
            log.error("处理文件上传回调失败", e);
            throw new RuntimeException("处理文件上传回调失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(FileUploadRequest request) {
        // 验证文件大小
        if (request.getFileSize() > ossConfig.getMaxFileSize()) {
            throw new RuntimeException("文件大小超过限制，最大允许: " + 
                    (ossConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }
        
        // 验证文件类型
        if (StringUtils.hasText(ossConfig.getAllowedFileTypes()) && 
            StringUtils.hasText(request.getFileExtension())) {
            List<String> allowedTypes = Arrays.asList(
                ossConfig.getAllowedFileTypes().toLowerCase().split(","));
            if (!allowedTypes.contains(request.getFileExtension().toLowerCase())) {
                throw new RuntimeException("不支持的文件类型: " + request.getFileExtension());
            }
        }
    }
    
    /**
     * 生成文件键
     */
    private String generateFileKey(String originalFileName) {
        // 按日期分目录
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String datePath = sdf.format(new Date());
        
        // 生成唯一文件名
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = "";
        if (originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        return "uploads/" + datePath + "/" + uuid + extension;
    }
    
    /**
     * 创建文件信息对象
     */
    private FileInfo createFileInfo(FileUploadRequest request, String fileKey) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalName(request.getFileName());
        fileInfo.setOssKey(fileKey);
        fileInfo.setFileSize(request.getFileSize());
        fileInfo.setContentType(request.getContentType());
        fileInfo.setFileExtension(request.getFileExtension());
        fileInfo.setRemark(request.getRemark());
        fileInfo.setUploadStatus(0); // 待上传
        return fileInfo;
    }
    
    /**
     * 生成OSS访问URL
     */
    private String generateOssUrl(String ossKey) {
        return ossConfig.getEndpoint().replace("https://", "https://" + ossConfig.getBucketName() + ".") 
                + "/" + ossKey;
    }
}