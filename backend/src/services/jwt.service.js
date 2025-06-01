const jwt = require('jsonwebtoken');
const invalidatedTokenRepository = require('../repositories/invalidatedToken.repository');

class JwtService {
    generateToken(payload) {
        const accessToken = jwt.sign(
            payload,
            process.env.JWT_SECRET,
            {
                expiresIn: `${process.env.JWT_EXPIRATION}s`,
                issuer: process.env.JWT_ISSUER
            }
        )


        const refreshToken = jwt.sign(
            payload,
            process.env.JWT_SECRET,
            {
                expiresIn: `${process.env.JWT_REFRESH}s`,
                issuer: process.env.JWT_ISSUER
            }
        );

        return {
            accessToken,
            refreshToken
        };
    }

    verifyToken(token) {
        return jwt.verify(token,process.env.JWT_SECRET);
    }

    decodeToken(token) {
        return jwt.decode(token);
    }

    async isTokenInvalidated(token) {
        try {
            return await invalidatedTokenRepository.isTokenInvalidated(token);
        } catch (error) {
            throw new Error('Error checking token invalidation: ' + error.message);
        }
    }

    async invalidateToken(token, userId) {
        try {
            const decoded = this.decodeToken(token);
            if (!decoded || !decoded.exp) {
                throw new Error('Invalid token format');
            }

            const expiryTime = new Date(decoded.exp * 1000); // JWT exp is in seconds
            
            await invalidatedTokenRepository.create({
                token: token,
                user_id: userId,
                expiry_time: expiryTime
            });

            return true;
        } catch (error) {
            throw new Error('Error invalidating token: ' + error.message);
        }
    }

    getTokenExpiryTime(token) {
        try {
            const decoded = this.decodeToken(token);
            if (!decoded || !decoded.exp) {
                return null;
            }
            return new Date(decoded.exp * 1000);
        } catch (error) {
            return null;
        }
    }
}

module.exports = new JwtService();