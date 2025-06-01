const FileUtils = require('../utils/file.utils');

// Middleware để validate và sanitize filename
const sanitizeFilename = (req, res, next) => {
    if (req.file) {
        // Sanitize filename để đảm bảo an toàn
        req.file.filename = FileUtils.sanitizeFileName(req.file.filename);
        
        // Validate filename không empty sau khi sanitize
        if (!req.file.filename) {
            const error = new Error('Tên file không hợp lệ');
            error.statusCode = 400;
            return next(error);
        }
    }
    
    next();
};

module.exports = {
    sanitizeFilename
};
