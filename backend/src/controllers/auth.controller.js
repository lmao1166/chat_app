const jwtService = require('../services/jwt.service');
const AuthService = require('../services/auth.service');
const userService = require('../services/user.service');
const { get } = require('lodash');


const login = async (req, res, next) => {
    try {
        const tokens = await AuthService.login(req.body);
        res.status(200).json({
            status: 200,
            success: true,
            data: tokens,
            message: 'Đăng nhập thành công'
        });
    } catch (error) {
        next(error);
    }
};

const logout = async (req, res, next) => {
    try {
        const authHeader = req.headers['authorization'];
        const token = authHeader && authHeader.split(' ')[1];
        
        if (!token) {
            const error = new Error('Token là bắt buộc');
            error.statusCode = 401;
            throw error;
        }

        const userId = req.user.userId; // Lấy từ middleware authentication
        
        const result = await AuthService.logout(token, userId);
        res.status(200).json({
            status: 200,
            success: true,
            data: result,
            message: 'Đăng xuất thành công'
        });
    } catch (error) {
        next(error);
    }
};

const logoutAll = async (req, res, next) => {
    try {
        const userId = req.user.userId; // Lấy từ middleware authentication
        
        const result = await AuthService.logoutAll(userId);
        res.status(200).json({
            status: 200,
            success: true,
            data: result,
            message: 'Đăng xuất khỏi tất cả thiết bị thành công'
        });
    } catch (error) {
        next(error);
    }
};

const getCurrentUser = async (req, res, next) => {
    try {
        const userId = req.user.userId; // Lấy từ middleware authentication
        const user = await userService.getUserById(userId);
        
        res.status(200).json({
            status: 200,
            success: true,
            data: user,
            message: 'Lấy thông tin người dùng thành công'
        });
    } catch (error) {
        next(error);
    }
}

module.exports = { 
    login, 
    logout, 
    logoutAll, 
    getCurrentUser
};