const authController = require('../controllers/auth.controller');
const { authenticateToken } = require('../middlewares/auth.middlewares');
const express = require('express');
const router = express.Router();

// Test endpoint để kiểm tra kết nối server
router.get('/test', (req, res) => {
    res.json({
        success: true,
        message: 'Server is running and accessible!',
        timestamp: new Date().toISOString(),
        client_ip: req.ip,
        headers: req.headers
    });
});


router.post('/login', authController.login);
router.post('/logout', authenticateToken, authController.logout);
router.post('/logout-all', authenticateToken, authController.logoutAll);

module.exports = router;