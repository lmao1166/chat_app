const axios = require('axios');
const io = require('socket.io-client');

// Configuration
const BASE_URL = 'http://localhost:3000/api/v1';
const SOCKET_URL = 'http://localhost:3000';

// Test users
const testUsers = [
    { username: 'user1_socket', email: 'user1socket@test.com', password: 'password123' },
    { username: 'user2_socket', email: 'user2socket@test.com', password: 'password123' }
];

class RealTimeMessageTester {
    constructor() {
        this.users = [];
        this.sockets = [];
        this.conversationId = null;
    }

    async setup() {
        console.log('🚀 Setting up real-time messaging test...\n');

        // Register and login users
        for (let i = 0; i < testUsers.length; i++) {
            const userData = testUsers[i];
            let token;

            try {
                // Try to login first
                const loginResponse = await axios.post(`${BASE_URL}/auth/login`, {
                    email: userData.email,
                    password: userData.password
                });
                token = loginResponse.data.data.token;
                console.log(`✅ User ${userData.username} logged in successfully`);
            } catch (error) {
                if (error.response?.status === 400) {
                    // User doesn't exist, register them
                    try {
                        await axios.post(`${BASE_URL}/users`, userData);
                        console.log(`✅ User ${userData.username} registered successfully`);

                        // Now login
                        const loginResponse = await axios.post(`${BASE_URL}/auth/login`, {
                            email: userData.email,
                            password: userData.password
                        });
                        token = loginResponse.data.data.token;
                        console.log(`✅ User ${userData.username} logged in successfully`);
                    } catch (regError) {
                        console.error(`❌ Failed to register user ${userData.username}:`, regError.response?.data?.message || regError.message);
                        throw regError;
                    }
                } else {
                    console.error(`❌ Failed to login user ${userData.username}:`, error.response?.data?.message || error.message);
                    throw error;
                }
            }

            this.users.push({
                ...userData,
                token,
                id: i + 1
            });
        }

        // Create a conversation between users
        await this.createConversation();

        // Setup Socket.IO connections
        await this.setupSockets();
    }

    async createConversation() {
        try {
            // Create a conversation between the two users
            const response = await axios.post(
                `${BASE_URL}/conversations`,
                {
                    members: [this.users[0].id, this.users[1].id],
                    type: 'private'
                },
                {
                    headers: { Authorization: `Bearer ${this.users[0].token}` }
                }
            );
            
            this.conversationId = response.data.data.id;
            console.log(`✅ Conversation created with ID: ${this.conversationId}\n`);
        } catch (error) {
            console.error('❌ Failed to create conversation:', error.response?.data?.message || error.message);
            throw error;
        }
    }

    async setupSockets() {
        console.log('🔌 Setting up Socket.IO connections...\n');

        for (let i = 0; i < this.users.length; i++) {
            const user = this.users[i];
            
            const socket = io(SOCKET_URL, {
                auth: {
                    token: user.token
                }
            });

            // Setup event listeners
            this.setupSocketListeners(socket, user.username);

            // Wait for connection
            await new Promise((resolve, reject) => {
                socket.on('connect', () => {
                    console.log(`✅ ${user.username} connected with socket ID: ${socket.id}`);
                    resolve();
                });

                socket.on('connect_error', (error) => {
                    console.error(`❌ ${user.username} connection failed:`, error.message);
                    reject(error);
                });
            });

            // Join the conversation
            socket.emit('join_conversation', this.conversationId);
            console.log(`🚪 ${user.username} joined conversation ${this.conversationId}`);

            this.sockets.push({ socket, user });
        }

        console.log('\n🎉 All users connected and joined conversation!\n');
    }

    setupSocketListeners(socket, username) {
        socket.on('new_message', (message) => {
            console.log(`\n📨 ${username} received new message:`, {
                id: message.id,
                content: message.content,
                sender: message.sender?.username,
                timestamp: new Date(message.timestamp).toLocaleString()
            });
        });

        socket.on('message_updated', (message) => {
            console.log(`\n✏️ ${username} received message update:`, {
                id: message.id,
                content: message.content,
                sender: message.sender?.username
            });
        });

        socket.on('message_deleted', (data) => {
            console.log(`\n🗑️ ${username} received message deletion:`, {
                messageId: data.messageId,
                conversationId: data.conversationId
            });
        });

        socket.on('user_typing', (data) => {
            console.log(`\n⌨️ ${username} sees ${data.userInfo.username} is typing...`);
        });

        socket.on('user_stopped_typing', (data) => {
            console.log(`\n⏹️ ${username} sees user stopped typing`);
        });

        socket.on('user_online', (data) => {
            console.log(`\n🟢 ${username} sees ${data.userInfo.username} came online`);
        });

        socket.on('user_offline', (data) => {
            console.log(`\n🔴 ${username} sees user went offline`);
        });
    }

