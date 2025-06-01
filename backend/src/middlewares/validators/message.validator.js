const { body, param } = require('express-validator');

const sendMessageValidator = [
    body('content')
        .notEmpty().withMessage('Nội dung tin nhắn không được để trống')
        .isLength({ min: 1, max: 5000 }).withMessage('Nội dung tin nhắn phải từ 1 đến 5000 ký tự')
        .exists().withMessage('Nội dung tin nhắn là bắt buộc'),
    body('conversation_id')
        .isInt({ min: 1 }).withMessage('ID cuộc trò chuyện phải là số nguyên dương')
        .notEmpty().withMessage('ID cuộc trò chuyện không được để trống')
        .exists().withMessage('ID cuộc trò chuyện là bắt buộc'),
    body('type')
        .optional()
        .isIn(['text', 'image', 'video', 'file', 'system'])
        .withMessage('Loại tin nhắn phải là: text, image, video, file, hoặc system'),
    body('attachment_url')
        .optional()
];

const updateMessageValidator = [
    param('id')
        .isInt({ min: 1 }).withMessage('ID tin nhắn phải là số nguyên dương'),
    body('content')
        .notEmpty().withMessage('Nội dung tin nhắn không được để trống')
        .isLength({ min: 1, max: 5000 }).withMessage('Nội dung tin nhắn phải từ 1 đến 5000 ký tự')
        .exists().withMessage('Nội dung tin nhắn là bắt buộc'),
    body('type')
        .optional()
        .isIn(['text', 'image', 'video', 'file', 'system'])
        .withMessage('Loại tin nhắn phải là: text, image, video, file, hoặc system'),
    body('attachment_url')
        .optional()
        .isURL().withMessage('URL đính kèm không hợp lệ')
];

const messageIdValidator = [
    param('id')
        .isInt({ min: 1 }).withMessage('ID tin nhắn phải là số nguyên dương')
];

const conversationIdValidator = [
    param('conversationId')
        .isInt({ min: 1 }).withMessage('ID cuộc trò chuyện phải là số nguyên dương')
];

module.exports = {
    sendMessageValidator,
    updateMessageValidator,
    messageIdValidator,
    conversationIdValidator
};