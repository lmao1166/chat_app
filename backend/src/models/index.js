// src/models/index.js
const { sequelize } = require('../configs/database');
const User = require('./user.model');
const Conversation = require('./conversation.model');
const Message = require('./message.model');
const ConversationMember = require('./conversationMember.model');
const Notification = require('./notification.model');
const InvalidatedToken = require('./invalidatedToken.model');

// --- Định nghĩa Quan hệ (Associations) ---

// 1. User - Message (One-to-Many)
User.hasMany(Message, {
    foreignKey: 'sender_id',
    as: 'sentMessages',
    onDelete: 'CASCADE'
});
Message.belongsTo(User, {
    foreignKey: 'sender_id',
    as: 'sender'
});

// 2. Conversation - Message (One-to-Many)
Conversation.hasMany(Message, {
    foreignKey: 'conversation_id',
    as: 'messages',
    onDelete: 'CASCADE'
});
Message.belongsTo(Conversation, {
    foreignKey: 'conversation_id',
    as: 'conversation'
});

// 3. User - Conversation (Many-to-Many thông qua ConversationMember)
User.belongsToMany(Conversation, {
    through: ConversationMember,
    foreignKey: 'user_id',
    otherKey: 'conversation_id',
    as: 'conversations'
});
Conversation.belongsToMany(User, {
    through: ConversationMember,
    foreignKey: 'conversation_id',
    otherKey: 'user_id',
    as: 'members'
});

// 4. ConversationMember - Message (Many-to-One)
ConversationMember.belongsTo(Message, {
    foreignKey: 'last_read_message_id',
    as: 'lastReadMessage',
    onDelete: 'SET NULL'
});


// 6. User - InvalidatedToken (One-to-Many)
User.hasMany(InvalidatedToken, {
    foreignKey: 'user_id',
    as: 'invalidatedTokens',
    onDelete: 'CASCADE'
});
InvalidatedToken.belongsTo(User, {
    foreignKey: 'user_id',
    as: 'user'
});

// Xuất tất cả các model và sequelize instance
module.exports = {
    sequelize,
    User,
    Conversation,
    Message,
    ConversationMember,
    Notification,
    InvalidatedToken
};