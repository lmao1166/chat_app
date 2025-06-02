const { validationResult } = require('express-validator');

const validate = (req, res, next) => {
    const errors = validationResult(req);
    if (errors.isEmpty()) {
        return next();
    }
    const validationMessages = errors.array().map(error => error.msg);
    const error = new Error('Lỗi dữ liệu đầu vào');
    error.statusCode = 400;
    error.validationMessages = validationMessages;
    next(error);
}

module.exports = {
    validate
};