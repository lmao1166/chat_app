const messageRepository = require('../repositories/message.repository');
const conversationRepository = require('../repositories/conversation.repository');
const userRepository = require('../repositories/user.repository');

class MessageService {
    async getAllMessages() {
        try {
            const messages = await messageRepository.findAll();
            return messages.map(message => this.formatMessage(message));
        } catch (error) {
            throw new Error('Error fetching messages: ' + error.message);
        }
    }

    async getMessageById(id) {
        try {
            const message = await messageRepository.findById(id);
            if (!message) {
                const error = new Error('Tin nhắn không tồn tại');
                error.statusCode = 404;
                throw error;
            }
            return this.formatMessage(message);
        } catch (error) {
            if (error.statusCode) throw error;
            throw new Error('Error fetching message: ' + error.message);
        }
    }

    async getMessagesByConversationId(conversationId, options = {}) {
        try {
            // Check if conversation exists
            const conversation = await conversationRepository.findById(conversationId);
            if (!conversation) {
                const error = new Error('Cuộc trò chuyện không tồn tại');
                error.statusCode = 404;
                throw error;
            }

            const messages = await messageRepository.findByConversationId(conversationId, options);
            return messages.map(message => this.formatMessage(message));
        } catch (error) {
            if (error.statusCode) throw error;
            throw new Error('Error fetching messages by conversation: ' + error.message);
        }
    }

    async sendMessage(senderId, messageData) {
        try {
            // Check if conversation exists
            const conversation = await conversationRepository.findById(messageData.conversation_id);
            if (!conversation) {
                const error = new Error('Cuộc trò chuyện không tồn tại');
                error.statusCode = 404;
                throw error;
            }

            // Prepare message data
            const newMessageData = {
                content: messageData.content.trim(),
                sender_id: senderId,
                conversation_id: messageData.conversation_id,
                timestamp: new Date(),
                attachment_url: messageData.attachment_url || null
            };

            // Create the message
            const newMessage = await messageRepository.create(newMessageData);

            // Update conversation's last_message_at
            await conversationRepository.update(messageData.conversation_id, {
                last_message_at: new Date()
            });

            return this.formatMessage(newMessage);
        } catch (error) {
            if (error.statusCode) throw error;
            throw new Error('Error sending message: ' + error.message);
        }
    }

    async updateMessage(id, messageData, userId) {
        try {
            const message = await messageRepository.findById(id);
            if (!message) {
                const error = new Error('Tin nhắn không tồn tại');
                error.statusCode = 404;
                throw error;
            }

            // Check if user is the sender
            if (message.sender_id !== userId) {
                const error = new Error('Bạn chỉ có thể chỉnh sửa tin nhắn của mình');
                error.statusCode = 403;
                throw error;
            }


            const updateData = {
                content: messageData.content.trim(),
                type: messageData.type || message.type,
                attachment_url: messageData.attachment_url || message.attachment_url
            };

            await messageRepository.update(id, updateData);
            const updatedMessage = await messageRepository.findById(id);
            return this.formatMessage(updatedMessage);
        } catch (error) {
            if (error.statusCode) throw error;
            throw new Error('Error updating message: ' + error.message);
        }
    }

    async deleteMessage(id, userId) {
        try {
            const message = await messageRepository.findById(id);
            if (!message) {
                const error = new Error('Tin nhắn không tồn tại');
                error.statusCode = 404;
                throw error;
            }

            // Check if user is the sender
            if (message.sender_id !== userId) {
                const error = new Error('Bạn chỉ có thể xóa tin nhắn của mình');
                error.statusCode = 403;
                throw error;
            }

            await messageRepository.markAsDeleted(id, userId);
            return { message: 'Tin nhắn đã được xóa thành công' };
        } catch (error) {
            if (error.statusCode) throw error;
            throw new Error('Error deleting message: ' + error.message);
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