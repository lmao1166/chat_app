// src/models/message.model.js
const { DataTypes } = require('sequelize');
const { sequelize } = require('../configs/database');

const Message = sequelize.define('Message', {
    id: {
        type: DataTypes.INTEGER, 
        autoIncrement: true,    
        primaryKey: true,
        unique: true
    },

    content: {
        type: DataTypes.TEXT,
        allowNull: false
    },
    timestamp: {
        type: DataTypes.DATE,
        allowNull: false,
        defaultValue: DataTypes.NOW
    },
    attachment_url: {
        type: DataTypes.STRING(255),
        allowNull: true
    },
    message_type: {
        type: DataTypes.ENUM('text', 'image', 'file', 'system'),
        allowNull: false,
        defaultValue: 'text'
    },
    delivery_status: {
        type: DataTypes.ENUM('sent', 'delivered', 'failed'),
        allowNull: false,
        defaultValue: 'sent'
    },
    is_deleted: {
        type: DataTypes.BOOLEAN,
        allowNull: false,
        defaultValue: false
    }
}, {
    tableName: 'messages',
    timestamps: true,
    underscored: true,
});

module.exports = Message;