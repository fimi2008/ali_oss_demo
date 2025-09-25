// 全局变量
let currentPage = 0;
let pageSize = 10;
let currentSort = 'createTime';
let currentSortDir = 'desc';

// DOM加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

// 初始化应用
function initializeApp() {
    setupFileUpload();
    loadFileList();
    loadStatistics();
    setupSearchForm();
}

// 设置文件上传功能
function setupFileUpload() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    const uploadBtn = document.getElementById('uploadBtn');

    // 点击上传区域触发文件选择
    uploadArea.addEventListener('click', () => {
        fileInput.click();
    });

    // 拖拽上传
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFileSelect(files[0]);
        }
    });

    // 文件选择
    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleFileSelect(e.target.files[0]);
        }
    });

    // 上传按钮点击
    uploadBtn.addEventListener('click', () => {
        const file = fileInput.files[0];
        if (file) {
            uploadFile(file);
        } else {
            showMessage('请先选择文件', 'error');
        }
    });
}

// 处理文件选择
function handleFileSelect(file) {
    const fileInfo = document.getElementById('fileInfo');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');
    const fileType = document.getElementById('fileType');

    fileName.textContent = file.name;
    fileSize.textContent = formatFileSize(file.size);
    fileType.textContent = file.type || '未知类型';

    fileInfo.classList.add('show');
    
    // 验证文件
    if (!validateFile(file)) {
        return;
    }
}

// 验证文件
function validateFile(file) {
    const maxSize = 100 * 1024 * 1024; // 100MB
    const allowedTypes = ['jpg', 'jpeg', 'png', 'gif', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'zip', 'rar'];
    
    // 检查文件大小
    if (file.size > maxSize) {
        showMessage('文件大小不能超过100MB', 'error');
        return false;
    }
    
    // 检查文件类型
    const extension = file.name.split('.').pop().toLowerCase();
    if (!allowedTypes.includes(extension)) {
        showMessage('不支持的文件类型: ' + extension, 'error');
        return false;
    }
    
    return true;
}

// 上传文件
async function uploadFile(file) {
    try {
        const uploadBtn = document.getElementById('uploadBtn');
        const progress = document.getElementById('progress');
        const progressBar = document.getElementById('progressBar');
        const progressText = document.getElementById('progressText');

        // 禁用上传按钮
        uploadBtn.disabled = true;
        uploadBtn.innerHTML = '<span class="loading"></span> 准备上传...';

        // 显示进度条
        progress.classList.add('show');
        updateProgress(0, '准备上传...');

        // 获取文件扩展名
        const extension = file.name.split('.').pop().toLowerCase();

        // 请求上传签名
        const signatureResponse = await fetch('/api/oss/signature', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                fileName: file.name,
                fileSize: file.size,
                contentType: file.type,
                fileExtension: extension
            })
        });

        const signatureResult = await signatureResponse.json();
        
        if (signatureResult.code !== 200) {
            throw new Error(signatureResult.message);
        }

        const signature = signatureResult.data;
        updateProgress(10, '获取签名成功，开始上传...');

        // 构建FormData
        const formData = new FormData();
        formData.append('key', signature.key);
        formData.append('policy', signature.policy);
        formData.append('OSSAccessKeyId', signature.accessKeyId);
        formData.append('signature', signature.signature);
        formData.append('file', file);

        // 上传到OSS
        const xhr = new XMLHttpRequest();
        
        // 监听上传进度
        xhr.upload.addEventListener('progress', (e) => {
            if (e.lengthComputable) {
                const percentComplete = Math.round((e.loaded / e.total) * 100);
                updateProgress(10 + percentComplete * 0.8, `上传中... ${percentComplete}%`);
            }
        });

        // 上传完成处理
        xhr.addEventListener('load', async () => {
            if (xhr.status === 204) {
                updateProgress(95, '上传完成，处理中...');
                
                // 通知后端上传成功
                await notifyUploadSuccess(signature.fileInfoId, signature.key, file.size);
                
                updateProgress(100, '上传成功！');
                showMessage('文件上传成功！', 'success');
                
                // 重置表单
                resetUploadForm();
                
                // 刷新文件列表和统计
                loadFileList();
                loadStatistics();
                
            } else {
                throw new Error('上传失败，状态码: ' + xhr.status);
            }
        });

        // 上传错误处理
        xhr.addEventListener('error', () => {
            throw new Error('网络错误，上传失败');
        });

        // 开始上传
        xhr.open('POST', signature.host);
        xhr.send(formData);

    } catch (error) {
        console.error('上传失败:', error);
        showMessage('上传失败: ' + error.message, 'error');
        
        // 重置上传状态
        const uploadBtn = document.getElementById('uploadBtn');
        uploadBtn.disabled = false;
        uploadBtn.textContent = '开始上传';
        
        const progress = document.getElementById('progress');
        progress.classList.remove('show');
    }
}

