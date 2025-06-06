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
            }            const existingConversation = await conversationRepository.findPrivateConversations(userId, memberData.userId2);
            if (existingConversation) {
                const conversationData = existingConversation.toJSON();
                
                // Set thumbnail to the other member's profile picture
                const otherMember = conversationData.members.find(member => member.id !== userId);
                if (otherMember && otherMember.profilePicUrl) {
                    const fullProfileUrl = `http://192.168.1.10:3000/api/v1/uploads/profiles/${otherMember.profilePicUrl}`;
                    conversationData.thumbnail = fullProfileUrl;
                    
                    // Also update the member's profilePicUrl to full URL
                    conversationData.members.forEach(member => {
                        if (member.profilePicUrl) {
                            member.profilePicUrl = `http://192.168.1.10:3000/api/v1/uploads/profiles/${member.profilePicUrl}`;
                        }
                    });
                }
                
                const { members, ...responseData } = conversationData;
                return responseData;
            }

            const newConversation = await conversationRepository.create({
                type: 'private',
                name: null
            });            await memberRepository.bulkCreate([
                { conversation_id: newConversation.id, user_id: user1.id, joined_at: new Date() },
                { conversation_id: newConversation.id, user_id: user2.id, joined_at: new Date() }
            ]);

            const createdConversationWithMembers = await conversationRepository.findById(newConversation.id, {
                include: [{
                    model: User,
                    as: 'members',
                    attributes: ['id', 'username', 'email', 'profilePicUrl']
                }]
            });            const conversationData = createdConversationWithMembers.toJSON();
            
            // Set thumbnail to the other member's profile picture
            const otherMember = conversationData.members.find(member => member.id !== userId);
            if (otherMember && otherMember.profilePicUrl) {
                const fullProfileUrl = `http://192.168.1.10:3000/api/v1/uploads/profiles/${otherMember.profilePicUrl}`;
                conversationData.thumbnail = fullProfileUrl;
                
                // Also update the member's profilePicUrl to full URL
                conversationData.members.forEach(member => {
                    if (member.profilePicUrl) {
                        member.profilePicUrl = `http://192.168.1.10:3000/api/v1/uploads/profiles/${member.profilePicUrl}`;
                    }
                });
            }
            
            const { members, ...responseData } = conversationData;
            return responseData;

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
            const conversations = await conversationRepository.findByUserId(userId);

            return conversations.map(conversation => {
                // Repository already returns plain objects, no need to call toJSON()
                const conversationData = conversation;
                
                // Set thumbnail to the member's profile picture (for private conversations)
                if (conversationData.type === 'private' && conversationData.members.length > 0) {
                    const member = conversationData.members[0];
                    if (member.profilePicUrl) {
                        // Add full URL prefix for the profile picture
                        const fullProfileUrl = `http://192.168.1.10:3000/api/v1/uploads/profiles/${member.profilePicUrl}`;
                        conversationData.thumbnail = fullProfileUrl;
                        
                        // Also update the member's profilePicUrl to full URL
                        member.profilePicUrl = fullProfileUrl;
                    }
                }
                
                return conversationData;
            });
        } catch (error) {
            console.error('Error in getConversationsByUserId:', error);
            throw new Error('Không thể lấy cuộc trò chuyện của người dùng: ' + error.message);
        }
    }
}

module.exports = new ConversationService();