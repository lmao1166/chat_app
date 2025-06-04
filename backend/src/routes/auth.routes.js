const authController = require('../controllers/auth.controller');
const { authenticateToken } = require('../middlewares/auth.middlewares');
const { validate } = require('../middlewares/validators/validation.midleware');
const express = require('express');
const router = express.Router();


router.get('/', authenticateToken, authController.getCurrentUser);


router.post('/login', validate, authController.login);
router.post('/logout', authenticateToken, authController.logout);
router.post('/logout-all', authenticateToken, authController.logoutAll);

module.exports = router;