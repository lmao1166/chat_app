const path = require('path');
const { profileUploadDir, chatUploadDir } = require('../configs/upload.config');
const uploadService = require('../services/upload.service');

class FileServeController {
    // Phục vụ ảnh profile
    serveProfileImage(req, res) {
        const filename = req.params.filename;
        const filePath = path.join(profileUploadDir, filename);
        
        if (uploadService.fileExists(filePath)) {
            res.sendFile(filePath);
        } else {
            res.status(404).json({
                success: false,
                message: 'Không tìm thấy ảnh profile'
            });
        }
    }

    // Phục vụ ảnh chat
    serveChatImage(req, res) {
        const { conversationId, filename } = req.params;
        const filePath = path.join(chatUploadDir, conversationId, filename);
        
        if (uploadService.fileExists(filePath)) {
            res.sendFile(filePath);
        } else {
            res.status(404).json({
                success: false,
                message: 'Không tìm thấy ảnh chat'
            });
        }
    }
}

module.exports = new FileServeController();
