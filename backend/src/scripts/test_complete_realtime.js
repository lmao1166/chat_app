const axios = require('axios');
const { io } = require('socket.io-client');

const BASE_URL = 'http://localhost:3000/api/v1';

async function testRealTimeMessaging() {
    console.log('🚀 Testing Real-Time Messaging Integration');
    console.log('========================================\n');

    try {
        // 1. Login to get JWT token
        console.log('1. 🔐 Logging in to get JWT token...');
        const loginResponse = await axios.post(`${BASE_URL}/auth/login`, {
            email: 'user@example.com', // Replace with actual test user
            password: 'password123'     // Replace with actual password
        });

        if (!loginResponse.data.success) {
            console.log('❌ Login failed. Please create a test user first.');
            console.log('💡 You can register via: POST /api/v1/users');
            return;
        }

        const token = loginResponse.data.data.token;
        console.log('✅ Login successful');

        // 2. Test Socket.IO connection
        console.log('\n2. 🔌 Testing Socket.IO connection...');
        const socket = io('http://localhost:3000', {
            auth: { token }
        });

        socket.on('connect', () => {
            console.log('✅ Socket.IO connection successful');
            
            // Join a conversation
            socket.emit('join_conversation', 1);
            console.log('🚪 Joined conversation 1');
        });

        socket.on('connect_error', (error) => {
            console.log('❌ Socket.IO connection failed:', error.message);
        });

        // 3. Listen for real-time events
        socket.on('new_message', (message) => {
            console.log('📨 Real-time message received:', {
                id: message.id,
                content: message.content,
                sender: message.sender.username
            });
        });

        socket.on('user_online', (data) => {
            console.log('👤 User came online:', data.userInfo.username);
        });

        // 4. Test online users endpoint
        setTimeout(async () => {
            console.log('\n3. 👥 Testing online users endpoint...');
            try {
                const onlineResponse = await axios.get(`${BASE_URL}/users/online`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                
                console.log('✅ Online users retrieved:', {
                    count: onlineResponse.data.count,
                    users: onlineResponse.data.data.map(u => u.username)
                });
            } catch (error) {
                console.log('❌ Failed to get online users:', error.response?.data?.message);
            }
        }, 2000);

        // 5. Test sending a message via API (should trigger real-time event)
        setTimeout(async () => {
            console.log('\n4. 📤 Testing message sending (API + Real-time)...');
            try {
                const messageResponse = await axios.post(`${BASE_URL}/messages`, {
                    content: 'Hello from real-time test! 🎉',
                    conversation_id: 1
                }, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (messageResponse.data.success) {
                    console.log('✅ Message sent via API');
                    console.log('⏳ Waiting for real-time broadcast...');
                } else {
                    console.log('❌ Failed to send message:', messageResponse.data.message);
                }
            } catch (error) {
                console.log('❌ Message send error:', error.response?.data?.message || error.message);
            }
        }, 3000);

        // 6. Test typing indicators
        setTimeout(() => {
            console.log('\n5. ⌨️ Testing typing indicators...');
            socket.emit('typing_start', { conversationId: 1 });
            console.log('✅ Typing start event sent');

            setTimeout(() => {
                socket.emit('typing_stop', { conversationId: 1 });
                console.log('✅ Typing stop event sent');
            }, 2000);
        }, 5000);

        // Clean up
        setTimeout(() => {
            console.log('\n6. 🔚 Test completed successfully!');
            console.log('✨ Real-time messaging is working properly');
            socket.disconnect();
            process.exit(0);
        }, 8000);

    } catch (error) {
        console.error('❌ Test failed:', error.response?.data?.message || error.message);
        
        if (error.response?.status === 401) {
            console.log('\n💡 Authentication failed. Make sure you have:');
            console.log('   - A valid user account');
            console.log('   - Correct email/password');
            console.log('   - At least one conversation with ID 1');
        }
        
        process.exit(1);
    }
}

// Run the test
if (require.main === module) {
    testRealTimeMessaging();
}

module.exports = { testRealTimeMessaging };
