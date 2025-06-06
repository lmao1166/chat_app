const conservation = require('../models/conversation.model.js');
const { Conversation, User, ConversationMember, Message, sequelize } = require('../models');
const { Op } = require('sequelize');

class ConservationRepository {
    async findAll() {
        try {
            const conservations = await conservation.findAll();
            return conservations;
        } catch (error) {
            throw new Error('Error fetching conservations: ' + error.message);
        }
    }    async findById(id, options = {}) {
        try {
            const conservationRecord = await conservation.findByPk(id, options);
            return conservationRecord;
        } catch (error) {
            throw new Error('Error fetching conservation with options: ' + error.message);
        }
    }    
    
    async findPrivateConversations(userId1, userId2) {
        try {
            const conversations = await sequelize.query(`
                SELECT DISTINCT c.id
                FROM conversations c
                INNER JOIN conversation_members cm1 ON c.id = cm1.conversation_id AND cm1.user_id = :userId1
                INNER JOIN conversation_members cm2 ON c.id = cm2.conversation_id AND cm2.user_id = :userId2
                WHERE c.type = 'private'
                AND (
                    SELECT COUNT(DISTINCT user_id) 
                    FROM conversation_members 
                    WHERE conversation_id = c.id
                ) = 2
            `, {
                replacements: { userId1, userId2 },
                type: sequelize.QueryTypes.SELECT
            });

            if (conversations.length > 0) {
                // Return the full conversation with members (bao gồm các trường cần thiết)
                return await Conversation.findByPk(conversations[0].id, {
                    include: [{
                        model: User,
                        as: 'members',
                        attributes: ['id', 'username', 'email', 'profilePicUrl'],
                        through: { attributes: ['joined_at'] },
                    }],
                });
            }

            return null;
        } catch (error) {
            throw new Error('Error fetching private conversation: ' + error.message);
        }
    }

    async create(conservationData) {
        const transaction = await conservation.sequelize.transaction();
        try {
            const newConservation = await conservation.create(conservationData, { transaction });
            await transaction.commit();
            return newConservation;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error creating conservation: ' + error.message);
        }
    }

    async update(id, conservationData) {
        const transaction = await conservation.sequelize.transaction();
        try {
            const [updatedRows] = await conservation.update(conservationData, {
                where: { id: id },
                transaction: transaction
            });
            await transaction.commit();
            return updatedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error updating conservation: ' + error.message);
        }
    }

    async delete(id) {
        const transaction = await conservation.sequelize.transaction();
        try {
            const deletedRows = await conservation.destroy({
                where: { id: id },
                transaction: transaction
            });
            await transaction.commit();
            return deletedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error deleting conservation: ' + error.message);
        }
    }    async findByUserId(userId) {        
        try {
            const conversations = await Conversation.findAll({
                include: [
                    {
                        model: User,
                        as: 'members',
                        through: { 
                            attributes: ['joined_at', 'left_at']
                        },
                        where: {
                            id: {
                                [Op.ne]: userId  // Chỉ lấy người dùng khác
                            }
                        },
                        attributes: ['id', 'username', 'email', 'profilePicUrl']
                    }
                ],
                where: {
                    id: {
                        [Op.in]: sequelize.literal(`(
                            SELECT conversation_id 
                            FROM conversation_members 
                            WHERE user_id = ${userId}
                        )`)
                    }
                },
                order: [['last_message_at', 'DESC']],
                distinct: true
            });

            // Lấy tin nhắn cuối cùng cho mỗi conversation
            const conversationsWithLastMessage = await Promise.all(
                conversations.map(async (conversation) => {
                    const conversationData = conversation.toJSON();
                    
                    // Lấy tin nhắn cuối cùng
                    const lastMessage = await Message.findOne({
                        where: { 
                            conversation_id: conversation.id,
                            is_deleted: false
                        },                        
                        include: [{
                            model: User,
                            as: 'sender',
                            attributes: ['id', 'username', 'profilePicUrl']
                        }],
                        order: [['timestamp', 'DESC']]
                    });
                    
                    if (lastMessage) {
                        conversationData.lastMessage = {
                            id: lastMessage.id,
                            content: lastMessage.content,
                            timestamp: lastMessage.timestamp,
                            message_type: lastMessage.message_type,
                            attachment_url: lastMessage.attachment_url,
                            sender: lastMessage.sender
                        };
                    } else {
                        conversationData.lastMessage = null;
                    }
                    
                    return conversationData;
                })
            );

            return conversationsWithLastMessage;
        } catch (error) {
            throw new Error('Error fetching conversations by user ID: ' + error.message);
        }
    }
}

module.exports = new ConservationRepository();