const { body } = require('express-validator');

const updateUserWithProfileValidator = [
    body('username')
        .optional()
        .notEmpty()
        .withMessage('Tên người dùng không được để trống')
        .isLength({ max: 100 })
        .withMessage('Tên đầy đủ không được quá 100 ký tự'),
    
    body('email')
        .optional()
        .isEmail()
        .withMessage('Email không hợp lệ')
        .normalizeEmail(),
];

module.exports = {
    updateUserWithProfileValidator
};
