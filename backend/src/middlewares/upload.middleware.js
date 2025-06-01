const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { 
    profileUploadDir, 
    chatUploadDir, 
    uploadLimits, 
    fileFilter 
} = require('../configs/upload.config');

// Cấu hình multer cho profile pictures
const profileStorage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, profileUploadDir);
    },
    filename: function (req, file, cb) {
        const uniqueName = `${req.user.userId}_${Date.now()}${path.extname(file.originalname)}`;
        cb(null, uniqueName);
    }
});

// Cấu hình multer cho chat images
const chatStorage = multer.diskStorage({
    destination: function (req, file, cb) {
        const conversationId = req.body.conversationId || req.params.conversationId;
        if (!conversationId) {
            return cb(new Error('Conversation ID is required for chat images'), null);
        }
        
        const conversationDir = path.join(chatUploadDir, conversationId.toString());
        if (!fs.existsSync(conversationDir)) {
            fs.mkdirSync(conversationDir, { recursive: true });
        }
        cb(null, conversationDir);
    },
    filename: function (req, file, cb) {
        const uniqueName = `${req.user.userId}_${Date.now()}${path.extname(file.originalname)}`;
        cb(null, uniqueName);
    }
});

// Tạo các multer instance
const profileUpload = multer({
    storage: profileStorage,
    fileFilter: fileFilter,
    limits: uploadLimits.profile
});

const chatUpload = multer({
    storage: chatStorage,
    fileFilter: fileFilter,
    limits: uploadLimits.chat
});

module.exports = {
    profileUpload,
    profilePictureUpload: profileUpload.single('profilePicUrl'),
    chatUpload,
    chatImageUpload: chatUpload.single('chatImage')
};
