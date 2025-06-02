const { error } = require('winston');
const userRepository = require('../repositories/user.repository');
const bcrypt = require('bcryptjs');


class UserService {
    async getAllUsers() {
        try {
            const users = await userRepository.findAll(); //users 
            return users.map(user => ({
                id: user.id,
                username: user.username,
                email: user.email,
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
        const data = {
            id: user.id,
            username: user.username,
            email: user.email,
            createdAt: user.createdAt,
            updatedAt: user.updatedAt
        }
        return data;
    }
    // {
    // }
    async register(userData) {
        const existingUser = await userRepository.findByEmail(userData.email);

        if (existingUser) {
            const error = new Error('Người dùng đã tồn tại');
            error.statusCode = 400;
            throw error;
        }

        try {
            // Loại bỏ confirmPassword trước khi lưu
            return await userRepository.create(userData);
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

        }

        // Loại bỏ confirmPassword trước khi lưu
        const { confirmPassword, ...userDataToUpdate } = userData;

        try {
            return await userRepository.update(id, userDataToUpdate);
        } catch (error) {
            throw new Error('Error updating user: ' + error.message);
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