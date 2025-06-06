const jwtService = require('./jwt.service');
const userRepository = require('../repositories/user.repository');

class SocketService {
    constructor() {
        this.io = null;
        this.onlineUsers = new Map();
        this.userSockets = new Map();
    }

    initialize(io) {
        this.io = io;
        this.io.use(this._authenticateSocket.bind(this));
        this.io.on('connection', this._handleConnection.bind(this));
    }

    async _authenticateSocket(socket, next) {
        try {
            const token = socket.handshake.auth.token || 
                         socket.handshake.headers.authorization?.replace('Bearer ', '');
            
            if (!token) return next(new Error('No token provided'));

            const decoded = jwtService.verifyToken(token);
            if (!decoded || await jwtService.isTokenInvalidated(token)) {
                return next(new Error('Invalid or expired token'));
            }

            const user = await userRepository.findById(decoded.userId);
            if (!user) return next(new Error('User not found'));

            socket.userId = decoded.userId;
            socket.userInfo = { id: user.id, username: user.username, email: user.email, profilePicUrl: user.profilePicUrl };
            next();
        } catch (error) {
            console.error('Socket auth error:', error);
            next(new Error('Authentication failed'));
        }
    }

    _handleConnection(socket) {
        const { userId, userInfo } = socket;
        console.log(`User ${userInfo.username} connected`);
        
        // Store connection
        this.onlineUsers.set(userId, socket.id);
        this.userSockets.set(socket.id, userId);
        socket.join(`user_${userId}`);

        // Notify user online status
        socket.broadcast.emit('user_online', { userId, userInfo });        // Event handlers
        const events = {
            join_conversation: (id) => {
                socket.join(`conversation_${id}`);
                console.log(`User ${userInfo.username} joined conversation ${id}`);
            },
            leave_conversation: (id) => {
                socket.leave(`conversation_${id}`);
                console.log(`User ${userInfo.username} left conversation ${id}`);
            },
            typing_start: (data) => socket.to(`conversation_${data.conversationId}`).emit('user_typing', { 
                userId, 
                userInfo, 
                conversationId: data.conversationId,
                username: userInfo.username,
                isTyping: true
            }),
            typing_stop: (data) => socket.to(`conversation_${data.conversationId}`).emit('user_stopped_typing', { 
                userId, 
                conversationId: data.conversationId,
                username: userInfo.username,
                isTyping: false
            }),
            message_read: (data) => socket.to(`conversation_${data.conversationId}`).emit('message_read_by_user', { ...data, userId })
        };

        Object.entries(events).forEach(([event, handler]) => socket.on(event, handler));

        socket.on('disconnect', () => {
            console.log(`User ${userInfo.username} disconnected`);
            this.onlineUsers.delete(userId);
            this.userSockets.delete(socket.id);
            socket.broadcast.emit('user_offline', { userId });
        });
    }    // Real-time event emission methods

    emitNewMessage(conversationId, message) {
        // Thêm conversationId vào message data để client có thể filter
        const messageWithConversationId = {
            ...message,
            conversationId: conversationId
        };
        this._emitToConversation(conversationId, 'new_message', messageWithConversationId);
    }

    emitMessageUpdate(conversationId, message) {
        const messageWithConversationId = {
            ...message,
            conversationId: conversationId
        };
        this._emitToConversation(conversationId, 'message_updated', messageWithConversationId);
    }

    emitMessageDelete(conversationId, messageId, userId) {
        this._emitToConversation(conversationId, 'message_deleted', { 
            messageId, 
            deletedBy: userId, 
            conversationId: conversationId 
        });
    }

    notifyUser(userId, event, data) {
        this.io?.to(`user_${userId}`).emit(event, data);
    }

    // Helper method for conversation events
    _emitToConversation(conversationId, event, data) {
        if (this.io) {
            this.io.to(`conversation_${conversationId}`).emit(event, data);
            console.log(`Emitted ${event} to conversation ${conversationId}`);
        }
    }

    // Utility methods
    getOnlineUsers() { return Array.from(this.onlineUsers.keys()); }
    isUserOnline(userId) { return this.onlineUsers.has(userId); }
    getSocketByUserId(userId) { return this.onlineUsers.get(userId); }
}

module.exports = new SocketService();
