const io = require('socket.io-client');

// Test configuration
const SERVER_URL = 'http://localhost:3000';
const TEST_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjIsImVtYWlsIjoiYWJjZEBnbWFpbC5jb20iLCJpYXQiOjE3NDg4MjYzMjUsImV4cCI6MTc0ODgyOTkyNSwiaXNzIjoibXktY2hhdC1hcHAifQ.aWJ41anj9C61TKNsbnvgFPQsdvsVNgrXbWe5cVWipSI'; // Replace with actual token

class SocketTester {
    constructor(token, username) {
        this.token = token;
        this.username = username;
        this.socket = null;
    }

    connect() {
        console.log(`\n🔌 Connecting ${this.username} to server...`);
        
        this.socket = io(SERVER_URL, {
            auth: {
                token: this.token
            }
        });

        this.setupEventListeners();
        
        return new Promise((resolve, reject) => {
            this.socket.on('connect', () => {
                console.log(`✅ ${this.username} connected with socket ID: ${this.socket.id}`);
                resolve(this.socket);
            });

            this.socket.on('connect_error', (error) => {
                console.error(`❌ ${this.username} connection failed:`, error.message);
                reject(error);
            });
        });
    }

    setupEventListeners() {
        // Listen for new messages
        this.socket.on('new_message', (message) => {
            console.log(`\n📨 ${this.username} received new message:`, {
                id: message.id,
                content: message.content,
                sender: message.sender?.username,
                timestamp: new Date(message.timestamp).toLocaleString()
            });
        });

        // Listen for message updates
        this.socket.on('message_updated', (message) => {
            console.log(`\n✏️ ${this.username} received message update:`, {
                id: message.id,
                content: message.content,
                sender: message.sender?.username
            });
        });

        // Listen for message deletions
        this.socket.on('message_deleted', (data) => {
            console.log(`\n🗑️ ${this.username} received message deletion:`, {
                messageId: data.messageId,
                conversationId: data.conversationId,
                deletedBy: data.deletedBy
            });
        });

        // Listen for typing indicators
        this.socket.on('user_typing', (data) => {
            console.log(`\n⌨️ ${this.username} sees ${data.userInfo.username} is typing in conversation ${data.conversationId}`);
        });

        this.socket.on('user_stopped_typing', (data) => {
            console.log(`\n⏹️ ${this.username} sees user stopped typing in conversation ${data.conversationId}`);
        });

        // Listen for online/offline status
        this.socket.on('user_online', (data) => {
            console.log(`\n🟢 ${this.username} sees ${data.userInfo.username} came online`);
        });

        this.socket.on('user_offline', (data) => {
            console.log(`\n🔴 ${this.username} sees user ${data.userId} went offline`);
        });

        // Listen for message read status
        this.socket.on('message_read_by_user', (data) => {
            console.log(`\n👁️ ${this.username} sees message ${data.messageId} was read by user ${data.userId}`);
        });

        this.socket.on('disconnect', () => {
            console.log(`\n🔌 ${this.username} disconnected from server`);
        });
    }

    joinConversation(conversationId) {
        console.log(`\n🚪 ${this.username} joining conversation ${conversationId}`);
        this.socket.emit('join_conversation', conversationId);
    }

    leaveConversation(conversationId) {
        console.log(`\n🚪 ${this.username} leaving conversation ${conversationId}`);
        this.socket.emit('leave_conversation', conversationId);
    }

    startTyping(conversationId) {
        console.log(`\n⌨️ ${this.username} started typing in conversation ${conversationId}`);
        this.socket.emit('typing_start', { conversationId });
    }

    stopTyping(conversationId) {
        console.log(`\n⏹️ ${this.username} stopped typing in conversation ${conversationId}`);
        this.socket.emit('typing_stop', { conversationId });
    }

    markMessageAsRead(messageId, conversationId) {
        console.log(`\n👁️ ${this.username} marking message ${messageId} as read`);
        this.socket.emit('message_read', { messageId, conversationId });
    }

    disconnect() {
        if (this.socket) {
            console.log(`\n🔌 ${this.username} disconnecting...`);
            this.socket.disconnect();
        }
    }
}

// Demo function
async function runSocketDemo() {
    console.log('🚀 Starting Socket.IO Real-time Messaging Demo');
    console.log('📝 Note: Replace TEST_TOKEN with actual JWT tokens for testing');
    
    if (TEST_TOKEN === 'your_jwt_token_here') {
        console.log('\n❌ Error: Please replace TEST_TOKEN with actual JWT tokens');
        console.log('📋 Steps to get tokens:');
        console.log('1. Start the server: npm start');
        console.log('2. Register/login users via API to get JWT tokens');
        console.log('3. Replace TEST_TOKEN in this script');
        console.log('4. Run this script again: node src/scripts/test_socket.js');
        return;
    }

    try {
        // Create two socket connections (simulating two users)
        const user1 = new SocketTester(TEST_TOKEN, 'User1');
        const user2 = new SocketTester(TEST_TOKEN, 'User2');

        // Connect both users
        await user1.connect();
        await user2.connect();

        // Simulate conversation interactions
        const conversationId = 1; // Replace with actual conversation ID

        // Both users join the same conversation
        user1.joinConversation(conversationId);
        user2.joinConversation(conversationId);

        // Simulate typing indicators
        setTimeout(() => {
            user1.startTyping(conversationId);
        }, 1000);

        setTimeout(() => {
            user1.stopTyping(conversationId);
        }, 3000);

        // Simulate message read
        setTimeout(() => {
            user2.markMessageAsRead(1, conversationId); // Replace with actual message ID
        }, 5000);

        // Keep connections alive for testing
        console.log('\n⏰ Demo running... Press Ctrl+C to exit');
        
        // Cleanup after 30 seconds
        setTimeout(() => {
            console.log('\n🧹 Cleaning up connections...');
            user1.disconnect();
            user2.disconnect();
            process.exit(0);
        }, 30000);

    } catch (error) {
        console.error('❌ Demo failed:', error.message);
        process.exit(1);
    }
}

// Handle graceful shutdown
process.on('SIGINT', () => {
    console.log('\n👋 Shutting down gracefully...');
    process.exit(0);
});

// Run the demo
if (require.main === module) {
    runSocketDemo();
}

module.exports = SocketTester;