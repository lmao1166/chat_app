const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notification.controller');
const { authenticateToken } = require('../middlewares/auth.middlewares');

// Apply authentication middleware to all notification routes
router.use(authenticateToken);

// GET /api/notifications - Get user notifications
router.get('/', notificationController.getUserNotifications);

// GET /api/notifications/unread-count - Get unread notifications count
router.get('/unread-count', notificationController.getUnreadCount);

// PUT /api/notifications/:id/read - Mark notification as read
router.put('/:id/read', notificationController.markAsRead);

// PUT /api/notifications/mark-all-read - Mark all notifications as read
router.put('/mark-all-read', notificationController.markAllAsRead);

// DELETE /api/notifications/:id - Delete notification
router.delete('/:id', notificationController.deleteNotification);

module.exports = router;