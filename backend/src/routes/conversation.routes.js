const conversationController = require('../controllers/conversation.controller');
const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middlewares/auth.middlewares');

// router.get('/', ConservationController.getAllConversations); // Lấy tất cả cuộc trò chuyện

router.get('/:id', conversationController.getConversationById); // Lấy cuộc trò chuyện theo ID

router.get('/user/:id', conversationController.getConservationsByUserId); 

router.post('/', authenticateToken, conversationController.findOrCreateConversation); // Tạo hoặc tìm cuộc trò chuyện

module.exports = router;