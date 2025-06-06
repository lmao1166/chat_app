const express = require('express');
const router = express.Router();
const messageController = require('../controllers/message.controller');
const { validate } = require('../middlewares/validators/validation.midleware');
const { 
    sendMessageValidator, 
    updateMessageValidator, 
    messageIdValidator,
    conversationIdValidator 
} = require('../middlewares/validators/message.validator');
const { authenticateToken } = require('../middlewares/auth.middlewares');
const { chatImageUpload } = require('../middlewares/upload.middleware');

// router.get('/', authenticateToken, messageController.getAllMessages);

router.get('/:id', authenticateToken, messageIdValidator, validate, messageController.getMessageById);

router.get('/conversation/:conversationId', authenticateToken, conversationIdValidator, validate, messageController.getMessagesByConversationId);

router.post('/', authenticateToken, sendMessageValidator, validate, messageController.sendMessage);

router.post('/with-image', 
    authenticateToken,
    chatImageUpload,
    messageController.sendMessageWithImage
);

router.put('/:id', authenticateToken, updateMessageValidator, validate, messageController.updateMessage);

router.delete('/:id', authenticateToken, messageIdValidator, validate, messageController.deleteMessage);


module.exports = router;