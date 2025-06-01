const path = require('path');
const fs = require('fs');

// Cấu hình đường dẫn upload
const baseUploadDir = path.join(__dirname, '../../uploads');
const profileUploadDir = path.join(baseUploadDir, 'profiles');
const chatUploadDir = path.join(baseUploadDir, 'chats');

// Cấu hình giới hạn file
const uploadLimits = {
    profile: {
        fileSize: 5 * 1024 * 1024 // 5MB
    },
    chat: {
        fileSize: 10 * 1024 * 1024 // 10MB
    }
};

// Kiểm tra loại file
const fileFilter = (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
        cb(null, true);
    } else {
        cb(new Error('Chỉ cho phép upload file ảnh'), false);
    }
};

// Tạo các thư mục cần thiết
const createUploadDirectories = () => {
    if (!fs.existsSync(baseUploadDir)) {
        fs.mkdirSync(baseUploadDir, { recursive: true });
    }
    if (!fs.existsSync(profileUploadDir)) {
        fs.mkdirSync(profileUploadDir, { recursive: true });
    }
    if (!fs.existsSync(chatUploadDir)) {
        fs.mkdirSync(chatUploadDir, { recursive: true });
    }
};

module.exports = {
    baseUploadDir,
    profileUploadDir,
    chatUploadDir,
    uploadLimits,
    fileFilter,
    createUploadDirectories
};
