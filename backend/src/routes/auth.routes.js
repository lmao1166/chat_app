const authController = require('../controllers/auth.controller');
const { authenticateToken } = require('../middlewares/auth.middlewares');
const express = require('express');
const router = express.Router();

router.post('/login', authController.login);
router.post('/logout', authenticateToken, authController.logout);
router.post('/logout-all', authenticateToken, authController.logoutAll);

module.exports = router;