// 通知后端上传成功
async function notifyUploadSuccess(fileInfoId, ossKey, actualFileSize) {
    try {
        const response = await fetch('/api/oss/callback', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                fileInfoId: fileInfoId,
                ossKey: ossKey,
                actualFileSize: actualFileSize,
                uploadStatus: 1
            })
        });

        const result = await response.json();
        if (result.code !== 200) {
            console.error('通知后端失败:', result.message);
        }
    } catch (error) {
        console.error('通知后端失败:', error);
    }
}

// 更新进度条
function updateProgress(percent, text) {
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');
    
    progressBar.style.width = percent + '%';
    progressText.textContent = text;
}

// 重置上传表单
function resetUploadForm() {
    const fileInput = document.getElementById('fileInput');
    const fileInfo = document.getElementById('fileInfo');
    const progress = document.getElementById('progress');
    const uploadBtn = document.getElementById('uploadBtn');

    fileInput.value = '';
    fileInfo.classList.remove('show');
    progress.classList.remove('show');
    uploadBtn.disabled = false;
    uploadBtn.textContent = '开始上传';
}

// 加载文件列表
async function loadFileList() {
    try {
        const searchForm = document.getElementById('searchForm');
        const formData = new FormData(searchForm);
        
        const params = new URLSearchParams();
        params.append('page', currentPage);
        params.append('size', pageSize);
        params.append('sortBy', currentSort);
        params.append('sortDir', currentSortDir);
        
        if (formData.get('originalName')) {
            params.append('originalName', formData.get('originalName'));
        }
        if (formData.get('uploadStatus')) {
            params.append('uploadStatus', formData.get('uploadStatus'));
        }

        const response = await fetch('/api/files?' + params.toString());
        const result = await response.json();

        if (result.code === 200) {
            renderFileList(result.data);
        } else {
            showMessage('加载文件列表失败: ' + result.message, 'error');
        }
    } catch (error) {
        console.error('加载文件列表失败:', error);
        showMessage('加载文件列表失败: ' + error.message, 'error');
    }
}

