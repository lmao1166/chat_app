const conversationService = require('../services/conversation.service');

class ConservationController {
    async findOrCreateConversation(req, res, next) {
        try {
            const memberData = req.body;
            const userId = req.user.userId;
            const conversation = await conversationService.findOrCreateConversation(memberData, userId);

            res.status(200).json({
                status: 200,
                success: true,
                data: conversation,
                message: 'Tạo hoặc tìm cuộc trò chuyện thành công'
            });
        } catch (error) {
            next(error);
        }
    }

    async getConversationById(req, res, next) {
        try {
            const conversationId = req.params.id;
            const conversation = await conversationService.getConversationById(conversationId);
            if (!conversation) {
                return res.status(404).json({ message: 'Cuộc trò chuyện không tồn tại' });
            }
            res.status(200).json(
                {
                    status: 200,
                    success: true,
                    data: conversation,
                    message: 'Lấy cuộc trò chuyện thành công'
                }
            );
        } catch (error) {
            next(error);
        }
    }  

    async getConversations(req, res, next) {
        try {
            const userId = req.user.userId;
            const conversations = await conversationService.getConversationsByUserId(userId);
            res.status(200).json(
                {
                    status: 200,
                    success: true,
                    data: conversations,
                    message: 'Lấy danh sách cuộc trò chuyện theo người dùng thành công'
                }
            );
        } catch (error) {
            next(error);
        }
    }
}

module.exports = new ConservationController();