const messageRepository = require('../repositories/message.repository');
const conversationRepository = require('../repositories/conversation.repository');
const memberRepository = require('../repositories/member.repository');
const userRepository = require('../repositories/user.repository');
const notificationService = require('./notification.service');
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
    }    async getMessagesByConversationId(conversationId, userId, options = {}) {
        try {
            const conversation = await conversationRepository.findById(conversationId);
            if (!conversation) throw this._createError('Cuộc trò chuyện không tồn tại', 404);

            // Kiểm tra xem user có phải là thành viên của cuộc trò chuyện không
            const isMember = await memberRepository.isUserInConversation(userId, conversationId);
            if (!isMember) throw this._createError('Bạn không có quyền truy cập cuộc trò chuyện này', 403);

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

            // 1. Lưu tin nhắn vào database TRƯỚC (primary source of truth)
            const newMessage = await messageRepository.create({
                content: messageData.content.trim(),
                sender_id: senderId,
                conversation_id: messageData.conversation_id,
                timestamp: new Date(),
                attachment_url: messageData.attachment_url || null,
                message_type: messageData.type || 'text',
                delivery_status: 'sent' // Đã lưu thành công vào DB
            });

            // 2. Update conversation
            await conversationRepository.update(messageData.conversation_id, { 
                last_message_at: new Date() 
            });

            // 3. Lấy tin nhắn đầy đủ với thông tin sender
            const completeMessage = await messageRepository.findById(newMessage.id);
            const formattedMessage = this.formatMessage(completeMessage);            // 4. Create notifications for conversation members (exclude sender)
            try {
                const members = await memberRepository.getConversationMembers(messageData.conversation_id);
                const senderInfo = await userRepository.findById(senderId);
                
                for (const member of members) {
                    if (member.user_id !== senderId) {  // Don't notify sender
                        await notificationService.createNotification({
                            userId: member.user_id,
                            type: 'new_message',
                            content: `${senderInfo.username}: ${messageData.content.substring(0, 50)}${messageData.content.length > 50 ? '...' : ''}`,
                            relatedEntityId: newMessage.id
                        });
                    }
                }
            } catch (notificationError) {
                console.warn('Notification creation failed:', notificationError.message);
            }

            // 5. Emit real-time event (secondary, not critical)
            try {
                socketService.emitNewMessage(messageData.conversation_id, formattedMessage);
                
                // Cập nhật delivery status nếu emit thành công
                await messageRepository.update(newMessage.id, {
                    delivery_status: 'delivered'
                });
                
                console.log(`Message ${newMessage.id} delivered via Socket.IO`);
            } catch (socketError) {
                console.warn('Socket emit failed, but message saved:', socketError.message);
                // Message vẫn được lưu, chỉ real-time bị lỗi
            }

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
    }    formatMessage(message) {
        const domain = process.env.DOMAIN || 'localhost:3000';
        return {
            id: message.id,
            content: message.content,
            timestamp: message.timestamp,
            type: message.type || message.message_type,
            attachment_url: message.attachment_url && message.attachment_url !== 'null' ? message.attachment_url : null,
            delivery_status: message.delivery_status || 'sent',
            deleted_by_sender: message.deleted_by_sender,
            sender: message.sender ? {
                id: message.sender.id,
                username: message.sender.username,
                profilePicUrl: message.sender.profilePicUrl ? 
                    `http://${domain}/api/v1/uploads/profiles/${message.sender.profilePicUrl}` : 
                    null,
            } : null
        };
    }
}

module.exports = new MessageService();