const notificationRepository = require('../repositories/notification.repository');
const conversationRepository = require('../repositories/conversation.repository');
const userRepository = require('../repositories/user.repository');
const socketService = require('./socket.service');

class NotificationService {
    // Notification types constants
    static TYPES = {
        NEW_MESSAGE: 'new_message',
        MENTION: 'mention',
        CONVERSATION_INVITE: 'conversation_invite',
        SYSTEM: 'system'
    };

    async createNewMessageNotification(messageData, senderId, conversationId) {
        try {
            // Get conversation details
            const conversation = await conversationRepository.findById(conversationId);
            if (!conversation) {
                throw new Error('Conversation not found');
            }

            // Get sender info
            const sender = await userRepository.findById(senderId);
            if (!sender) {
                throw new Error('Sender not found');
            }

            // Get all conversation members except the sender
            const members = conversation.members || [];
            const recipientIds = members
                .filter(member => member.id !== senderId)
                .map(member => member.id);

            // Create notifications for each recipient
            const notifications = [];
            for (const recipientId of recipientIds) {
                // Check if user is online and in the conversation
                const isRecipientOnline = socketService.isUserOnline(recipientId);
                
                // Create notification content
                let content;
                if (conversation.type === 'private') {
                    content = `${sender.username} đã gửi tin nhắn: "${this._truncateMessage(messageData.content)}"`;
                } else {
                    content = `${sender.username} đã gửi tin nhắn trong nhóm ${conversation.name}: "${this._truncateMessage(messageData.content)}"`;
                }

                const notificationData = {
                    user_id: recipientId,
                    type: NotificationService.TYPES.NEW_MESSAGE,
                    content: content,
                    related_entity_id: messageData.id || null,
                    is_read: false
                };

                const notification = await notificationRepository.create(notificationData);
                notifications.push(notification);

                // Send real-time notification if user is online
                if (isRecipientOnline) {
                    socketService.notifyUser(recipientId, 'new_notification', {
                        notification: this.formatNotification(notification),
                        unreadCount: await this.getUnreadCount(recipientId)
                    });
                }

                console.log(`Created message notification for user ${recipientId}: ${content}`);
            }

            return notifications;
        } catch (error) {
            console.error('Error creating new message notification:', error);
            throw new Error('Error creating new message notification: ' + error.message);
        }
    }

    async createMentionNotification(mentionedUserId, messageData, senderId, conversationId) {
        try {
            const sender = await userRepository.findById(senderId);
            const conversation = await conversationRepository.findById(conversationId);
            
            if (!sender || !conversation) {
                throw new Error('Sender or conversation not found');
            }

            const content = `${sender.username} đã nhắc đến bạn: "${this._truncateMessage(messageData.content)}"`;

            const notificationData = {
                user_id: mentionedUserId,
                type: NotificationService.TYPES.MENTION,
                content: content,
                related_entity_id: messageData.id || null,
                is_read: false
            };

            const notification = await notificationRepository.create(notificationData);

            // Send real-time notification
            if (socketService.isUserOnline(mentionedUserId)) {
                socketService.notifyUser(mentionedUserId, 'new_notification', {
                    notification: this.formatNotification(notification),
                    unreadCount: await this.getUnreadCount(mentionedUserId)
                });
            }

            console.log(`Created mention notification for user ${mentionedUserId}`);
            return notification;
        } catch (error) {
            console.error('Error creating mention notification:', error);
            throw new Error('Error creating mention notification: ' + error.message);
        }
    }

    async getNotificationsByUserId(userId, options = {}) {
        try {
            const notifications = await notificationRepository.findByUserId(userId, options);
            return notifications.map(notification => this.formatNotification(notification));
        } catch (error) {
            throw new Error('Error fetching notifications: ' + error.message);
        }
    }

    async getUnreadNotifications(userId) {
        try {
            const notifications = await notificationRepository.findUnreadByUserId(userId);
            return notifications.map(notification => this.formatNotification(notification));
        } catch (error) {
            throw new Error('Error fetching unread notifications: ' + error.message);
        }
    }

    async getUnreadCount(userId) {
        try {
            return await notificationRepository.getUnreadCount(userId);
        } catch (error) {
            throw new Error('Error getting unread count: ' + error.message);
        }
    }

