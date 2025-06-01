const fs = require('fs');
const userService = require('./user.service');
const FileUtils = require('../utils/file.utils');

class UploadService {    // Upload profile picture
    async uploadProfilePicture(userId, file) {
        try {
            if (!file) {
                const error = new Error('Vui lòng chọn file ảnh để upload');
                error.statusCode = 400;
                throw error;
            }            
            
            // Chỉ lưu tên file vào database (không lưu đường dẫn đầy đủ)
            const profilePicUrl = FileUtils.extractFileName(file.filename);
            
            // Cập nhật profilePicUrl trong database
            await userService.updateUser(userId, { profilePicUrl });

            const domain = process.env.DOMAIN || 'localhost:3000';
            return {
                profilePicUrl: `http://${domain}/api/v1/uploads/profiles/${file.filename}`,
                filename: file.filename
            };
        } catch (error) {
            // Xóa file nếu có lỗi
            if (file) {
                this.deleteFile(file.path);
            }
            throw error;
        }
    }// Upload chat image
    async uploadChatImage(conversationId, file) {
        try {
            if (!file) {
                const error = new Error('Vui lòng chọn file ảnh để upload');
                error.statusCode = 400;
                throw error;
            }

            if (!conversationId) {
                const error = new Error('Conversation ID là bắt buộc');
                error.statusCode = 400;
                throw error;
            }

            // Tạo URL để truy cập ảnh với domain prefix
            const domain = process.env.DOMAIN || 'localhost:3000';
            const imageUrl = `http://${domain}/api/v1/uploads/chats/${conversationId}/${file.filename}`;

            return {
                imageUrl,
                filename: file.filename,
                conversationId: conversationId
            };
        } catch (error) {
            // Xóa file nếu có lỗi
            if (file) {
                this.deleteFile(file.path);
            }
            throw error;
        }
    }

    // Xóa file
    deleteFile(filePath) {
        fs.unlink(filePath, (err) => {
            if (err) console.error('Lỗi khi xóa file:', err);
        });
    }

    // Kiểm tra file có tồn tại
    fileExists(filePath) {
        return fs.existsSync(filePath);
    }
}

module.exports = new UploadService();
