const { body } = require('express-validator');

const uploadProfileValidator = [
    // Không cần validation cho file vì multer đã xử lý
    // Có thể thêm validation khác nếu cần
];

const uploadChatImageValidator = [
    body('conversationId')
        .isInt({ min: 1 }).withMessage('Conversation ID phải là số nguyên dương')
        .notEmpty().withMessage('Conversation ID không được để trống')
        .exists().withMessage('Conversation ID là bắt buộc')
];

module.exports = {
    uploadProfileValidator,
    uploadChatImageValidator
};
