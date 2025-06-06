// src/models/conversation_member.model.js
const { DataTypes } = require('sequelize');
const { sequelize } = require('../configs/database');

const ConversationMember = sequelize.define('ConversationMember', {
    joined_at: {
        type: DataTypes.DATE,
        allowNull: false
    },//
    left_at: {
        type: DataTypes.DATE,
        allowNull: true
    },//
}, {
    tableName: 'conversation_members',
    timestamps: false,
    underscored: true,
});

module.exports = ConversationMember;