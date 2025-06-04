const conservationMember = require('../models/conversationMember.model');

class MemberRepository {
    async create(memberData) {
        const transaction = await conservationMember.sequelize.transaction();
        try {
            const newMember = await conservationMember.create(memberData, { transaction });
            await transaction.commit();
            return newMember;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error creating member: ' + error.message);
        }
    }


    async bulkCreate(membersData) {
        const transaction = await conservationMember.sequelize.transaction();
        try {
            const newMembers = await conservationMember.bulkCreate(membersData, { transaction });
            await transaction.commit();
            return newMembers;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error bulk creating members: ' + error.message);
        }
    }

    async isUserInConversation(userId, conversationId) {
        try {
            const member = await conservationMember.findOne({
                where: {
                    user_id: userId,
                    conversation_id: conversationId
                }
            });
            return !!member; // Trả về true nếu tìm thấy, false nếu không
        } catch (error) {
            throw new Error('Error checking user membership: ' + error.message);
        }
    }
}

module.exports = new MemberRepository();
