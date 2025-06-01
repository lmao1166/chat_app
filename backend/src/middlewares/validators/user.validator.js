const { body } = require('express-validator');

const updateUserWithProfileValidator = [
    body('fullName')
        .optional()
        .notEmpty()
        .withMessage('Tên đầy đủ không được để trống')
        .isLength({ max: 100 })
        .withMessage('Tên đầy đủ không được quá 100 ký tự'),
    
    body('email')
        .optional()
        .isEmail()
        .withMessage('Email không hợp lệ')
        .normalizeEmail(),
    
    body('phoneNumber')
        .optional()
        .matches(/^[0-9+\-\s()]+$/)
        .withMessage('Số điện thoại không hợp lệ')
        .isLength({ min: 10, max: 15 })
        .withMessage('Số điện thoại phải từ 10-15 ký tự'),
    
    body('password')
        .notEmpty().withMessage('Mật khẩu không được để trống')
        .isLength({ min: 6 })
        .withMessage('Mật khẩu phải có ít nhất 6 ký tự'),
    
    body('confirmPassword')
        .optional()
        .custom((value, { req }) => {
            if (req.body.password && value !== req.body.password) {
                throw new Error('Mật khẩu xác nhận không khớp');
            }
            return true;
        })
];

module.exports = {
    updateUserWithProfileValidator
};
