const uploadService = require('../services/upload.service');

class UploadController {
    // Upload profile picture
    async uploadProfilePicture(req, res, next) {
        try {
            const result = await uploadService.uploadProfilePicture(req.user.userId, req.file);
            
            res.status(200).json({
                status: 200,
                success: true,
                data: result,
                message: 'Upload ảnh profile thành công'
            });
        } catch (error) {
            next(error);
        }
    }

    // Upload chat image
    async uploadChatImage(req, res, next) {
        try {
            const conversationId = req.body.conversationId;
            const result = await uploadService.uploadChatImage(conversationId, req.file);

            res.status(200).json({
                status: 200,
                success: true,
                data: result,
                message: 'Upload ảnh chat thành công'
            });
        } catch (error) {
            next(error);
        }
    }
}

module.exports = new UploadController();
