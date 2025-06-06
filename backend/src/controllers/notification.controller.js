const notificationService = require('../services/notification.service');

const getUserNotifications = async (req, res, next) => {
    try {
        const userId = req.user.userId;
        const { page = 1, limit = 20, unreadOnly = false } = req.query;
        
        const notifications = await notificationService.getUserNotifications(
            userId, 
            { page: parseInt(page), limit: parseInt(limit), unreadOnly: unreadOnly === 'true' }
        );
        
        res.status(200).json({
            status: 200,
            success: true,
            data: notifications,
            message: 'Lấy danh sách thông báo thành công'
        });
    } catch (error) {
        next(error);
    }
};

const markAsRead = async (req, res, next) => {
    try {
        const notificationId = req.params.id;
        const userId = req.user.userId;
        
        const notification = await notificationService.markAsRead(notificationId, userId);
        res.status(200).json({
            status: 200,
            success: true,
            data: notification,
            message: 'Đánh dấu đã đọc thành công'
        });
    } catch (error) {
        next(error);
    }
};

const markAllAsRead = async (req, res, next) => {
    try {
        const userId = req.user.userId;
        
        const result = await notificationService.markAllAsRead(userId);
        res.status(200).json({
            status: 200,
            success: true,
            data: result,
            message: 'Đánh dấu tất cả đã đọc thành công'
        });
    } catch (error) {
        next(error);
    }
};

const getUnreadCount = async (req, res, next) => {
    try {
        const userId = req.user.userId;
        
        const count = await notificationService.getUnreadCount(userId);
        res.status(200).json({
            status: 200,
            success: true,
            data: { count },
            message: 'Lấy số thông báo chưa đọc thành công'
        });
    } catch (error) {
        next(error);
    }
};

const deleteNotification = async (req, res, next) => {
    try {
        const notificationId = req.params.id;
        const userId = req.user.userId;
        
        await notificationService.deleteNotification(notificationId, userId);
        res.status(200).json({
            status: 200,
            success: true,
            message: 'Xóa thông báo thành công'
        });
    } catch (error) {
        next(error);
    }
};

module.exports = {
    getUserNotifications,
    markAsRead,
    markAllAsRead,
    getUnreadCount,
    deleteNotification
};