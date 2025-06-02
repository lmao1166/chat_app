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
}

module.exports = new MemberRepository();
