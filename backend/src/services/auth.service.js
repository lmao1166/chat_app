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
}

module.exports = new AuthService();