    async testRealTimeMessaging() {
        console.log('📝 Testing real-time messaging...\n');

        // Test 1: Send a message from user1
        console.log('📤 Test 1: User1 sending a message...');
        const message1Response = await axios.post(
            `${BASE_URL}/messages`,
            {
                content: 'Hello from User1! This is a real-time test message.',
                conversation_id: this.conversationId
            },
            {
                headers: { Authorization: `Bearer ${this.users[0].token}` }
            }
        );
        const message1Id = message1Response.data.data.id;
        console.log(`✅ Message sent with ID: ${message1Id}`);

        await this.delay(2000);

        // Test 2: Send a reply from user2
        console.log('\n📤 Test 2: User2 sending a reply...');
        await axios.post(
            `${BASE_URL}/messages`,
            {
                content: 'Hi User1! I received your message in real-time!',
                conversation_id: this.conversationId
            },
            {
                headers: { Authorization: `Bearer ${this.users[1].token}` }
            }
        );

        await this.delay(2000);

        // Test 3: Typing indicators
        console.log('\n⌨️ Test 3: Testing typing indicators...');
        this.sockets[0].socket.emit('typing_start', { conversationId: this.conversationId });
        
        setTimeout(() => {
            this.sockets[0].socket.emit('typing_stop', { conversationId: this.conversationId });
        }, 3000);

        await this.delay(4000);

        // Test 4: Update a message
        console.log('\n✏️ Test 4: User1 updating the first message...');
        await axios.put(
            `${BASE_URL}/messages/${message1Id}`,
            {
                content: 'Hello from User1! This message has been updated in real-time!',
                user_id: this.users[0].id
            },
            {
                headers: { Authorization: `Bearer ${this.users[0].token}` }
            }
        );

        await this.delay(2000);

        // Test 5: Mark message as read
        console.log('\n👁️ Test 5: User2 marking message as read...');
        this.sockets[1].socket.emit('message_read', { 
            messageId: message1Id, 
            conversationId: this.conversationId 
        });

        await this.delay(2000);

        console.log('\n🎉 Real-time messaging tests completed!');
    }

    async testOnlineStatus() {
        console.log('\n👥 Testing online status...');
        
        // Disconnect user2
        console.log('🔌 Disconnecting User2...');
        this.sockets[1].socket.disconnect();

        await this.delay(2000);

        // Reconnect user2
        console.log('🔌 Reconnecting User2...');
        const socket = io(SOCKET_URL, {
            auth: {
                token: this.users[1].token
            }
        });

        this.setupSocketListeners(socket, this.users[1].username);

        await new Promise((resolve) => {
            socket.on('connect', () => {
                console.log(`✅ ${this.users[1].username} reconnected`);
                socket.emit('join_conversation', this.conversationId);
                resolve();
            });
        });

        this.sockets[1].socket = socket;
    }

    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    async cleanup() {
        console.log('\n🧹 Cleaning up...');
        
        // Disconnect all sockets
        this.sockets.forEach(({ socket, user }) => {
            if (socket.connected) {
                socket.disconnect();
                console.log(`🔌 ${user.username} disconnected`);
            }
        });

        console.log('✅ Cleanup completed');
    }

    async run() {
        try {
            await this.setup();
            await this.testRealTimeMessaging();
            await this.testOnlineStatus();
            
            console.log('\n⏰ Test will continue for 10 more seconds...');
            await this.delay(10000);
            
        } catch (error) {
            console.error('❌ Test failed:', error.message);
        } finally {
            await this.cleanup();
            process.exit(0);
        }
    }
}

// Run the test
if (require.main === module) {
    const tester = new RealTimeMessageTester();
    tester.run();
}

module.exports = RealTimeMessageTester;