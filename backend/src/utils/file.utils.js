const path = require('path');

class FileUtils {
    // Tạo tên file unique
    static generateUniqueFileName(userId, originalName) {
        const timestamp = Date.now();
        const extension = path.extname(originalName);
        return `${userId}_${timestamp}${extension}`;
    }

    // Lấy extension từ filename
    static getFileExtension(filename) {
        return path.extname(filename);
    }

    // Kiểm tra có phải file ảnh không
    static isImageFile(mimetype) {
        return mimetype.startsWith('image/');
    }

    // Chuyển đổi bytes thành string dễ đọc
    static formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // Tạo URL từ path
    static createFileUrl(basePath, filename) {
        return `${basePath}/${filename}`;
    }

    // Extract tên file từ đường dẫn đầy đủ
    static extractFileName(filePath) {
        if (!filePath) return null;
        
        // Nếu đã là tên file (không có path separator), trả về nguyên văn
        if (!filePath.includes('/') && !filePath.includes('\\')) {
            return filePath;
        }
        
        // Extract tên file từ đường dẫn
        return path.basename(filePath);
    }

    // Validate và clean filename
    static sanitizeFileName(filename) {
        if (!filename) return null;
        
        // Extract basename nếu có đường dẫn
        const cleanName = this.extractFileName(filename);
        
        // Loại bỏ các ký tự không an toàn
        return cleanName.replace(/[^a-zA-Z0-9.-_]/g, '_');
    }

    static deleteFile(filePath) {
        const fs = require('fs');
        if (fs.existsSync(filePath)) {
            fs.unlink(filePath, (err) => {
                if (err) {
                    console.error(`Lỗi khi xóa file ${filePath}:`, err);
                } else {
                    console.log(`File ${filePath} xóa thành công.`);
                }
            });
        }
    }

}

module.exports = FileUtils;
