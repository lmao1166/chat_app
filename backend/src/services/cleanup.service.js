// src/services/cleanup.service.js
const invalidatedTokenRepository = require('../repositories/invalidatedToken.repository');

class CleanupService {
    constructor() {
        this.intervalId = null;
    }

    // Bắt đầu tự động dọn dẹp token hết hạn
    startTokenCleanup() {
        // Chạy cleanup mỗi 1 giờ (3600000 ms)
        this.intervalId = setInterval(async () => {
            try {
                const deletedCount = await invalidatedTokenRepository.cleanupExpiredTokens();
                if (deletedCount > 0) {
                    console.log(`Đã xóa ${deletedCount} token hết hạn`);
                }
            } catch (error) {
                console.error('Lỗi khi dọn dẹp token hết hạn:', error.message);
            }
        }, 3600000); // 1 giờ

        console.log('Token cleanup service đã được khởi động');
    }

    // Dừng tự động dọn dẹp
    stopTokenCleanup() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
            console.log('Token cleanup service đã được dừng');
        }
    }

    // Dọn dẹp thủ công
    async manualCleanup() {
        try {
            const deletedCount = await invalidatedTokenRepository.cleanupExpiredTokens();
            console.log(`Dọn dẹp thủ công hoàn thành. Đã xóa ${deletedCount} token hết hạn`);
            return deletedCount;
        } catch (error) {
            console.error('Lỗi khi dọn dẹp thủ công:', error.message);
            throw error;
        }
    }
}

module.exports = new CleanupService();