// 渲染文件列表
function renderFileList(pageData) {
    const tbody = document.getElementById('fileTableBody');
    const pagination = document.getElementById('pagination');
    
    // 清空表格
    tbody.innerHTML = '';
    
    if (pageData.content.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #6c757d;">暂无数据</td></tr>';
    } else {
        pageData.content.forEach(file => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${file.id}</td>
                <td title="${file.originalName}">${truncateText(file.originalName, 30)}</td>
                <td>${formatFileSize(file.fileSize)}</td>
                <td>${file.fileExtension || '-'}</td>
                <td>${getStatusBadge(file.uploadStatus)}</td>
                <td>${formatDateTime(file.createTime)}</td>
                <td>
                    ${file.uploadStatus === 1 && file.ossUrl ? 
                        `<a href="${file.ossUrl}" target="_blank" class="btn btn-primary" style="padding: 0.25rem 0.5rem; font-size: 0.8rem;">查看</a>` : 
                        '-'
                    }
                    <button onclick="deleteFile(${file.id})" class="btn btn-danger" style="padding: 0.25rem 0.5rem; font-size: 0.8rem;">删除</button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }
    
    // 渲染分页
    renderPagination(pageData, pagination);
}

// 渲染分页
function renderPagination(pageData, container) {
    container.innerHTML = '';
    
    const totalPages = pageData.totalPages;
    const currentPageNum = pageData.number;
    
    // 上一页按钮
    const prevBtn = document.createElement('button');
    prevBtn.textContent = '上一页';
    prevBtn.disabled = currentPageNum === 0;
    prevBtn.onclick = () => {
        if (currentPageNum > 0) {
            currentPage = currentPageNum - 1;
            loadFileList();
        }
    };
    container.appendChild(prevBtn);
    
    // 页码信息
    const pageInfo = document.createElement('span');
    pageInfo.textContent = `第 ${currentPageNum + 1} 页，共 ${totalPages} 页`;
    pageInfo.style.margin = '0 1rem';
    container.appendChild(pageInfo);
    
    // 下一页按钮
    const nextBtn = document.createElement('button');
    nextBtn.textContent = '下一页';
    nextBtn.disabled = currentPageNum >= totalPages - 1;
    nextBtn.onclick = () => {
        if (currentPageNum < totalPages - 1) {
            currentPage = currentPageNum + 1;
            loadFileList();
        }
    };
    container.appendChild(nextBtn);
}

// 加载统计信息
async function loadStatistics() {
    try {
        const response = await fetch('/api/files/statistics');
        const result = await response.json();

        if (result.code === 200) {
            renderStatistics(result.data);
        }
    } catch (error) {
        console.error('加载统计信息失败:', error);
    }
}

// 渲染统计信息
function renderStatistics(data) {
    document.getElementById('totalCount').textContent = data.totalCount;
    document.getElementById('successCount').textContent = data.statusCounts.success;
    document.getElementById('pendingCount').textContent = data.statusCounts.pending;
    document.getElementById('failedCount').textContent = data.statusCounts.failed;
}

// 设置搜索表单
function setupSearchForm() {
    const searchForm = document.getElementById('searchForm');
    const searchBtn = document.getElementById('searchBtn');
    const resetBtn = document.getElementById('resetBtn');

    searchBtn.addEventListener('click', (e) => {
        e.preventDefault();
        currentPage = 0;
        loadFileList();
    });

    resetBtn.addEventListener('click', (e) => {
        e.preventDefault();
        searchForm.reset();
        currentPage = 0;
        loadFileList();
    });
}

// 删除文件
async function deleteFile(id) {
    if (!confirm('确定要删除这个文件吗？')) {
        return;
    }

    try {
        const response = await fetch(`/api/files/${id}`, {
            method: 'DELETE'
        });

        const result = await response.json();
        
        if (result.code === 200) {
            showMessage('删除成功', 'success');
            loadFileList();
            loadStatistics();
        } else {
            showMessage('删除失败: ' + result.message, 'error');
        }
    } catch (error) {
        console.error('删除失败:', error);
        showMessage('删除失败: ' + error.message, 'error');
    }
}

// 工具函数

// 格式化文件大小
function formatFileSize(bytes) {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// 格式化日期时间
function formatDateTime(dateTimeStr) {
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-CN');
}

// 截断文本
function truncateText(text, maxLength) {
    if (text.length <= maxLength) {
        return text;
    }
    return text.substring(0, maxLength) + '...';
}

// 获取状态标签
function getStatusBadge(status) {
    switch (status) {
        case 0:
            return '<span class="status-badge status-pending">待上传</span>';
        case 1:
            return '<span class="status-badge status-success">上传成功</span>';
        case 2:
            return '<span class="status-badge status-failed">上传失败</span>';
        default:
            return '<span class="status-badge">未知</span>';
    }
}

// 显示消息
function showMessage(message, type = 'info') {
    // 移除现有消息
    const existingMessage = document.querySelector('.message');
    if (existingMessage) {
        existingMessage.remove();
    }

    // 创建新消息
    const messageDiv = document.createElement('div');
    messageDiv.className = `message message-${type} fade-in`;
    messageDiv.textContent = message;

    // 插入到页面顶部
    const container = document.querySelector('.container');
    container.insertBefore(messageDiv, container.firstChild);

    // 3秒后自动移除
    setTimeout(() => {
        if (messageDiv.parentNode) {
            messageDiv.remove();
        }
    }, 3000);
}