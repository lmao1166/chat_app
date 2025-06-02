const messageRepository = require('../repositories/message.repository');
const conversationRepository = require('../repositories/conversation.repository');
const socketService = require('./socket.service');

class MessageService {
    // Helper method to create error with status code
    _createError(message, statusCode = 500) {
        const error = new Error(message);
        error.statusCode = statusCode;
        return error;
    }

    // Helper method to handle errors consistently
    _handleError(error, defaultMessage) {
        if (error.statusCode) throw error;
        throw new Error(`${defaultMessage}: ${error.message}`);
    }

    async getAllMessages() {
        try {
            const messages = await messageRepository.findAll();
            return messages.map(message => this.formatMessage(message));
        } catch (error) {
            this._handleError(error, 'Error fetching messages');
        }
    }

    async getMessageById(id) {
        try {
            const message = await messageRepository.findById(id);
            if (!message) throw this._createError('Tin nhắn không tồn tại', 404);
            return this.formatMessage(message);
        } catch (error) {
            this._handleError(error, 'Error fetching message');
        }
    }

    async getMessagesByConversationId(conversationId, options = {}) {
        try {
            const conversation = await conversationRepository.findById(conversationId);
            if (!conversation) throw this._createError('Cuộc trò chuyện không tồn tại', 404);

            const messages = await messageRepository.findByConversationId(conversationId, options);
            if (!messages || messages.length === 0) return [];
            return messages.map(message => this.formatMessage(message));
        } catch (error) {
            this._handleError(error, 'Error fetching messages by conversation');
        }
    }


      async sendMessage(senderId, messageData) {
        try {
            const conversation = await conversationRepository.findById(messageData.conversation_id);
            if (!conversation) throw this._createError('Cuộc trò chuyện không tồn tại', 404);

            const newMessage = await messageRepository.create({
                content: messageData.content.trim(),
                sender_id: senderId,
                conversation_id: messageData.conversation_id,
                timestamp: new Date(),
                attachment_url: messageData.attachment_url || null
            });

            // Update conversation and get formatted message
            await conversationRepository.update(messageData.conversation_id, { last_message_at: new Date() });
            const completeMessage = await messageRepository.findById(newMessage.id);
            const formattedMessage = this.formatMessage(completeMessage);

            // Emit real-time event
            socketService.emitNewMessage(messageData.conversation_id, formattedMessage);
            return formattedMessage;
        } catch (error) {
            this._handleError(error, 'Error sending message');
        }
    }

    async updateMessage(id, messageData, userId) {
        try {
            const message = await messageRepository.findById(id);
            if (!message) throw this._createError('Tin nhắn không tồn tại', 404);
            if (message.sender_id !== userId) throw this._createError('Bạn chỉ có thể chỉnh sửa tin nhắn của mình', 403);

            await messageRepository.update(id, {
                content: messageData.content.trim(),
                type: messageData.type || message.type,
                attachment_url: messageData.attachment_url || message.attachment_url
            });

            const updatedMessage = await messageRepository.findById(id);
            const formattedMessage = this.formatMessage(updatedMessage);

            // Emit real-time event
            socketService.emitMessageUpdate(message.conversation_id, formattedMessage);
            return formattedMessage;
        } catch (error) {
            this._handleError(error, 'Error updating message');
        }
    }

    async deleteMessage(id, userId) {
        try {
            const message = await messageRepository.findById(id);
            if (!message) throw this._createError('Tin nhắn không tồn tại', 404);
            if (message.sender_id !== userId) throw this._createError('Bạn chỉ có thể xóa tin nhắn của mình', 403);

            await messageRepository.markAsDeleted(id, userId);
            socketService.emitMessageDelete(message.conversation_id, id, userId);
            
            return { message: 'Tin nhắn đã được xóa thành công' };
        } catch (error) {
            this._handleError(error, 'Error deleting message');
        }
    }

    formatMessage(message) {
        return {
            id: message.message_id,
            content: message.content,
            timestamp: message.timestamp,
            type: message.type,
            attachment_url: message.attachment_url,
            deleted_by_sender: message.deleted_by_sender,
            sender: message.sender ? {
                id: message.sender.id,
                username: message.sender.username,
                email: message.sender.email,
                profilePicUrl: message.sender.profilePicUrl,
            } : null
        };
    }
}

module.exports = new MessageService();