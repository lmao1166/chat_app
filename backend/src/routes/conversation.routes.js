const conversationController = require('../controllers/conversation.controller');
const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middlewares/auth.middlewares');

// router.get('/', ConservationController.getAllConversations); // Lấy tất cả cuộc trò chuyện

// router.get('/:id', authenticateToken, conversationController.getConversationById); // Lấy cuộc trò chuyện theo ID

router.get('/user', authenticateToken, conversationController.getConversations); 

router.post('/', authenticateToken, conversationController.findOrCreateConversation); // Tạo hoặc tìm cuộc trò chuyện

module.exports = router;