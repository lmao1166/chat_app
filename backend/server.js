require('dotenv').config();

const app = require('./src/app'); 
const {sequelize, connectDB} = require('./src/configs/database'); // Import sequelize và connectDB từ file cấu hình database
const cleanupService = require('./src/services/cleanup.service'); // Import cleanup service

// Import tất cả models để đảm bảo chúng được đăng ký với Sequelize
require('./src/models/index'); // Import models và associations
require('./src/models/user.model'); // Import model User
require('./src/models/conversation.model'); // Import model Conversation
require('./src/models/message.model'); // Import model Message
require('./src/models/conversationMember.model'); // Import model ConversationMember
require('./src/models/invalidatedToken.model'); // Import model InvalidatedToken

const PORT = process.env.PORT || 3000;

async function startServer() {
    // 1. Kết nối đến database
    await connectDB(); // Gọi hàm kiểm tra kết nối từ database.js

    // 2. Đồng bộ hóa các model với database (tạo bảng nếu chưa tồn tại)
    // CHÚ Ý QUAN TRỌNG:
    // `force: false` nghĩa là: Nếu bảng đã tồn tại, KHÔNG làm gì cả.
    // `force: true` nghĩa là: Nếu bảng đã tồn tại, HÃY XÓA NÓ VÀ TẠO LẠI.
    // Dùng `force: true` chỉ trong môi trường phát triển (development) để tiện reset DB.
    // Trong môi trường production, bạn sẽ dùng Sequelize Migrations để quản lý thay đổi schema.
    try {
        await sequelize.sync({ force: false });
        console.log('Đã đồng bộ hóa các model với database.');
    } catch (error) {
        console.error('Lỗi khi đồng bộ hóa database:', error);
        process.exit(1); // Thoát ứng dụng nếu không thể đồng bộ hóa
    }    // 3. Khởi động server lắng nghe các yêu cầu HTTP với Socket.IO
    const http = require('http');
    const { Server } = require('socket.io');
    
    const server = http.createServer(app);
    const io = new Server(server, {
        cors: {
            origin: "*",
            methods: ["GET", "POST"]
        }
    });

    // Import và khởi tạo socket service
    const socketService = require('./src/services/socket.service');
    socketService.initialize(io);    server.listen(PORT, '0.0.0.0', () => {
        console.log(`Server đang chạy trên cổng ${PORT}`);
        console.log(`Truy cập API tại: http://localhost:${PORT}/api/v1`);
        console.log(`Socket.IO server đã sẵn sàng cho real-time messaging`);
        
        // Khởi động cleanup service để tự động xóa token hết hạn
        cleanupService.startTokenCleanup();
    });
}

startServer().catch(error => {
    console.error('Lỗi khi khởi động server:', error);
    process.exit(1); // Thoát ứng dụng nếu có lỗi
});