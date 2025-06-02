const authController = require('../controllers/auth.controller');
const { authenticateToken } = require('../middlewares/auth.middlewares');
const express = require('express');
const router = express.Router();

router.post('/login', authController.login);


module.exports = router;