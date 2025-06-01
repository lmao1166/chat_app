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
        allowNull: false
    },
    attachment_url: {
        type: DataTypes.STRING(255),
        allowNull: true
    },
}, {
    tableName: 'messages',
    timestamps: false,
    underscored: true,
});

module.exports = Message;