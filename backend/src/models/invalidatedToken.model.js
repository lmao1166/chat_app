// src/models/invalidatedToken.model.js
const { DataTypes } = require('sequelize');
const { sequelize } = require('../configs/database');

const InvalidatedToken = sequelize.define('InvalidatedToken', {
    id: {
        type: DataTypes.INTEGER,
        autoIncrement: true,
        primaryKey: true,
        unique: true
    },    token: {
        type: DataTypes.STRING(500),
        allowNull: false,
        unique: true
    },
    expiry_time: {
        type: DataTypes.DATE,
        allowNull: false
    },
    user_id: {
        type: DataTypes.INTEGER,
        allowNull: false
    }
}, {
    tableName: 'invalidated_tokens',
    timestamps: true,
    underscored: true,
});

module.exports = InvalidatedToken;