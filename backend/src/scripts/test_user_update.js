// Test script để demo cách update user info
const fs = require('fs');
const path = require('path');
const FormData = require('form-data');
const axios = require('axios');

const BASE_URL = 'http://localhost:3000/api/v1';

// Mock JWT token - thay bằng token thật khi test
const JWT_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'; // Thay bằng token thật

// Test 1: Update user info thường (không có file)
async function testUpdateUserInfo() {
    try {
        console.log('🧪 Test 1: Update user info (JSON)');
        
        const userData = {
            fullName: 'Nguyễn Văn Test',
            email: 'test@example.com',
            phoneNumber: '0987654321',
            status: 'online'
        };

        const response = await axios.put(
            `${BASE_URL}/users/1`, // Thay 1 bằng user ID thật
            userData,
            {
                headers: {
                    'Authorization': `Bearer ${JWT_TOKEN}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        console.log('✅ Success:', response.data);
    } catch (error) {
        console.error('❌ Error:', error.response?.data || error.message);
    }
}

// Test 2: Update user info + profile picture
async function testUpdateUserWithProfile() {
    try {
        console.log('🧪 Test 2: Update user with profile picture');
        
        const formData = new FormData();
        
        // Thêm thông tin user
        formData.append('fullName', 'Nguyễn Văn Test Profile');
        formData.append('email', 'testprofile@example.com');
        formData.append('status', 'busy');
        
        // Thêm file ảnh (sử dụng default-avatar.png làm ví dụ)
        const imagePath = path.join(__dirname, '../uploads/profiles/default-avatar.png');
        if (fs.existsSync(imagePath)) {
            formData.append('profilePicture', fs.createReadStream(imagePath));
        } else {
            console.log('⚠️  File default-avatar.png không tồn tại, skip file upload');
        }

        const response = await axios.put(
            `${BASE_URL}/users/1/with-profile`, // Thay 1 bằng user ID thật
            formData,
            {
                headers: {
                    'Authorization': `Bearer ${JWT_TOKEN}`,
                    ...formData.getHeaders()
                }
            }
        );

        console.log('✅ Success:', response.data);
    } catch (error) {
        console.error('❌ Error:', error.response?.data || error.message);
    }
}

// Test 3: Update password
async function testUpdatePassword() {
    try {
        console.log('🧪 Test 3: Update password');
        
        const userData = {
            password: 'newpassword123',
            confirmPassword: 'newpassword123'
        };

        const response = await axios.put(
            `${BASE_URL}/users/1`, // Thay 1 bằng user ID thật
            userData,
            {
                headers: {
                    'Authorization': `Bearer ${JWT_TOKEN}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        console.log('✅ Success:', response.data);
    } catch (error) {
        console.error('❌ Error:', error.response?.data || error.message);
    }
}

// Test 4: Update chỉ một field
async function testPartialUpdate() {
    try {
        console.log('🧪 Test 4: Partial update (chỉ status)');
        
        const userData = {
            status: 'away'
        };

        const response = await axios.put(
            `${BASE_URL}/users/1`, // Thay 1 bằng user ID thật
            userData,
            {
                headers: {
                    'Authorization': `Bearer ${JWT_TOKEN}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        console.log('✅ Success:', response.data);
    } catch (error) {
        console.error('❌ Error:', error.response?.data || error.message);
    }
}

// Test 5: Validation error
async function testValidationError() {
    try {
        console.log('🧪 Test 5: Validation error');
        
        const userData = {
            email: 'invalid-email', // Email không hợp lệ
            phoneNumber: '123', // Số điện thoại quá ngắn
            status: 'invalid-status' // Status không hợp lệ
        };

        const response = await axios.put(
            `${BASE_URL}/users/1`, // Thay 1 bằng user ID thật
            userData,
            {
                headers: {
                    'Authorization': `Bearer ${JWT_TOKEN}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        console.log('✅ Success:', response.data);
    } catch (error) {
        console.log('✅ Expected validation error:', error.response?.data);
    }
}

// Chạy tất cả tests
async function runAllTests() {
    console.log('🚀 Starting User Update API Tests\n');
    
    await testUpdateUserInfo();
    console.log('\n' + '='.repeat(50) + '\n');
    
    await testUpdateUserWithProfile();
    console.log('\n' + '='.repeat(50) + '\n');
    
    await testUpdatePassword();
    console.log('\n' + '='.repeat(50) + '\n');
    
    await testPartialUpdate();
    console.log('\n' + '='.repeat(50) + '\n');
    
    await testValidationError();
    
    console.log('\n🏁 Tests completed!');
}

// Browser example (copy này vào console của browser)
function generateBrowserExample() {
    return `
// === BROWSER EXAMPLE ===
// Copy code này vào browser console để test

// 1. Update user info
const updateUserInfo = async () => {
    const token = 'YOUR_JWT_TOKEN_HERE'; // Thay bằng token thật
    
    const response = await fetch('/api/v1/users/1', {
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            fullName: 'Test Browser Update',
            status: 'online'
        })
    });
    
    const result = await response.json();
    console.log('Update result:', result);
};

// 2. Update với file upload
const updateWithFile = async () => {
    const token = 'YOUR_JWT_TOKEN_HERE'; // Thay bằng token thật
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = 'image/*';
    
    fileInput.click();
    
    fileInput.onchange = async () => {
        const formData = new FormData();
        formData.append('fullName', 'Test File Upload');
        formData.append('status', 'busy');
        formData.append('profilePicture', fileInput.files[0]);
        
        const response = await fetch('/api/v1/users/1/with-profile', {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token
            },
            body: formData
        });
        
        const result = await response.json();
        console.log('Update with file result:', result);
    };
};

// Chạy tests
updateUserInfo();
// updateWithFile(); // Uncomment để test file upload
`;
}

if (require.main === module) {
    // Chỉ chạy test nếu có JWT token
    if (JWT_TOKEN && JWT_TOKEN !== 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...') {
        runAllTests();
    } else {
        console.log('❌ Vui lòng thay JWT_TOKEN bằng token thật để chạy test');
        console.log('\n📝 Browser example:');
        console.log(generateBrowserExample());
    }
}

module.exports = {
    testUpdateUserInfo,
    testUpdateUserWithProfile,
    testUpdatePassword,
    testPartialUpdate,
    testValidationError
};
