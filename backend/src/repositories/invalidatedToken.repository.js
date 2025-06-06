// src/repositories/invalidatedToken.repository.js
const invalidatedToken = require('../models/invalidatedToken.model');

class InvalidatedTokenRepository {
    async create(tokenData) {
        const transaction = await invalidatedToken.sequelize.transaction();
        try {
            const newToken = await invalidatedToken.create(tokenData, { transaction });
            await transaction.commit();
            return newToken;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error creating invalidated token: ' + error.message);
        }
    }

    async findByToken(token) {
        try {
            const tokenRecord = await invalidatedToken.findOne({ 
                where: { token: token } 
            });
            return tokenRecord;
        } catch (error) {
            throw new Error('Error finding invalidated token: ' + error.message);
        }
    }    
    
    
    async isTokenInvalidated(token) {
        try {
            const tokenRecord = await this.findByToken(token);
            if (!tokenRecord) {
                return false;
            }
            
            // Kiểm tra nếu token đã hết hạn
            // Token được coi là vô hiệu hóa (invalidated) nếu hiện tại > thời gian hết hạn
            const now = new Date();
            return now > tokenRecord.expiry_time;
        } catch (error) {
            throw new Error('Error checking token status: ' + error.message);
        }
    }

    async cleanupExpiredTokens() {
        const transaction = await invalidatedToken.sequelize.transaction();
        try {
            const now = new Date();
            const deletedRows = await invalidatedToken.destroy({
                where: {
                    expiry_time: {
                        [require('sequelize').Op.lt]: now
                    }
                },
                transaction: transaction
            });
            await transaction.commit();
            return deletedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error cleaning up expired tokens: ' + error.message);
        }
    }

    async invalidateAllUserTokens(userId) {
        const transaction = await invalidatedToken.sequelize.transaction();
        try {
            // Trong thực tế, bạn có thể muốn lưu tất cả các token active của user
            // Ở đây chúng ta sẽ đánh dấu user_id để có thể kiểm tra sau
            const result = await invalidatedToken.create({
                token: `user_${userId}_all_tokens_${Date.now()}`,
                user_id: userId,
                expiry_time: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7 ngày
            }, { transaction });
            
            await transaction.commit();
            return result;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error invalidating all user tokens: ' + error.message);
        }
    }
}

module.exports = new InvalidatedTokenRepository();
