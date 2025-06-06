const Notification = require('../models/notification.model');
const { Op } = require('sequelize');

class NotificationRepository {
    async findAll() {
        try {
            const notifications = await Notification.findAll({
                order: [['created_at', 'DESC']]
            });
            return notifications;
        } catch (error) {
            throw new Error('Error fetching notifications: ' + error.message);
        }
    }

    async findById(id) {
        try {
            const notification = await Notification.findByPk(id);
            return notification;
        } catch (error) {
            throw new Error('Error fetching notification: ' + error.message);
        }
    }

    async findByUserId(userId, options = {}) {
        try {
            const queryOptions = {
                where: { user_id: userId },
                order: [['created_at', 'DESC']],
                ...options
            };

            const notifications = await Notification.findAll(queryOptions);
            return notifications;
        } catch (error) {
            throw new Error('Error fetching notifications by user ID: ' + error.message);
        }
    }

    async findUnreadByUserId(userId) {
        try {
            const notifications = await Notification.findAll({
                where: {
                    user_id: userId,
                    is_read: false
                },
                order: [['created_at', 'DESC']]
            });
            return notifications;
        } catch (error) {
            throw new Error('Error fetching unread notifications: ' + error.message);
        }
    }

    async getUnreadCount(userId) {
        try {
            const count = await Notification.count({
                where: {
                    user_id: userId,
                    is_read: false
                }
            });
            return count;
        } catch (error) {
            throw new Error('Error getting unread notification count: ' + error.message);
        }
    }

    async getCountByUserId(userId, unreadOnly = false) {
        try {
            const whereClause = { user_id: userId };
            
            if (unreadOnly) {
                whereClause.is_read = false;
            }

            const count = await Notification.count({
                where: whereClause
            });
            return count;
        } catch (error) {
            throw new Error('Error getting notification count by user ID: ' + error.message);
        }
    }

    async create(notificationData) {
        const transaction = await Notification.sequelize.transaction();
        try {
            const newNotification = await Notification.create(notificationData, { transaction });
            await transaction.commit();
            return newNotification;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error creating notification: ' + error.message);
        }
    }

    async update(id, notificationData) {
        const transaction = await Notification.sequelize.transaction();
        try {
            const [updatedRows] = await Notification.update(notificationData, {
                where: { id: id },
                transaction: transaction
            });
            await transaction.commit();
            return updatedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error updating notification: ' + error.message);
        }
    }

    async markAsRead(id) {
        const transaction = await Notification.sequelize.transaction();
        try {
            const [updatedRows] = await Notification.update(
                { is_read: true },
                {
                    where: { id: id },
                    transaction: transaction
                }
            );
            await transaction.commit();
            return updatedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error marking notification as read: ' + error.message);
        }
    }

    async markAllAsReadByUserId(userId) {
        const transaction = await Notification.sequelize.transaction();
        try {
            const [updatedRows] = await Notification.update(
                { is_read: true },
                {
                    where: {
                        user_id: userId,
                        is_read: false
                    },
                    transaction: transaction
                }
            );
            await transaction.commit();
            return updatedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error marking all notifications as read: ' + error.message);
        }
    }

    async delete(id) {
        const transaction = await Notification.sequelize.transaction();
        try {
            const deletedRow = await Notification.destroy({
                where: { id: id },
                transaction: transaction
            });
            await transaction.commit();
            return deletedRow;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error deleting notification: ' + error.message);
        }
    }

    async deleteByUserId(userId) {
        const transaction = await Notification.sequelize.transaction();
        try {
            const deletedRows = await Notification.destroy({
                where: { user_id: userId },
                transaction: transaction
            });
            await transaction.commit();
            return deletedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error deleting notifications by user ID: ' + error.message);
        }
    }

    async deleteOldNotifications(daysOld = 30) {
        const transaction = await Notification.sequelize.transaction();
        try {
            const cutoffDate = new Date();
            cutoffDate.setDate(cutoffDate.getDate() - daysOld);

            const deletedRows = await Notification.destroy({
                where: {
                    created_at: {
                        [Op.lt]: cutoffDate
                    }
                },
                transaction: transaction
            });
            await transaction.commit();
            return deletedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error deleting old notifications: ' + error.message);
        }
    }

    async findByTypeAndUserId(type, userId, options = {}) {
        try {
            const queryOptions = {
                where: {
                    user_id: userId,
                    type: type
                },
                order: [['created_at', 'DESC']],
                ...options
            };

            const notifications = await Notification.findAll(queryOptions);
            return notifications;
        } catch (error) {
            throw new Error('Error fetching notifications by type and user ID: ' + error.message);
        }
    }

    async findByRelatedEntityId(relatedEntityId, options = {}) {
        try {
            const queryOptions = {
                where: { related_entity_id: relatedEntityId },
                order: [['created_at', 'DESC']],
                ...options
            };

            const notifications = await Notification.findAll(queryOptions);
            return notifications;
        } catch (error) {
            throw new Error('Error fetching notifications by related entity ID: ' + error.message);
        }
    }

    async markNotificationsByEntityAsRead(userId, relatedEntityId) {
        const transaction = await Notification.sequelize.transaction();
        try {
            const [updatedRows] = await Notification.update(
                { is_read: true },
                {
                    where: {
                        user_id: userId,
                        related_entity_id: relatedEntityId,
                        is_read: false
                    },
                    transaction: transaction
                }
            );
            await transaction.commit();
            return updatedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error marking notifications by entity as read: ' + error.message);
        }
    }

    async getNotificationStats(userId) {
        try {
            const stats = await Notification.findAll({
                attributes: [
                    'type',
                    [Notification.sequelize.fn('COUNT', Notification.sequelize.col('id')), 'count'],
                    [Notification.sequelize.fn('SUM', Notification.sequelize.literal('CASE WHEN is_read = false THEN 1 ELSE 0 END')), 'unread_count']
                ],
                where: { user_id: userId },
                group: ['type'],
                raw: true
            });
            return stats;
        } catch (error) {
            throw new Error('Error getting notification stats: ' + error.message);
        }
    }
}

module.exports = new NotificationRepository();
