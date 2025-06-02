const messageService = require('../services/message.service');

const getAllMessages = async (req, res, next) => {
    try {
        const messages = await messageService.getAllMessages();
        res.status(200).json({
            status: 200,
            success: true,
            data: messages,
            message: 'Lấy danh sách tin nhắn thành công'
        });
    } catch (error) {
        next(error);
    }
};

const getMessageById = async (req, res, next) => {
    try {
        const message = await messageService.getMessageById(req.params.id);
        res.status(200).json({
            status: 200,
            success: true,
            data: message,
            message: 'Lấy thông tin tin nhắn thành công'
        });
    } catch (error) {
        next(error);
    }
};

const getMessagesByConversationId = async (req, res, next) => {
    try {
        const conversationId = req.params.conversationId;
        const options = {};
        
        // Add pagination if provided
        if (req.query.limit) {
            options.limit = parseInt(req.query.limit);
        }
        if (req.query.offset) {
            options.offset = parseInt(req.query.offset);
        }

        const messages = await messageService.getMessagesByConversationId(conversationId, options);
        res.status(200).json({
            status: 200,
            success: true,
            data: messages,
            message: 'Lấy tin nhắn theo cuộc trò chuyện thành công'
        });
    } catch (error) {
        next(error);
    }
};

const sendMessage = async (req, res, next) => {
    try {
        const newMessage = await messageService.sendMessage(req.body);
        res.status(201).json({
            status: 201,
            success: true,
            data: newMessage,
            message: 'Gửi tin nhắn thành công'
        });
    } catch (error) {
        next(error);
    }
};

const updateMessage = async (req, res, next) => {
    try {
        const messageId = req.params.id;
        const userId = req.body.user_id; // This should come from authentication middleware in a real app
        
        const updatedMessage = await messageService.updateMessage(messageId, req.body, userId);
        res.status(200).json({
            status: 200,
            success: true,
            data: updatedMessage,
            message: 'Cập nhật tin nhắn thành công'
        });
    } catch (error) {
        next(error);
    }
};

const deleteMessage = async (req, res, next) => {
    try {
        const messageId = req.params.id;
        const userId = req.body.user_id; // This should come from authentication middleware in a real app
        
        const result = await messageService.deleteMessage(messageId, userId);
        res.status(200).json({
            status: 200,
            success: true,
            data: result,
            message: 'Xóa tin nhắn thành công'
        });
    } catch (error) {
        next(error);
    }
};

module.exports = {
    getAllMessages,
    getMessageById,
    getMessagesByConversationId,
    sendMessage,
    updateMessage,
    deleteMessage
};