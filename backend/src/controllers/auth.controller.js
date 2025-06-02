const jwtService = require('../services/jwt.service');
const AuthService = require('../services/auth.service');


const login = async (req, res, next) => {
    try {
        const tokens = await AuthService.login(req.body);
        res.status(200).json({
            status: 200,
            success: true,
            data: tokens,
            message: 'Login successful'
        });
    } catch (error) {
        next(error);
    }
};

module.exports = { login };