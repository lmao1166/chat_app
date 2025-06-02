const express = require('express');
const cors = require('cors');
const errorHandler = require('./middlewares/errorHandler'); 

// Create Express server
const app = express();
const userRouter = require('./routes/user.routes');
const conversationRouter = require('./routes/conversation.routes');
const messageRouter = require('./routes/message.routes');
const authRouter = require('./routes/auth.routes');
const uploadRouter = require('./routes/upload.routes');
// const notificationRouter = require('./routes/notification.routes');

// CORS configuration for both HTTP and Socket.IO
app.use(cors({
    origin: "*",
    methods: ["GET", "POST", "PUT", "DELETE"],
    allowedHeaders: ["Content-Type", "Authorization"],
    credentials: true
}));

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Middleware để xử lý trailing slash
app.use((req, res, next) => {
    if (req.path !== '/' && req.path.endsWith('/')) {
        const newPath = req.path.slice(0, -1);
        return res.redirect(301, newPath + req.url.slice(req.path.length));
    }
    next();
});

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
    version: '1.0.0',
    endpoints: {
      auth: {
        'POST /api/v1/auth/login': 'User login',
        'POST /api/v1/auth/logout': 'User logout (requires token)',
        'POST /api/v1/auth/logout-all': 'Logout from all devices (requires token)',
        'GET /api/v1/auth/login': 'Login endpoint info (for testing)'
      },
      users: {
        'GET /api/v1/users': 'Get all users',
        'POST /api/v1/users': 'Create user',
        'PUT /api/v1/users': 'Update current user (requires token)',
        'DELETE /api/v1/users': 'Delete current user (requires token)'
      },
      messages: {
        'GET /api/v1/messages': 'Get messages (requires token)',
        'POST /api/v1/messages': 'Send message (requires token)',
        'PUT /api/v1/messages/:id': 'Update message (requires token)',
        'DELETE /api/v1/messages/:id': 'Delete message (requires token)'
      },
      conversations: {
        'GET /api/v1/conversations': 'Get conversations (requires token)',
        'POST /api/v1/conversations': 'Create conversation (requires token)'
      }
    }
  });
});


app.use('/api/v1/users', userRouter)
app.use('/api/v1/conversations', conversationRouter)
app.use('/api/v1/messages', messageRouter)
app.use('/api/v1/auth', authRouter);
app.use('/api/v1/uploads', uploadRouter);
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


