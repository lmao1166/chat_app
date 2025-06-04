const userService = require('../services/user.service');
const socketService = require('../services/socket.service');
const FileUtils = require('../utils/file.utils');

const getAllUsers = async (req, res, next) => { //get
    try {
        const users = await userService.getAllUsers();
        res.status(200).json(
            {
                status: 200,
                success: true,
                data: users,
                message: 'Lấy danh sách người dùng thành công',
            }
        );
    } catch (error) {
        next(error);
    }
};

const getUserById = async (req, res, next) => {
    try {
        const user = await userService.getUserById(req.params.id);
        res.status(200).json({
            status: 200,
            success: true,
            data: user,
            message: 'Lấy thông tin người dùng thành công'
        });
    } catch (error) {
        next(error);
    }
};

const register = async (req, res, next) => { //post
    try {
        const newUser = await userService.register(req.body);
        res.status(201).json({
            status: 201,
            success: true,
            data: newUser,
            message: 'Đăng ký người dùng thành công'
        });
    } catch (error) {
        next(error);
    }
};

const updateUser = async (req, res, next) => { //put 
    try {
        const userId = req.user.userId;
        const userData = req.body;
        
        console.log('Update user request:', {
            userId,
            userData,
            files: req.files
        });
        
        // Check if a new profile picture was uploaded
        let profilePicUrl = null;
        if (req.files && req.files.profilePicture && req.files.profilePicture[0]) {
            const uploadedFile = req.files.profilePicture[0];
            // Chỉ lưu tên file, không lưu đường dẫn đầy đủ
            profilePicUrl = FileUtils.extractFileName(uploadedFile.filename);
            
            console.log('Profile picture uploaded:', {
                filename: uploadedFile.filename,
                originalname: uploadedFile.originalname,
                size: uploadedFile.size
            });
        }
        
        // Add profile picture URL to user data if a new one was uploaded
        if (profilePicUrl) {
            userData.profilePicUrl = profilePicUrl;
        }
        
        const updatedUser = await userService.changeUserProfile(userId, userData);

        res.status(200).json({
            status: 200,
            success: true,
            data: updatedUser,
            message: 'Cập nhật thông tin người dùng thành công'
        });
    } catch (error) {
        next(error);
    }
};

const changePassword = async (req, res, next) => { 
    try {
        const userId = req.user.userId;
        const passwordData = req.body;

        const updatedUser = await userService.changePassword(userId, passwordData);

        res.status(200).json({
            status: 200,
            success: true,
            data: updatedUser,
            message: 'Đổi mật khẩu thành công'
        });
    } catch (error) {
        next(error);
    }
}

const deleteUser = async (req, res, next) => { //delete
    try {
        await userService.deleteUser(req.params.id);
        res.status(200).json({
            status: 200,
            success: true,
            message: 'Xóa người dùng thành công'
        });
    } catch (error) {
        next(error);
    }
};

const getOnlineUsers = async (req, res, next) => {
    try {
        const onlineUserIds = socketService.getOnlineUsers();
        const onlineUsers = await Promise.allSettled(
            onlineUserIds.map(userId => userService.getUserById(userId))
        );
        
        const validUsers = onlineUsers
            .filter(result => result.status === 'fulfilled')
            .map(result => result.value);

        res.status(200).json({
            status: 200,
            success: true,
            data: validUsers,
            count: validUsers.length,
            message: 'Lấy danh sách người dùng online thành công'
        });
    } catch (error) {
        next(error);
    }
};

module.exports = {
    getAllUsers,
    getUserById,
    register,
    updateUser,
    deleteUser,
    getOnlineUsers,
    changePassword,
};