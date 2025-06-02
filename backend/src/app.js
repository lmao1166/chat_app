const express = require('express');
const errorHandler = require('./middlewares/errorHandler'); 

// Create Express server
const app = express();
const userRouter = require('./routes/user.routes');
const conversationRouter = require('./routes/conversation.routes');
const messageRouter = require('./routes/message.routes');
const authRouter = require('./routes/auth.routes');
// const notificationRouter = require('./routes/notification.routes');

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'OK',
    message: 'Server is running',
    timestamp: new Date().toISOString()
  });
});

// Basic routes
app.get('/api/v1/', (req, res) => {
  res.json({
    message: 'Welcome to ChatApp API',
    version: '1.0.0'
  });
});


app.use('/api/v1/users', userRouter)
app.use('/api/v1/conversations', conversationRouter)
app.use('/api/v1/messages', messageRouter)
app.use('/api/v1/auth', authRouter);
// app.use('/api/v1/notifications', notificationRouter);
// Import message routes

// Error handling middleware
app.use((req, res, next) => {
    const error = new Error('Endpoint không tồn tại'); // Tạo một lỗi mới
    error.statusCode = 404; // Gán mã lỗi 404
    next(error); // Chuyển lỗi này đến middleware xử lý lỗi chung
});

app.use(errorHandler)

module.exports = app;


