const jwtService = require('../services/jwt.service');

const authenticateToken = async (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ 
            success: false,
            message: 'Access token required' 
        });
    }

    try {
        // Kiểm tra xem token có bị vô hiệu hóa không
        const isInvalidated = await jwtService.isTokenInvalidated(token);
        if (isInvalidated) {
            return res.status(401).json({ 
                success: false,
                message: 'Token đã bị vô hiệu hóa' 
            });
        }

        const decoded = jwtService.verifyToken(token);
        req.user = decoded;
        req.token = token; // Lưu token để sử dụng trong logout
        next();
    } catch (error) {
        if (error.name === 'JsonWebTokenError') {
            return res.status(403).json({ 
                success: false,
                message: 'Token không hợp lệ' 
            });
        }
        if (error.name === 'TokenExpiredError') {
            return res.status(401).json({ 
                success: false,
                message: 'Token đã hết hạn' 
            });
        }
        return res.status(500).json({ 
            success: false,
            message: 'Lỗi xác thực token' 
        });
    }
};

module.exports = { authenticateToken };