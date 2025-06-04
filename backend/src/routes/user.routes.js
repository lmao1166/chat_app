// src/routes/user.routes.js
const express = require('express');
const router = express.Router(); // Lấy đối tượng Router từ Express
const userController = require('../controllers/user.controller'); // Import controller
const { validate } = require('../middlewares/validators/validation.midleware'); // Import middleware để kiểm tra dữ liệu đầu vào
const { registerValidator } = require('../middlewares/validators/auth.validator'); // Import các validator
const { updateUserWithProfileValidator } = require('../middlewares/validators/user.validator');
const { authenticateToken } = require('../middlewares/auth.middlewares');
const { profilePictureUpload } = require('../middlewares/upload.middleware');
const { sanitizeFilename } = require('../middlewares/filename.middleware');


router.get('/', authenticateToken, userController.getAllUsers);

router.get('/online', authenticateToken, userController.getOnlineUsers);

router.get('/:id', authenticateToken, userController.getUserById);

router.post('/', registerValidator, validate, userController.register);

router.put(
    '/', 
    authenticateToken,
    profilePictureUpload,
    sanitizeFilename,
    updateUserWithProfileValidator,
    validate,
    userController.updateUser
);

router.put('/password', authenticateToken, userController.changePassword);

router.delete('/:id', authenticateToken, userController.deleteUser);

module.exports = router;