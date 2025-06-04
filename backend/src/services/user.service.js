const userRepository = require('../repositories/user.repository');
const bcrypt = require('bcryptjs');
const FileUtils = require('../utils/file.utils');


class UserService {
    async getAllUsers() {
        try {
            const users = await userRepository.findAll(); //users 
            const domain = process.env.DOMAIN || 'localhost:3000';
            return users.map(user => ({
                id: user.id,
                username: user.username,
                email: user.email,
                profilePicUrl: user.profilePicUrl ? `http://${domain}/api/v1/uploads/profiles/${user.profilePicUrl}` : null,
                createdAt: user.createdAt,
                updatedAt: user.updatedAt
            }));
        } catch (error) {
            throw new Error('Error fetching users: ' + error.message);
        }
    }

    async getUserById(id) {
        const user = await userRepository.findById(id);

        if (!user) {
            const error = new Error('User not found');
            error.statusCode = 404;
            throw error;
        }

        const domain = process.env.DOMAIN || 'localhost:3000';
        const data = {
            id: user.id,
            username: user.username,
            email: user.email,
            profilePicUrl: user.profilePicUrl ? `http://${domain}/api/v1/uploads/profiles/${user.profilePicUrl}` : null,
            createdAt: user.createdAt,
            updatedAt: user.updatedAt
        }
        return data;
    }
    async register(userData) {
        const existingUser = await userRepository.findByEmail(userData.email);
        if (existingUser) {
            const error = new Error('Người dùng đã tồn tại');
            error.statusCode = 400;
            throw error;
        }
        try {
            // Loại bỏ confirmPassword trước khi lưu
            userData.password = await bcrypt.hash(userData.password, 10);

            // Set ảnh đại diện mặc định nếu không có
            if (!userData.profilePicUrl) {
                userData.profilePicUrl = 'default-avatar.png';
            }

            const newUser = await userRepository.create(userData);

            // Trả về với domain prefix cho profilePicUrl
            const domain = process.env.DOMAIN || 'localhost:3000';
            return {
                ...newUser,
                profilePicUrl: newUser.profilePicUrl ? `http://${domain}/api/v1/uploads/profiles/${newUser.profilePicUrl}` : null
            };
        } catch (error) {
            throw new Error('Error creating user: ' + error.message);
        }
    }

    async updateUser(id, userData) {
        const user = await userRepository.findById(id);
        if (!user) {
            const error = new Error('User not found');
            error.statusCode = 404;
            throw error;
        }

        if (userData.email) {
            const existingUser = await userRepository.findByEmail(userData.email);
            if (existingUser && existingUser.id !== id) {
                const error = new Error('Email đã tồn tại');
                error.statusCode = 400;
                throw error;
            }
        }

        // Chỉ validate password khi user muốn thay đổi password
        if (userData.password) {
            if (userData.password.trim() === '') {
                const error = new Error('Mật khẩu không được để trống');
                error.statusCode = 400;
                throw error;
            }

            if (userData.password !== userData.confirmPassword) {
                const error = new Error('Mật khẩu không khớp');
                error.statusCode = 400;
                throw error;
            }

            userData.password = await bcrypt.hash(userData.password, 10);
        }

        if (!userData.profilePicUrl) {
            userData.profilePicUrl = user.profilePicUrl; // Giữ nguyên ảnh đại diện nếu không có mới
        }

        const { confirmPassword, ...userDataToUpdate } = userData;

        try {
            const updatedUser = await userRepository.update(id, userDataToUpdate);
            if (user.profilePicUrl != "default-avatar.png") {
                FileUtils.deleteFile(process.env.PROFILE_PICTURE_PATH + user.profilePicUrl);
            }
            // Trả về với domain prefix cho profilePicUrl
            const domain = process.env.DOMAIN || 'localhost:3000';
            return {
                ...updatedUser,
                profilePicUrl: updatedUser.profilePicUrl ? `http://${domain}/api/v1/uploads/profiles/${updatedUser.profilePicUrl}` : null
            };
        } catch (error) {
            throw new Error('Error updating user: ' + error.message);
        }
    }

    async changeUserProfile(id, profileData) {
        const user = await userRepository.findById(id);
        if (!user) {
            const error = new Error('Người dùng không tồn tại');
            error.statusCode = 404;
            throw error;
        }

        if (!profileData.profilePicUrl) {
            profileData.profilePicUrl = user.profilePicUrl; 
        }

        try {
            const updatedUser = await userRepository.update(id, profileData);

            const domain = process.env.DOMAIN || 'localhost:3000';

            if (user.profilePicUrl != "default-avatar.png") {
                FileUtils.deleteFile(process.env.PROFILE_PICTURE_PATH + user.profilePicUrl);
            }
            return {
                ...updatedUser,
                profilePicUrl: updatedUser.profilePicUrl ? `http://${domain}/api/v1/uploads/profiles/${updatedUser.profilePicUrl}` : null
            };
        } catch (error) {
            throw new Error('Error updating user profile: ' + error.message);
        }
    }

    async changePassword(id, passwordData) {
        const user = await userRepository.findById(id);
        if(this.verifyPassword(passwordData.password, user.password) === false) {
            const error = new Error('Mật khẩu cũ không đúng');
            error.statusCode = 400;
            throw error;
        }

        if (!user) {
            const error = new Error('Người dùng không tồn tại');
            error.statusCode = 404;
            throw error;
        }

        if (passwordData.newPassword.trim() === '') {
            const error = new Error('Mật khẩu mới không được để trống');
            error.statusCode = 400;
            throw error;
        }

        if (passwordData.newPassword !== passwordData.confirmPassword) {
            const error = new Error('Mật khẩu mới không khớp');
            error.statusCode = 400;
            throw error;
        }

        try {
            const hashedPassword = await bcrypt.hash(passwordData.newPassword, 10);
            return await userRepository.update(id, { password: hashedPassword });
        } catch (error) {
            throw new Error('Error changing password: ' + error.message);
        }
    }

    async deleteUser(id) {
        try {
            const user = await userRepository.findById(id);
            if (!user) {
                const error = new Error('Không tìm thấy người dùng');
                error.statusCode = 404;
                throw error;
            }
            return await userRepository.delete(id);
        } catch (error) {
            throw new Error('Error deleting user: ' + error.message);
        }
    }

    async findByEmail(email) {
        const user = await userRepository.findByEmail(email);
        if (!user) {
            const error = new Error('Người dùng không tồn tại');
            error.statusCode = 404;
            throw error;
        }
        return user;
    }

    async verifyPassword(plainPassword, hashedPassword) {
        const isMatch = await bcrypt.compare(plainPassword, hashedPassword);
        if (!isMatch) {
            const error = new Error('Email hoặc mật khẩu không đúng');
            error.statusCode = 401;
            throw error;
        }
        return true;
    }
}

module.exports = new UserService();