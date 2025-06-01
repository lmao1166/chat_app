const userRepository = require('../repositories/user.repository');
const conversationRepository = require('../repositories/conversation.repository');
const memberRepository = require('../repositories/member.repository');
const User = require('../models/user.model');

class ConversationService {
    async findOrCreateConversation(memberData, userId) {
        try {
            const user1 = await userRepository.findById(userId);
            const user2 = await userRepository.findById(memberData.userId2);

            if (!user1 || !user2) {
                const error = new Error('Một trong hai người dùng không tồn tại');
                error.statusCode = 404;
                throw error;
            }

            if( user1.id === user2.id) {
                const error = new Error('Không thể tạo cuộc trò chuyện với chính mình');
                error.statusCode = 400;
            }

            const existingConversation = await conversationRepository.findPrivateConversations(userId, memberData.userId2);
            if (existingConversation) {
                const { members, ...conversationData } = existingConversation.toJSON();
                return conversationData;
            }

            const newConversation = await conversationRepository.create({
                type: 'private',
                name: null
            });
            await memberRepository.bulkCreate([
                { conversation_id: newConversation.id, user_id: user1.id, joined_at: new Date() },
                { conversation_id: newConversation.id, user_id: user2.id, joined_at: new Date() }
            ])

            const createdConversationWithMembers = await conversationRepository.findById(newConversation.id, {
                include: [{
                    model: User,
                    as: 'members',
                    attributes: ['id', 'username', 'email']
                }]
            });

            const { members, ...conversationData } = createdConversationWithMembers.toJSON();
            return conversationData;

        } catch (error) {
            console.error('Error in findOrCreateConversation:', error);
            throw new Error('Không thể tìm hoặc tạo cuộc trò chuyện: ' + error.message);
        }
    }

    async getConversationById(conversationId) {
        try {            const conversation = await conversationRepository.findById(conversationId, {
                include: [{
                    model: User,
                    as: 'members',
                    attributes: ['id', 'username', 'email']
                }]
            });

            if (!conversation) {
                const error = new Error('Không tìm thấy cuộc trò chuyện');
                error.statusCode = 404;
                throw error;
            }

            return conversation.toJSON();
        } catch (error) {
            console.error('Error in getConversationById:', error);
            throw new Error('Không thể lấy cuộc trò chuyện: ' + error.message);
        }
    }

    async getConversationsByUserId(userId) {
        try {
            const conversations = await conversationRepository.findByUserId(userId, {
                include: [{
                    model: User,
                    as: 'members',
                    attributes: ['id', 'username', 'email']
                }]
            });

            return conversations.map(conversation => conversation.toJSON());
        } catch (error) {
            console.error('Error in getConversationsByUserId:', error);
            throw new Error('Không thể lấy cuộc trò chuyện của người dùng: ' + error.message);
        }
    }
}

module.exports = new ConversationService();