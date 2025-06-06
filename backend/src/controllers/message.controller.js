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
        const userId = req.user.userId; // Lấy userId từ token
        
        const messages = await messageService.getMessagesByConversationId(conversationId, userId);
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
        const messageData = {
            ...req.body,
            // Đảm bảo attachment_url không được set cho tin nhắn văn bản thông thường
            attachment_url: null
        };
        const senderId = req.user.userId; 
        const newMessage = await messageService.sendMessage(senderId, messageData);
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

const sendMessageWithImage = async (req, res, next) => {
    try {
        const { conversationId, content } = req.body;
        const senderId = req.user.userId;
        const file = req.file;

        // Validate required fields
        if (!conversationId) {
            return res.status(400).json({
                status: 400,
                success: false,
                message: 'Conversation ID là bắt buộc'
            });
        }

        if (!file && !content) {
            return res.status(400).json({
                status: 400,
                success: false,
                message: 'Phải có ít nhất hình ảnh hoặc nội dung tin nhắn'
            });
        }

        let attachmentUrl = null;
        if (file) {
            // Tạo URL cho hình ảnh
            const domain = process.env.DOMAIN || 'localhost:3000';
            attachmentUrl = `http://${domain}/api/v1/uploads/chats/${conversationId}/${file.filename}`;
        }        // Tạo message data
        const messageData = {
            content: content || '', // Không tự động thêm text cho image-only messages
            conversation_id: parseInt(conversationId),
            type: file ? 'image' : 'text',
            attachment_url: attachmentUrl
        };

        const newMessage = await messageService.sendMessage(senderId, messageData);

        res.status(201).json({
            status: 201,
            success: true,
            data: newMessage,
            message: 'Gửi tin nhắn thành công'
        });
    } catch (error) {
        // Xóa file nếu có lỗi
        if (req.file) {
            const fs = require('fs');
            fs.unlink(req.file.path, (err) => {
                if (err) console.error('Lỗi khi xóa file:', err);
            });
        }
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
    sendMessageWithImage,
    updateMessage,
    deleteMessage
};