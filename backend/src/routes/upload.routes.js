const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middlewares/auth.middlewares');
const { validate } = require('../middlewares/validators/validation.midleware');
const { uploadProfileValidator, uploadChatImageValidator } = require('../middlewares/validators/upload.validator');
const { profilePictureUpload, chatImageUpload } = require('../middlewares/upload.middleware');
const { sanitizeFilename } = require('../middlewares/filename.middleware');
const uploadController = require('../controllers/upload.controller');
const fileServeController = require('../controllers/fileServe.controller');
const { createUploadDirectories } = require('../configs/upload.config');

// Khởi tạo thư mục upload
createUploadDirectories();

// Upload routes
router.post('/profile-picture', 
    authenticateToken,
    uploadProfileValidator,
    validate, 
    profilePictureUpload, 
    uploadController.uploadProfilePicture
);

router.post('/chat-image', 
    authenticateToken,
    uploadChatImageValidator,
    validate,
    chatImageUpload, 
    uploadController.uploadChatImage
);

// File serving routes
router.get('/profiles/:filename', fileServeController.serveProfileImage);
router.get('/chats/:conversationId/:filename', fileServeController.serveChatImage);

module.exports = router;
