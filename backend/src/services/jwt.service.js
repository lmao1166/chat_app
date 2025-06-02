const jwt = require('jsonwebtoken');

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
}

module.exports = new JwtService();