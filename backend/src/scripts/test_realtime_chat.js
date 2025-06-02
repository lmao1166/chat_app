const { io } = require('socket.io-client');

// Configuration
const SERVER_URL = 'http://localhost:3000';
const TEST_TOKENS = [
    'YOUR_JWT_TOKEN_1', // Replace with actual JWT tokens
    'YOUR_JWT_TOKEN_2'
];

class ChatTestClient {
    constructor(token, username) {
        this.token = token;
        this.username = username;
        this.socket = null;
        this.conversationId = 1; // Default conversation ID
    }

    connect() {
        console.log(`[${this.username}] Connecting to server...`);
        
        this.socket = io(SERVER_URL, {
            auth: { token: this.token }
        });

        this.setupEventHandlers();
    }

    setupEventHandlers() {
        this.socket.on('connect', () => {
            console.log(`[${this.username}] ✅ Connected to server`);
            this.joinConversation(this.conversationId);
        });

        this.socket.on('disconnect', () => {
            console.log(`[${this.username}] ❌ Disconnected from server`);
        });

        this.socket.on('user_online', (data) => {
            console.log(`[${this.username}] 👤 ${data.userInfo.username} is now online`);
        });

        this.socket.on('user_offline', (data) => {
            console.log(`[${this.username}] 👤 User ${data.userId} went offline`);
        });

        this.socket.on('new_message', (message) => {
            console.log(`[${this.username}] 💬 ${message.sender.username}: ${message.content}`);
        });

        this.socket.on('message_updated', (message) => {
            console.log(`[${this.username}] 📝 Message updated: ${message.content}`);
        });

        this.socket.on('message_deleted', (data) => {
            console.log(`[${this.username}] 🗑️ Message ${data.messageId} was deleted`);
        });

        this.socket.on('user_typing', (data) => {
            console.log(`[${this.username}] ⌨️ ${data.userInfo.username} is typing...`);
        });

        this.socket.on('user_stopped_typing', (data) => {
            console.log(`[${this.username}] ✋ User stopped typing`);
        });

        this.socket.on('connect_error', (error) => {
            console.error(`[${this.username}] ❌ Connection error:`, error.message);
        });
    }

    joinConversation(conversationId) {
        this.conversationId = conversationId;
        this.socket.emit('join_conversation', conversationId);
        console.log(`[${this.username}] 🚪 Joined conversation ${conversationId}`);
    }

    startTyping() {
        this.socket.emit('typing_start', { conversationId: this.conversationId });
        console.log(`[${this.username}] ⌨️ Started typing`);
    }

    stopTyping() {
        this.socket.emit('typing_stop', { conversationId: this.conversationId });
        console.log(`[${this.username}] ✋ Stopped typing`);
    }

    simulateTyping() {
        this.startTyping();
        setTimeout(() => this.stopTyping(), 2000);
    }

    disconnect() {
        if (this.socket) {
            this.socket.disconnect();
            console.log(`[${this.username}] 👋 Disconnected`);
        }
    }
}

// Test script
async function runSocketTest() {
    console.log('🚀 Starting Socket.IO Real-Time Chat Test\n');

    if (TEST_TOKENS[0] === 'YOUR_JWT_TOKEN_1') {
        console.log('❌ Please replace TEST_TOKENS with actual JWT tokens from your API');
        console.log('💡 You can get tokens by logging in via: POST /api/v1/auth/login');
        return;
    }

    // Create test clients
    const client1 = new ChatTestClient(TEST_TOKENS[0], 'User1');
    const client2 = new ChatTestClient(TEST_TOKENS[1], 'User2');

    // Connect clients
    client1.connect();
    
    setTimeout(() => {
        client2.connect();
    }, 1000);

    // Simulate typing after connections are established
    setTimeout(() => {
        console.log('\n📝 Testing typing indicators...');
        client1.simulateTyping();
    }, 3000);

    setTimeout(() => {
        client2.simulateTyping();
    }, 4000);

    // Keep alive for testing
    setTimeout(() => {
        console.log('\n🔚 Test completed. Disconnecting clients...');
        client1.disconnect();
        client2.disconnect();
        process.exit(0);
    }, 10000);
}

// Instructions
console.log('📋 Socket.IO Real-Time Chat Test');
console.log('=====================================');
console.log('This script tests the real-time functionality of the chat app.');
console.log('');
console.log('Before running:');
console.log('1. Make sure your server is running on http://localhost:3000');
console.log('2. Replace TEST_TOKENS with actual JWT tokens');
console.log('3. Ensure you have at least one conversation with ID 1');
console.log('');
console.log('To get JWT tokens:');
console.log('POST /api/v1/auth/login with valid credentials');
console.log('');

if (require.main === module) {
    runSocketTest().catch(console.error);
}

module.exports = { ChatTestClient };
