// src/scripts/test_logout.js
require('dotenv').config();

const axios = require('axios');

const BASE_URL = 'http://localhost:3000/api/v1';

async function testLogoutFlow() {
    try {
        console.log('=== Test Logout Flow ===\n');

        // 1. Tạo user mới (nếu chưa có)
        console.log('1. Đăng ký user mới...');
        const registerData = {
            username: 'testuser',
            email: 'test@example.com',
            password: '123456',
            confirmPassword: '123456'
        };

        try {
            await axios.post(`${BASE_URL}/users`, registerData);
            console.log('✓ Đăng ký thành công');
        } catch (error) {
            if (error.response?.status === 400) {
                console.log('ℹ User đã tồn tại, tiếp tục test...');
            } else {
                throw error;
            }
        }

        // 2. Đăng nhập
        console.log('\n2. Đăng nhập...');
        const loginData = {
            email: 'test@example.com',
            password: '123456'
        };

        const loginResponse = await axios.post(`${BASE_URL}/auth/login`, loginData);
        const { accessToken } = loginResponse.data.data;
        console.log('✓ Đăng nhập thành công');
        console.log('Token:', accessToken.substring(0, 50) + '...');

        // 3. Test API với token
        console.log('\n3. Test API với token...');
        const authHeaders = {
            Authorization: `Bearer ${accessToken}`
        };

        const profileResponse = await axios.get(`${BASE_URL}/conversations`, {
            headers: authHeaders
        });
        console.log('✓ Truy cập API thành công');

        // 4. Logout
        console.log('\n4. Logout...');
        const logoutResponse = await axios.post(`${BASE_URL}/auth/logout`, {}, {
            headers: authHeaders
        });
        console.log('✓ Logout thành công:', logoutResponse.data.message);

        // 5. Test API sau logout (sẽ thất bại)
        console.log('\n5. Test API sau logout (sẽ thất bại)...');
        try {
            await axios.get(`${BASE_URL}/conversations`, {
                headers: authHeaders
            });
            console.log('✗ FAILED: API vẫn hoạt động sau logout');
        } catch (error) {
            if (error.response?.status === 401) {
                console.log('✓ API đã từ chối token sau logout');
            } else {
                throw error;
            }
        }

        console.log('\n=== Test Logout Flow Hoàn Thành ===');

    } catch (error) {
        console.error('Lỗi trong test:', error.response?.data || error.message);
    }
}

// Chỉ chạy khi file này được gọi trực tiếp
if (require.main === module) {
    testLogoutFlow();
}

module.exports = { testLogoutFlow };
