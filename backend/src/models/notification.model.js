// src/models/notification.model.js
const { DataTypes } = require('sequelize');
const { sequelize } = require('../configs/database');

const Notification = sequelize.define('Notification', {
    id: {
        type: DataTypes.INTEGER,
        autoIncrement: true,    
        primaryKey: true,
        unique: true
    },
    user_id: {
        type: DataTypes.INTEGER,
        allowNull: false,
        references: {
            model: 'users',
            key: 'id'
        }
    },
    type: {
        type: DataTypes.STRING(255),
        allowNull: false
    },
    content: {
        type: DataTypes.TEXT,
        allowNull: true
    },
    is_read: {
        type: DataTypes.BOOLEAN,
        defaultValue: false
    },
    related_entity_id: {
        type: DataTypes.INTEGER,
        allowNull: true
    }
}, {
    tableName: 'notifications',
    timestamps: true,
    underscored: true,
});

module.exports = Notification;