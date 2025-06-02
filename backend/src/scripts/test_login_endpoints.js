const axios = require('axios');

const BASE_URL = 'http://192.168.1.10:3000';

async function testLoginEndpoints() {
    console.log('Testing login endpoints...\n');
    
    const testData = {
        email: 'test@example.com',
        password: 'password123'
    };
    
    const endpoints = [
        '/api/v1/auth/login',     // Không có trailing slash
        '/api/v1/auth/login/',    // Có trailing slash
    ];
    
    for (const endpoint of endpoints) {
        try {
            console.log(`Testing: ${BASE_URL}${endpoint}`);
            
            const response = await axios.post(`${BASE_URL}${endpoint}`, testData, {
                headers: {
                    'Content-Type': 'application/json'
                },
                timeout: 5000
            });
            
            console.log(`✅ Success: ${response.status} - ${response.statusText}`);
            console.log(`Response: ${JSON.stringify(response.data, null, 2)}\n`);
            
        } catch (error) {
            if (error.response) {
                console.log(`❌ Error: ${error.response.status} - ${error.response.statusText}`);
                console.log(`Response: ${JSON.stringify(error.response.data, null, 2)}\n`);
            } else {
                console.log(`❌ Network Error: ${error.message}\n`);
            }
        }
    }
}

// Test browser-like request
async function testBrowserRequest() {
    console.log('Testing browser-like request...\n');
    
    try {
        const response = await axios.post('http://192.168.1.10:3000/api/v1/auth/login/', {
            email: 'test@example.com',
            password: 'password123'
        }, {
            headers: {
                'Content-Type': 'application/json',
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                'Accept': 'application/json, text/plain, */*',
                'Origin': 'http://localhost:3000'
            }
        });
        
        console.log('✅ Browser-like request successful');
        console.log(JSON.stringify(response.data, null, 2));
        
    } catch (error) {
        if (error.response) {
            console.log('❌ Browser-like request failed');
            console.log(JSON.stringify(error.response.data, null, 2));
        } else {
            console.log(`❌ Network Error: ${error.message}`);
        }
    }
}

async function runTests() {
    await testLoginEndpoints();
    await testBrowserRequest();
}

runTests().catch(console.error);
