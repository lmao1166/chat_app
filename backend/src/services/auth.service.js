const jwtService = require('./jwt.service');
const userService = require('./user.service');

class AuthService {
    async login(userData) {
        const { email, password } = userData;

        const user = await userService.findByEmail(email);
        if (!user || !(await userService.verifyPassword(password, user.password))) {
            throw new Error('Tài khoản hoặc mật khẩu không đúng');
        }

        const payload = {
            userId: user.id,
            email: user.email
        };

        const tokens = jwtService.generateToken(payload);
        return tokens;
    }

    async logout(token, userId) {
        try {
            // Kiểm tra xem token có hợp lệ không
            const decoded = jwtService.verifyToken(token);
            
            if (decoded.userId !== userId) {
                const error = new Error('Token không thuộc về người dùng này');
                error.statusCode = 403;
                throw error;
            }

            // Vô hiệu hóa token
            await jwtService.invalidateToken(token, userId);
            
            return { message: 'Đăng xuất thành công' };
        } catch (error) {
            if (error.name === 'JsonWebTokenError') {
                const customError = new Error('Token không hợp lệ');
                customError.statusCode = 401;
                throw customError;
            }
            if (error.name === 'TokenExpiredError') {
                const customError = new Error('Token đã hết hạn');
                customError.statusCode = 401;
                throw customError;
            }
            throw error;
        }
    }

    async logoutAll(userId) {
        try {
            // Vô hiệu hóa tất cả token của user
            const invalidatedTokenRepository = require('../repositories/invalidatedToken.repository');
            await invalidatedTokenRepository.invalidateAllUserTokens(userId);
            
            return { message: 'Đăng xuất khỏi tất cả thiết bị thành công' };
        } catch (error) {
            throw new Error('Lỗi khi đăng xuất khỏi tất cả thiết bị: ' + error.message);
        }
    }
}

module.exports = new AuthService();