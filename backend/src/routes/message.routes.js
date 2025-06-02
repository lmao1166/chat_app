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

// GET /api/v1/messages - Get all messages
router.get('/', messageController.getAllMessages);

// GET /api/v1/messages/:id - Get message by ID
router.get('/:id', messageIdValidator, validate, messageController.getMessageById);

// GET /api/v1/messages/conversation/:conversationId - Get messages by conversation ID
router.get('/conversation/:conversationId', conversationIdValidator, validate, messageController.getMessagesByConversationId);

// POST /api/v1/messages - Send a new message
router.post('/', sendMessageValidator, validate, messageController.sendMessage);

// PUT /api/v1/messages/:id - Update a message
router.put('/:id', updateMessageValidator, validate, messageController.updateMessage);

// DELETE /api/v1/messages/:id - Delete a message
router.delete('/:id', messageIdValidator, validate, messageController.deleteMessage);

router.get('/test/ok', (req, res) => {
    res.status(200).json({
        status: 200,
        success: true,
        message: 'Test endpoint is working'
    });
});

module.exports = router;