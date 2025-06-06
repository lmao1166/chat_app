const {DataTypes, Model} = require('sequelize');
const database = require('../configs/database'); // Đường dẫn đến file cấu hình kết nối database

const User = database.sequelize.define('User', {
    id:{
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        unique: true,
    },
    username:{
        type: DataTypes.STRING(255),
        allowNull: false,
    },
    email:{
        type: DataTypes.STRING(255),
        allowNull: false,
        unique: true,
    },
    password:{
        type: DataTypes.STRING(255), // Giả sử mã hóa password bằng bcrypt, nên giới hạn độ dài
        allowNull: false,
    },    
    profilePicUrl:{
        type: DataTypes.STRING,
        allowNull: true,
        defaultValue: '/api/v1/uploads/profiles/default-avatar.png'
    },
    // last_seen:{
    //     type: DataTypes.DATE,
    //     allowNull: true
    // },

}, {
    tableName: 'users',
    timestamps: true, 
    underscored: true,
})

module.exports = User;