    async markAsRead(notificationId, userId) {
        try {
            const notification = await notificationRepository.findById(notificationId);
            if (!notification) {
                const error = new Error('Notification not found');
                error.statusCode = 404;
                throw error;
            }

            // Check if notification belongs to the user
            if (notification.user_id !== userId) {
                const error = new Error('Unauthorized to mark this notification as read');
                error.statusCode = 403;
                throw error;
            }

            await notificationRepository.markAsRead(notificationId);

            // Send updated unread count to user
            if (socketService.isUserOnline(userId)) {
                socketService.notifyUser(userId, 'notification_read', {
                    notificationId: notificationId,
                    unreadCount: await this.getUnreadCount(userId)
                });
            }

            return { success: true };
        } catch (error) {
            if (error.statusCode) throw error;
            throw new Error('Error marking notification as read: ' + error.message);
        }
    }

    async markAllAsRead(userId) {
        try {
            await notificationRepository.markAllAsReadByUserId(userId);

            // Send updated unread count to user
            if (socketService.isUserOnline(userId)) {
                socketService.notifyUser(userId, 'all_notifications_read', {
                    unreadCount: 0
                });
            }

            return { success: true };
        } catch (error) {
            throw new Error('Error marking all notifications as read: ' + error.message);
        }
    }

    async deleteNotification(notificationId, userId) {
        try {
            const notification = await notificationRepository.findById(notificationId);
            if (!notification) {
                const error = new Error('Notification not found');
                error.statusCode = 404;
                throw error;
            }

            // Check if notification belongs to the user
            if (notification.user_id !== userId) {
                const error = new Error('Unauthorized to delete this notification');
                error.statusCode = 403;
                throw error;
            }

            await notificationRepository.delete(notificationId);
            return { success: true };
        } catch (error) {
            if (error.statusCode) throw error;
            throw new Error('Error deleting notification: ' + error.message);
        }
    }

    async getUserNotifications(userId, options = {}) {
        try {
            const { page = 1, limit = 20, unreadOnly = false } = options;
            
            // Build query options
            const queryOptions = {
                where: { user_id: userId },
                order: [['created_at', 'DESC']],
                limit: parseInt(limit),
                offset: (parseInt(page) - 1) * parseInt(limit)
            };

            // Add unread filter if specified
            if (unreadOnly) {
                queryOptions.where.is_read = false;
            }

            const notifications = await notificationRepository.findByUserId(userId, queryOptions);
            const formattedNotifications = notifications.map(notification => this.formatNotification(notification));

            // Get total count for pagination
            const totalCount = await notificationRepository.getCountByUserId(userId, unreadOnly);
            const totalPages = Math.ceil(totalCount / limit);

            return {
                notifications: formattedNotifications,
                pagination: {
                    currentPage: parseInt(page),
                    totalPages,
                    totalCount,
                    hasNextPage: parseInt(page) < totalPages,
                    hasPrevPage: parseInt(page) > 1
                }
            };
        } catch (error) {
            throw new Error('Error fetching user notifications: ' + error.message);
        }
    }

    // Helper methods
    formatNotification(notification) {
        return {
            id: notification.id,
            type: notification.type,
            content: notification.content,
            isRead: notification.is_read,
            relatedEntityId: notification.related_entity_id,
            createdAt: notification.created_at,
            updatedAt: notification.updated_at
        };
    }

    _truncateMessage(message, maxLength = 50) {
        if (!message) return '';
        return message.length > maxLength ? 
            message.substring(0, maxLength) + '...' : 
            message;
    }

    _createError(message, statusCode) {
        const error = new Error(message);
        error.statusCode = statusCode;
        return error;
    }

    // Check if message contains mentions
    _extractMentions(messageContent) {
        const mentionRegex = /@(\w+)/g;
        const mentions = [];
        let match;
        
        while ((match = mentionRegex.exec(messageContent)) !== null) {
            mentions.push(match[1]); // username without @
        }
        
        return mentions;
    }

    async processMentions(messageData, senderId, conversationId) {
        try {
            const mentions = this._extractMentions(messageData.content);
            if (mentions.length === 0) return [];

            const conversation = await conversationRepository.findById(conversationId);
            if (!conversation) return [];

            const notifications = [];
            
            // Find mentioned users in the conversation
            for (const username of mentions) {
                const mentionedUser = conversation.members?.find(
                    member => member.username.toLowerCase() === username.toLowerCase()
                );
                
                if (mentionedUser && mentionedUser.id !== senderId) {
                    const notification = await this.createMentionNotification(
                        mentionedUser.id, 
                        messageData, 
                        senderId, 
                        conversationId
                    );
                    notifications.push(notification);
                }
            }

            return notifications;
        } catch (error) {
            console.error('Error processing mentions:', error);
            return [];
        }
    }
}

module.exports = new NotificationService();
