// src/models/conversation.model.js
const { DataTypes } = require('sequelize');
const { sequelize } = require('../configs/database');

const Conversation = sequelize.define('Conversation', {
    id: {
        type: DataTypes.INTEGER, // <-- Đổi từ UUID sang INTEGER
        autoIncrement: true,     // <-- Tự động tăng
        primaryKey: true,
        unique: true
    },
    type: {
        type: DataTypes.ENUM('private', 'group'),
        allowNull: false
    },
    name: {
        type: DataTypes.STRING(255),
        allowNull: true
    },    thumbnail: {
        type: DataTypes.STRING,
        allowNull: true
    },
    last_message_at: {
        type: DataTypes.DATE,
        allowNull: true,
        defaultValue: null
    },

}, {
    tableName: 'conversations',
    timestamps: true,
    underscored: true,
});

module.exports = Conversation;