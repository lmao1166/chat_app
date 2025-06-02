const {body} = require('express-validator');

const registerValidator = [
    body('username')
        .notEmpty().withMessage('Tên người dùng không được để trống')
        .isLength({min: 3}).withMessage('Tên người dùng phải có ít nhất 3 ký tự')
        .exists().withMessage('Tên người dùng là bắt buộc'),
    body('email')
        .isEmail().withMessage('Email không hợp lệ')
        .notEmpty().withMessage('Email không được để trống')
        .exists().withMessage('Email là bắt buộc'),
    body('password')
        .isLength({min: 3})
        .withMessage('Mật khẩu phải có ít nhất 3 ký tự'),
    body('confirmPassword')
        .custom((value, {req}) => {
            if (value !== req.body.password) {
                throw new Error('Mật khẩu xác nhận không khớp');
            }
            return true;
        })
];


const loginValidator = [
    body('email')
        .isEmail()
        .withMessage('Email không hợp lệ'),
    body('password')
        .isLength({min: 3})
        .withMessage('Mật khẩu phải có ít nhất 3 ký tự')
];

module.exports = {
    registerValidator,
    loginValidator
};